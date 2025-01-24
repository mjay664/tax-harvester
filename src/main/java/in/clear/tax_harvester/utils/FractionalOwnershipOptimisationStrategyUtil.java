package in.clear.tax_harvester.utils;

import in.clear.tax_harvester.constant.FundType;
import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.FundFolioData;
import in.clear.tax_harvester.dto.TransactionData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FractionalOwnershipOptimisationStrategyUtil {

    private final static BigDecimal targetLTCG = BigDecimal.valueOf(125000);
    private final static long oneYearMillis = 365L * 24 * 60 * 60 * 1000;
    private final static long threeYearMillis = 3 * oneYearMillis;
    private final static String MP = "MF";
    private final static Map<String, Long> investmentTypeToLongTermMills = Map.of(MP, oneYearMillis);
    private final static long grandFatheringDate = 1517423400000L;

    public static FolioDataResponse getOptimisedStockSellingOrder(FolioDataResponse folioDataResponse) {
        return getOptimisedStockSellingOrder(folioDataResponse, 0);
    }

    public static FolioDataResponse getOptimisedStockSellingOrder(FolioDataResponse folioDataResponse,  int year) {
        List<FundFolioData> updatedFolioDataList = new ArrayList<>();

        long currentTimeMillis = System.currentTimeMillis() + year * oneYearMillis;

        BigDecimal remainingLTCG = targetLTCG;
        for (FundFolioData fundFolioData : folioDataResponse.getFolioDataList()) {
            if (FundType.isDebtRelated(fundFolioData.getFundType()) || BigDecimal.ZERO.compareTo(fundFolioData.getUnits()) == 0) {
                updatedFolioDataList.add(fundFolioData);
                continue;
            }

            List<FolioTransactionData> updatedTransactionDataList = new ArrayList<>();
            List<TransactionData> ltcgOptions = new ArrayList<>();

            fundFolioData.getFolioTransactionDataList().sort(Comparator.comparing(FolioTransactionData::getInvestmentDate));
            for (FolioTransactionData folioTransactionData : fundFolioData.getFolioTransactionDataList()) {
                if ((currentTimeMillis - folioTransactionData.getInvestmentDate().getTime()) >= investmentTypeToLongTermMills.get(MP)
                        && folioTransactionData.getInvestmentDate().getTime() >= grandFatheringDate) {
                    BigDecimal buyPricePerUnit = folioTransactionData.getNav();
                    BigDecimal ltcgPerUnit = folioTransactionData.getCurrentNav().subtract(buyPricePerUnit);
                    var isLtcgApplicable = isLtcgApplicable(fundFolioData, folioTransactionData, ltcgPerUnit, currentTimeMillis);
                    if(isLtcgApplicable) {
                        TransactionData transactionData = TransactionData.builder()
                                .ltcgPerUnit(ltcgPerUnit)
                                .units(folioTransactionData.getUnits())
                                .currentNav(folioTransactionData.getCurrentNav())
                                .transactionNumber(folioTransactionData.getTransactionNumber())
                                .investmentDate(folioTransactionData.getInvestmentDate())
                                .build();
                        ltcgOptions.add(transactionData);
                    }
                }
            }

            for (TransactionData option : ltcgOptions) {
                BigDecimal ltcgPerUnit = option.getLtcgPerUnit();
                BigDecimal unitsAvailable = option.getUnits();
                BigDecimal currentNav = option.getCurrentNav();

                BigDecimal unitsToSell = unitsAvailable.min(remainingLTCG.divide(ltcgPerUnit,2, RoundingMode.HALF_UP));
                if (BigDecimal.ZERO.compareTo(unitsToSell) < 0) {
                    FolioTransactionData updatedTransaction = FolioTransactionData.builder()
                            .units(unitsToSell)
                            .profit(ltcgPerUnit.multiply(unitsToSell))
                            .amountToSell(unitsToSell.multiply(currentNav))
                            .transactionNumber(option.getTransactionNumber())
                            .investmentDate(option.getInvestmentDate())
                            .build();

                    updatedTransactionDataList.add(updatedTransaction);
                    remainingLTCG = remainingLTCG.subtract(ltcgPerUnit.multiply(unitsToSell));
                }

                if (BigDecimal.ZERO.compareTo(remainingLTCG) >= 0) break;
            }

            updatedTransactionDataList.sort(Comparator.comparing(FolioTransactionData::getInvestmentDate));
            FundFolioData updatedFundFolioData = fundFolioData.toBuilder()
                    .isinCode(fundFolioData.getIsinCode())
                    .fundName(fundFolioData.getFundName())
                    .folioTransactionDataList(updatedTransactionDataList)
                    .profit(updatedTransactionDataList.stream().map(FolioTransactionData::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .amountToSell(updatedTransactionDataList.stream().map(FolioTransactionData::getAmountToSell).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .build();

            updatedFolioDataList.add(updatedFundFolioData);
        }

        return FolioDataResponse.builder().folioDataList(updatedFolioDataList).build();
    }

    private static boolean isLtcgApplicable(FundFolioData fundFolioData, FolioTransactionData folioTransactionData, BigDecimal ltcgPerUnit, long currentTimeMillis) {
        return BigDecimal.ZERO.compareTo(ltcgPerUnit) < 0 && (!fundFolioData.isELSS() || fundFolioData.isELSS() && (folioTransactionData.getInvestmentDate()
                                                                                 .getTime() + threeYearMillis) < currentTimeMillis);
    }
}
