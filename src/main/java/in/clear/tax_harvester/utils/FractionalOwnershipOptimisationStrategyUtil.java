package in.clear.tax_harvester.utils;

import in.clear.tax_harvester.constant.FundType;
import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.FundFolioData;
import in.clear.tax_harvester.dto.TransactionData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FractionalOwnershipOptimisationStrategyUtil {

    private final static double targetLTCG = 125000.0;
    private final static long oneYearMillis = 365L * 24 * 60 * 60 * 1000;
    private final static long threeYearMillis = 3 * oneYearMillis;
    private final static String MP = "MF";
    private final static Map<String, Long> investmentTypeToLongTermMills = Map.of(MP, oneYearMillis);
    private final static long grandFatheringDate = 1517423400000L;

    public static FolioDataResponse getOptimisedStockSellingOrder(FolioDataResponse folioDataResponse) {
        List<FundFolioData> updatedFolioDataList = new ArrayList<>();

        long currentTimeMillis = System.currentTimeMillis();

        double remainingLTCG = targetLTCG;
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
                    double ltcgPerUnit = folioTransactionData.getCurrentNav().subtract(buyPricePerUnit).doubleValue();
                    var isLtcgApplicable = isLtcgApplicable(fundFolioData, folioTransactionData, ltcgPerUnit, currentTimeMillis);
                    if(isLtcgApplicable) {
                        TransactionData transactionData = TransactionData.builder()
                                .ltcgPerUnit(ltcgPerUnit)
                                .units(folioTransactionData.getUnits().doubleValue())
                                .currentNav(folioTransactionData.getCurrentNav().doubleValue())
                                .transactionNumber(folioTransactionData.getTransactionNumber())
                                .investmentDate(folioTransactionData.getInvestmentDate())
                                .build();
                        ltcgOptions.add(transactionData);
                    }
                }
            }

            for (TransactionData option : ltcgOptions) {
                double ltcgPerUnit = option.getLtcgPerUnit();
                double unitsAvailable = option.getUnits();
                double currentNav = option.getCurrentNav();

                double unitsToSell = Math.min(unitsAvailable, remainingLTCG / ltcgPerUnit);
                if (unitsToSell > 0) {
                    FolioTransactionData updatedTransaction = FolioTransactionData.builder()
                            .units(BigDecimal.valueOf(unitsToSell).setScale(2, BigDecimal.ROUND_HALF_UP))
                            .profit(ltcgPerUnit * unitsToSell)
                            .amountToSell(unitsToSell * currentNav)
                            .transactionNumber(option.getTransactionNumber())
                            .investmentDate(option.getInvestmentDate())
                            .build();

                    updatedTransactionDataList.add(updatedTransaction);
                    remainingLTCG -= ltcgPerUnit * unitsToSell;
                }

                if (remainingLTCG <= 0) break;
            }

            updatedTransactionDataList.sort(Comparator.comparing(FolioTransactionData::getInvestmentDate));
            FundFolioData updatedFundFolioData = fundFolioData.toBuilder()
                    .isinCode(fundFolioData.getIsinCode())
                    .fundName(fundFolioData.getFundName())
                    .folioTransactionDataList(updatedTransactionDataList)
                    .profit(updatedTransactionDataList.stream().map(FolioTransactionData::getProfit).mapToDouble(Double::doubleValue).sum())
                    .amountToSell(updatedTransactionDataList.stream().map(FolioTransactionData::getAmountToSell).mapToDouble(Double::doubleValue).sum())
                    .build();

            updatedFolioDataList.add(updatedFundFolioData);
        }

        return FolioDataResponse.builder().folioDataList(updatedFolioDataList).build();
    }

    public static FolioDataResponse getOptimisedStockSellingOrder(FolioDataResponse folioDataResponse, long millisToCheckWith) {
        List<FundFolioData> updatedFolioDataList = new ArrayList<>();

        long currentTimeMillis = System.currentTimeMillis();

        double remainingLTCG = targetLTCG;
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
                        && folioTransactionData.getInvestmentDate().getTime() >= millisToCheckWith) {
                    BigDecimal buyPricePerUnit = folioTransactionData.getNav();
                    double ltcgPerUnit = folioTransactionData.getCurrentNav().subtract(buyPricePerUnit).doubleValue();
                    var isLtcgApplicable = isLtcgApplicable(fundFolioData, folioTransactionData, ltcgPerUnit, currentTimeMillis);
                    if(isLtcgApplicable) {
                        TransactionData transactionData = TransactionData.builder()
                                .ltcgPerUnit(ltcgPerUnit)
                                .units(folioTransactionData.getUnits().doubleValue())
                                .currentNav(folioTransactionData.getCurrentNav().doubleValue())
                                .transactionNumber(folioTransactionData.getTransactionNumber())
                                .investmentDate(folioTransactionData.getInvestmentDate())
                                .build();
                        ltcgOptions.add(transactionData);
                    }
                }
            }

            for (TransactionData option : ltcgOptions) {
                double ltcgPerUnit = option.getLtcgPerUnit();
                double unitsAvailable = option.getUnits();
                double currentNav = option.getCurrentNav();

                double unitsToSell = Math.min(unitsAvailable, remainingLTCG / ltcgPerUnit);
                if (unitsToSell > 0) {
                    FolioTransactionData updatedTransaction = FolioTransactionData.builder()
                            .units(BigDecimal.valueOf(unitsToSell).setScale(2, BigDecimal.ROUND_HALF_UP))
                            .profit(ltcgPerUnit * unitsToSell)
                            .amountToSell(unitsToSell * currentNav)
                            .transactionNumber(option.getTransactionNumber())
                            .investmentDate(option.getInvestmentDate())
                            .build();

                    updatedTransactionDataList.add(updatedTransaction);
                    remainingLTCG -= ltcgPerUnit * unitsToSell;
                }

                if (remainingLTCG <= 0) break;
            }

            updatedTransactionDataList.sort(Comparator.comparing(FolioTransactionData::getInvestmentDate));
            FundFolioData updatedFundFolioData = fundFolioData.toBuilder()
                    .isinCode(fundFolioData.getIsinCode())
                    .fundName(fundFolioData.getFundName())
                    .folioTransactionDataList(updatedTransactionDataList)
                    .profit(updatedTransactionDataList.stream().map(FolioTransactionData::getProfit).mapToDouble(Double::doubleValue).sum())
                    .amountToSell(updatedTransactionDataList.stream().map(FolioTransactionData::getAmountToSell).mapToDouble(Double::doubleValue).sum())
                    .build();

            updatedFolioDataList.add(updatedFundFolioData);
        }

        return FolioDataResponse.builder().folioDataList(updatedFolioDataList).build();
    }

    private static boolean isLtcgApplicable(FundFolioData fundFolioData, FolioTransactionData folioTransactionData, double ltcgPerUnit, long currentTimeMillis) {
        return ltcgPerUnit > 0 && (!fundFolioData.isELSS() || fundFolioData.isELSS() && (folioTransactionData.getInvestmentDate()
                                                                                 .getTime() + threeYearMillis) < currentTimeMillis);
    }
}
