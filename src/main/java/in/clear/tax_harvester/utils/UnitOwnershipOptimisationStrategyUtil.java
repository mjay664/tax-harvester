package in.clear.tax_harvester.utils;

import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.FundFolioData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnitOwnershipOptimisationStrategyUtil {

    private final static double targetLTCG = 125000.0;
    private final static long oneYearMillis = 365L * 24 * 60 * 60 * 1000;
    private final static String MP = "MF";
    private final static Map<String, Long> investmentTypeToLongTermMills = Map.of(MP, oneYearMillis);
    private final static long currentTimeMillis = System.currentTimeMillis();
    private final static long grandFatheringDate = 1517423400000L;

    public static FolioDataResponse getOptimisedStockSellingOrder(FolioDataResponse folioDataResponse) {
        List<FundFolioData> updatedFolioDataList = new ArrayList<>();

        for (FundFolioData fundFolioData : folioDataResponse.getFolioDataList()) {
            List<FolioTransactionData> updatedTransactionDataList = new ArrayList<>();
            List<double[]> ltcgOptions = new ArrayList<>();

            for (FolioTransactionData folioTransactionData : fundFolioData.getFolioTransactionDataList()) {
                if ((currentTimeMillis - folioTransactionData.getInvestmentDate().getTime()) >= investmentTypeToLongTermMills.get(MP)
                        && folioTransactionData.getInvestmentDate().getTime() >= grandFatheringDate) {
                    BigDecimal buyPricePerUnit = folioTransactionData.getNav();
                    double ltcgPerUnit = folioTransactionData.getCurrentNav().subtract(buyPricePerUnit).doubleValue();
                    ltcgOptions.add(new double[]{ltcgPerUnit, folioTransactionData.getUnits().doubleValue(), folioTransactionData.getInvestmentDate().getTime()});
                }
            }

            double maxProfit = knapsackOptimization(ltcgOptions, targetLTCG);
            double remainingLTCG = maxProfit;

            for (double[] option : ltcgOptions) {
                double ltcgPerUnit = option[0];
                double unitsAvailable = option[1];

                double unitsToSell = Math.min(unitsAvailable, remainingLTCG / ltcgPerUnit);
                if (unitsToSell > 0) {
                    FolioTransactionData updatedTransaction = FolioTransactionData.builder()
                            .units(BigDecimal.valueOf(unitsToSell))
                            .profit(ltcgPerUnit * unitsToSell)
                            .amountToSell(unitsToSell * fundFolioData.getNav().doubleValue())
                            .build();

                    updatedTransactionDataList.add(updatedTransaction);
                    remainingLTCG -= ltcgPerUnit * unitsToSell;
                }

                if (remainingLTCG <= 0) break;
            }

            FundFolioData updatedFundFolioData = FundFolioData.builder()
                    .isinCode(fundFolioData.getIsinCode())
                    .fundName(fundFolioData.getFundName())
                    .units(updatedTransactionDataList.stream().map(FolioTransactionData::getUnits).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .folioTransactionDataList(updatedTransactionDataList)
                    .profit(updatedTransactionDataList.stream().map(FolioTransactionData::getProfit).mapToDouble(Double::doubleValue).sum())
                    .amountToSell(updatedTransactionDataList.stream().map(FolioTransactionData::getAmountToSell).mapToDouble(Double::doubleValue).sum())
                    .build();

            updatedFolioDataList.add(updatedFundFolioData);
        }
        return FolioDataResponse.builder().folioDataList(updatedFolioDataList).build();
    }

    public static double knapsackOptimization(List<double[]> ltcgOptions, double maxLTCG) {
        int n = ltcgOptions.size();
        int W = (int) maxLTCG;

        double[][] dp = new double[n + 1][W + 1];

        for (int i = 1; i <= n; i++) {
            double[] currentOption = ltcgOptions.get(i - 1);
            double currentLTCG = currentOption[0];
            int quantity = (int) Math.floor(currentOption[1]);

            for (int w = 0; w <= W; w++) {
                if (w >= currentLTCG) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][(int) (w - currentLTCG)] + currentLTCG * quantity);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        return dp[n][W];
    }

}
