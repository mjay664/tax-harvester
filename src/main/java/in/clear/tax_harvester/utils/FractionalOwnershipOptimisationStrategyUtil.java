package in.clear.tax_harvester.utils;

import in.clear.tax_harvester.constant.FundType;
import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.FundFolioData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static in.clear.tax_harvester.constant.FundType.EQUITY;

public class FractionalOwnershipOptimisationStrategyUtil {

    private final static double targetLTCG = 125000.0;
    private final static long oneYearMillis = 365L * 24 * 60 * 60 * 1000;
    private final static String MP = "MF";
    private final static Map<String, Long> investmentTypeToLongTermMills = Map.of(MP, oneYearMillis);
    private final static long currentTimeMillis = System.currentTimeMillis();
    private final static long grandFatheringDate = 1517423400000L;

    public static FolioDataResponse getOptimisedStockSellingOrder(FolioDataResponse folioDataResponse) {
        List<FundFolioData> updatedFolioDataList = new ArrayList<>();

        double remainingLTCG = targetLTCG;
        for (FundFolioData fundFolioData : folioDataResponse.getFolioDataList()) {
            if (!FundType.isEquityRelated(fundFolioData.getFundType())) {
                updatedFolioDataList.add(fundFolioData);
                continue;
            }

            List<FolioTransactionData> updatedTransactionDataList = new ArrayList<>();
            List<double[]> ltcgOptions = new ArrayList<>();

            fundFolioData.getFolioTransactionDataList().sort(Comparator.comparing(FolioTransactionData::getInvestmentDate));
            for (FolioTransactionData folioTransactionData : fundFolioData.getFolioTransactionDataList()) {
                if ((currentTimeMillis - folioTransactionData.getInvestmentDate().getTime()) >= investmentTypeToLongTermMills.get(MP)
                        && folioTransactionData.getInvestmentDate().getTime() >= grandFatheringDate) {
                    BigDecimal buyPricePerUnit = folioTransactionData.getNav();
                    double ltcgPerUnit = folioTransactionData.getCurrentNav().subtract(buyPricePerUnit).doubleValue();
                    if(ltcgPerUnit > 0) {
                        ltcgOptions.add(new double[]{ltcgPerUnit, folioTransactionData.getUnits().doubleValue(), folioTransactionData.getInvestmentDate().getTime()});
                    }
                }
            }

            for (double[] option : ltcgOptions) {
                double ltcgPerUnit = option[0];
                double unitsAvailable = option[1];

                double unitsToSell = Math.min(unitsAvailable, remainingLTCG / ltcgPerUnit);
                if (unitsToSell > 0) {
                    FolioTransactionData updatedTransaction = FolioTransactionData.builder()
                            .units(BigDecimal.valueOf(unitsToSell).setScale(2, BigDecimal.ROUND_HALF_UP))
                            .profit(String.valueOf(ltcgPerUnit * unitsToSell))
                            .build();

                    updatedTransactionDataList.add(updatedTransaction);
                    remainingLTCG -= ltcgPerUnit * unitsToSell;
                }

                if (remainingLTCG <= 0) break;
            }

            FundFolioData updatedFundFolioData = fundFolioData.toBuilder()
                    .isinCode(fundFolioData.getIsinCode())
                    .fundName(fundFolioData.getFundName())
                    .units(updatedTransactionDataList.stream().map(FolioTransactionData::getUnits).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .folioTransactionDataList(updatedTransactionDataList)
                    .profit(updatedTransactionDataList.stream().map(FolioTransactionData::getProfit).mapToDouble(Double::parseDouble).sum())
                    .build();

            updatedFolioDataList.add(updatedFundFolioData);
        }

        return FolioDataResponse.builder().folioDataList(updatedFolioDataList).build();
    }

}
