package in.clear.tax_harvester.service.impl;

import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.FundFolioData;
import in.clear.tax_harvester.dto.GraphDataDTO;
import in.clear.tax_harvester.dto.GraphDataSetDTO;
import in.clear.tax_harvester.dto.GraphResponseDTO;
import in.clear.tax_harvester.dto.ListGraphResponseDTO;
import in.clear.tax_harvester.service.GraphService;
import in.clear.tax_harvester.utils.FractionalOwnershipOptimisationStrategyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraphServiceImpl implements GraphService {

    private static final BigDecimal ANNUAL_GROWTH_RATE = new BigDecimal("0.10");
    private static final BigDecimal EXEMPTION_LIMIT = new BigDecimal("125000");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    public ListGraphResponseDTO getGraphData(FolioDataResponse folioDataResponse, int years) {
        // Create the dataset for both strategies
        GraphDataSetDTO existingStrategyDataSet = generateDataSetForExistingStrategy(folioDataResponse, years);
        GraphDataSetDTO ourStrategyDataSet = generateDataSetForOurStrategy(folioDataResponse, years);

        GraphResponseDTO taxGraph = GraphResponseDTO.builder()
                .dataSets(List.of(existingStrategyDataSet, ourStrategyDataSet))
                .xAxis("Years")
                .yAxis("Tax Liability")
                .title("Tax Liability Comparison")
                .build();

        GraphResponseDTO patGraph = GraphResponseDTO.builder()
                .dataSets(getProfitAfterTaxDatasets(existingStrategyDataSet, ourStrategyDataSet))
                .xAxis("Years")
                .yAxis("Profit After Tax")
                .title("Profit After Tax Comparison")
                .build();

        return new ListGraphResponseDTO(List.of(taxGraph, patGraph));
    }

    private List<GraphDataSetDTO> getProfitAfterTaxDatasets(GraphDataSetDTO existingStrategyDataSet, GraphDataSetDTO ourStrategyDataSet) {
        List<GraphDataDTO> profitAfterTaxExistingStrategy = new ArrayList<>();
        List<GraphDataDTO> profitAfterTaxOurStrategy = new ArrayList<>();

        for (int i = 0; i < existingStrategyDataSet.getData().size(); i++) {
            GraphDataDTO existingStrategyData = existingStrategyDataSet.getData().get(i);
            GraphDataDTO ourStrategyData = ourStrategyDataSet.getData().get(i);

            BigDecimal profitAfterTaxExisting = existingStrategyData.getProfitBooked().subtract(existingStrategyData.getAmount());
            BigDecimal profitAfterTaxOur = existingStrategyData.getProfitBooked().subtract(ourStrategyData.getAmount());

            profitAfterTaxExistingStrategy.add(new GraphDataDTO(existingStrategyData.getTime(), profitAfterTaxExisting, null));
            profitAfterTaxOurStrategy.add(new GraphDataDTO(ourStrategyData.getTime(), profitAfterTaxOur, null));
        }

        return List.of(
                new GraphDataSetDTO(profitAfterTaxExistingStrategy, "Existing Strategy (Profit After Tax)"),
                new GraphDataSetDTO(profitAfterTaxOurStrategy, "Our Strategy (Profit After Tax)")
        );
    }

    private GraphDataSetDTO generateDataSetForExistingStrategy(FolioDataResponse folioDataResponse, int years) {
        List<FolioTransactionData> currentTrxns =
                folioDataResponse.getFolioDataList().stream().map(FundFolioData::getFolioTransactionDataList).flatMap(List::stream).collect(Collectors.toList());
        currentTrxns.sort(Comparator.comparing(FolioTransactionData::getInvestmentDate));
        List<GraphDataDTO> data = new ArrayList<>();

        BigDecimal totalInitialInvestment = null;
        BigDecimal totalCurrentValue = null;

        for (int year = 0; year < years; year++) {
            BigDecimal totalProfit = BigDecimal.ZERO;
            BigDecimal totalTaxLiability = BigDecimal.ZERO;

            if (totalInitialInvestment == null) {
                totalInitialInvestment = BigDecimal.ZERO;
                totalCurrentValue = BigDecimal.ZERO;
                for (FolioTransactionData transaction : currentTrxns) {
                    totalCurrentValue = totalCurrentValue.add(transaction.getCurrentAmount());
                    totalInitialInvestment = totalInitialInvestment.add(transaction.getInvestedAmount());
                }
            } else {
                totalCurrentValue = totalCurrentValue.multiply(BigDecimal.ONE.add(ANNUAL_GROWTH_RATE));
            }

            totalProfit = totalCurrentValue.subtract(totalInitialInvestment);
            var tax = totalProfit.subtract(EXEMPTION_LIMIT).multiply(TAX_RATE);
            totalTaxLiability = tax.max(BigDecimal.ZERO);

            data.add(new GraphDataDTO("Year " + year, totalTaxLiability, totalProfit));
        }

        return GraphDataSetDTO.builder()
                .data(data)
                .label("Existing Strategy (Tax Liabilities)")
                .build();
    }

    private GraphDataSetDTO generateDataSetForOurStrategy(FolioDataResponse currentFolio, int years) {
        List<GraphDataDTO> data = new ArrayList<>();

        for (int year = 0; year < years; year++) {
            FolioDataResponse sellOrdersFolio = FractionalOwnershipOptimisationStrategyUtil.getOptimisedStockSellingOrder(currentFolio, year);
            BigDecimal totalProfit = getTotalProfitAfterSellingAll(currentFolio);
            BigDecimal tax = totalProfit.subtract(EXEMPTION_LIMIT).multiply(TAX_RATE);
            BigDecimal totalTaxLiability = tax.max(BigDecimal.ZERO);
            data.add(new GraphDataDTO("Year " + year, totalTaxLiability, totalProfit));

            updateFolioData(currentFolio, sellOrdersFolio, year);
            updateFolioForNextYear(currentFolio);
        }
        return new GraphDataSetDTO(data, "Our Strategy (Tax Liabilities)");
    }

    private void updateFolioForNextYear(FolioDataResponse currentFolio) {
        for (FundFolioData folioData : currentFolio.getFolioDataList()) {
            folioData.getFolioTransactionDataList().forEach(trx -> {
                trx.setCurrentAmount(trx.getCurrentAmount().multiply(BigDecimal.ONE.add(ANNUAL_GROWTH_RATE)));
                trx.setCurrentNav(trx.getCurrentNav().multiply(BigDecimal.ONE.add(ANNUAL_GROWTH_RATE)));
            });
        }
    }

    private void updateFolioData(FolioDataResponse currentFolio, FolioDataResponse sellOrdersFolio, int year) {

        Map<String, List<FolioTransactionData>> isisnToSellTrxnsMap = sellOrdersFolio.getFolioDataList().stream()
                .collect(Collectors.toMap(FundFolioData::getIsinCode, FundFolioData::getFolioTransactionDataList));

        for (int i = 0; i < currentFolio.getFolioDataList().size(); i++) {
            FundFolioData currentFund = currentFolio.getFolioDataList().get(i);
            List<FolioTransactionData> sellTrxns = isisnToSellTrxnsMap.get(currentFund.getIsinCode());
            Map<String, FolioTransactionData> sellTrxnsMap = sellTrxns.stream().collect(Collectors.toMap(FolioTransactionData::getTransactionNumber, Function.identity()));
           for (int j = 0; j < currentFolio.getFolioDataList().get(i).getFolioTransactionDataList().size(); j++) {
                FolioTransactionData trx = currentFund.getFolioTransactionDataList().get(j);
                FolioTransactionData sellTrxn = sellTrxnsMap.get(trx.getTransactionNumber());

                if (Objects.isNull(sellTrxn)) {
                    continue;
                }

               if (trx.getUnits().equals(sellTrxn.getUnits())) {
                   Date buyDate = trx.getInvestmentDate();
                   trx.setInvestmentDate(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * year)));

                   int yearsDiff = (int) ((trx.getInvestmentDate().getTime() - buyDate.getTime()) / (1000L * 60 * 60 * 24 * 365));

                   trx.setNav(trx.getNav().multiply(BigDecimal.ONE.add(ANNUAL_GROWTH_RATE).pow(yearsDiff)));
                   trx.setCurrentNav(trx.getNav());
                   trx.setCurrentAmount(trx.getUnits().multiply(trx.getCurrentNav()));
                   trx.setInvestedAmount(trx.getUnits().multiply(trx.getNav()));
                   trx.setTransactionNumber(UUID.randomUUID().toString());
               } else {
                   Date buyDate = trx.getInvestmentDate();
                   int yearsDiff = (int) ((trx.getInvestmentDate().getTime() - buyDate.getTime()) / (1000L * 60 * 60 * 24 * 365));

                   FolioTransactionData newTrxn = FolioTransactionData.builder()
                           .investmentDate(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * year)))
                           .investedAmount(sellTrxn.getAmountToSell())
                           .units(sellTrxn.getUnits())
                           .nav(trx.getNav().multiply(BigDecimal.ONE.add(ANNUAL_GROWTH_RATE).pow(yearsDiff)))
                           .currentNav(trx.getNav().multiply(BigDecimal.ONE.add(ANNUAL_GROWTH_RATE).pow(yearsDiff)))
                           .currentAmount(sellTrxn.getAmountToSell())
                            .transactionNumber(UUID.randomUUID().toString())
                           .build();

                   trx.setUnits(trx.getUnits().subtract(sellTrxn.getUnits()));
                   trx.setInvestedAmount(trx.getUnits().multiply(trx.getNav()));
                   trx.setCurrentAmount(trx.getUnits().multiply(trx.getCurrentNav()));

                   currentFund.getFolioTransactionDataList().add(newTrxn);
               }
           }
        }
    }

    private BigDecimal getTotalProfitAfterSellingAll(FolioDataResponse currentFolio) {
        List<FolioTransactionData> currentTrxns =
                currentFolio.getFolioDataList().stream().map(FundFolioData::getFolioTransactionDataList).flatMap(List::stream).collect(Collectors.toList());
        currentTrxns.sort(Comparator.comparing(FolioTransactionData::getInvestmentDate));

        BigDecimal totalProfit = BigDecimal.ZERO;
        for (int i = 0; i < currentTrxns.size(); i++) {
            FolioTransactionData trx = currentTrxns.get(i);
            BigDecimal profit = trx.getCurrentAmount().subtract(trx.getInvestedAmount());
            totalProfit = totalProfit.add(profit);
        }
        return totalProfit;
    }

}
