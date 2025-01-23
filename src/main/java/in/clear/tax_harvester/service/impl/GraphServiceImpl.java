package in.clear.tax_harvester.service.impl;

import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.FundFolioData;
import in.clear.tax_harvester.dto.GraphDataDTO;
import in.clear.tax_harvester.dto.GraphDataSetDTO;
import in.clear.tax_harvester.dto.GraphResponseDTO;
import in.clear.tax_harvester.service.GraphService;
import in.clear.tax_harvester.utils.FractionalOwnershipOptimisationStrategyUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

public class GraphServiceImpl implements GraphService {

    private static final BigDecimal ANNUAL_GROWTH_RATE = new BigDecimal("0.10");
    private static final BigDecimal EXEMPTION_LIMIT = new BigDecimal("125000");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    public GraphResponseDTO getGraphData(FolioDataResponse folioDataResponse, int years) {
        // Create the dataset for both strategies
        return GraphResponseDTO.builder()
                .dataSets(List.of(
                        generateDataSetForExistingStrategy(folioDataResponse, years),
                        generateDataSetForOurStrategy(folioDataResponse, years)
                ))
                .build();
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

    public GraphDataSetDTO generateDataSetForOurStrategy(FolioDataResponse currentFolio, int years) {
        List<GraphDataDTO> data = new ArrayList<>();

        for (int year = 0; year < years; year++) {
            FolioDataResponse sellOrdersFolio = FractionalOwnershipOptimisationStrategyUtil.getOptimisedStockSellingOrder(currentFolio);
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
            folioData.getFolioTransactionDataList().stream().peek(trx -> {
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
                           .investedAmount(BigDecimal.valueOf(sellTrxn.getAmountToSell()))
                           .units(sellTrxn.getUnits())
                           .nav(trx.getNav().multiply(BigDecimal.ONE.add(ANNUAL_GROWTH_RATE).pow(yearsDiff)))
                           .currentNav(trx.getNav().multiply(BigDecimal.ONE.add(ANNUAL_GROWTH_RATE).pow(yearsDiff)))
                           .currentAmount(BigDecimal.valueOf(sellTrxn.getAmountToSell()))
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


    public static void main(String[] args) {
        // Simulation parameters
        int simulationYears = 20;
        String dummyEmail = "test@example.com";
        String dummyPan = "ABCDE1234F";

        // Instantiate the Graph service implementation
        GraphServiceImpl graphService = new GraphServiceImpl();

        // Define some dummy transactions
        FolioDataResponse folioDataResponse = createDummyfolioDataResponse();

        // Now invoke the method under test
        GraphResponseDTO response = graphService.getGraphData(folioDataResponse, simulationYears);

        // Check the output
        for (GraphDataSetDTO dataSet : response.getDataSets()) {
            System.out.println("Strategy: " + dataSet.getLabel());
            for (GraphDataDTO data : dataSet.getData()) {
                System.out.println(data.getTime() + ": Tax Liability - " + data.getAmountOfTax());
            }
        }
    }

    private static FolioDataResponse createDummyfolioDataResponse() {
        List<FolioTransactionData> transactions = new ArrayList<>();
        List<FolioTransactionData> transactions2 = new ArrayList<>();

        // Let's create some dummy transactions with various NAV, units, and invested amounts
        transactions.add(createTransaction(new BigDecimal("500"), new BigDecimal("100000"), new BigDecimal("200"), new BigDecimal("100000"), new BigDecimal("200"), 0));
        transactions2.add(createTransaction(new BigDecimal("300"), new BigDecimal("150000"), new BigDecimal("500"), new BigDecimal("165000"), new BigDecimal("550"),1));
        transactions.add(createTransaction(new BigDecimal("200"), new BigDecimal("200000"), new BigDecimal("1000"), new BigDecimal("242000"), new BigDecimal("1210"),2));

        List<FundFolioData> folioDataList = new ArrayList<>();
        folioDataList.add(FundFolioData.builder()
                .fundName("Fund A")
                .isinCode("ISIN123")
                .units(new BigDecimal("1200"))
                .folioTransactionDataList(transactions)
                .fundType("ELSS")
                .build());

        folioDataList.add(FundFolioData.builder()
                .fundName("Fund B")
                .isinCode("ISIN126")
                .fundType("ELSS")
                .units(new BigDecimal("500"))
                .folioTransactionDataList(transactions2)
                .build());

        return FolioDataResponse.builder()
                .folioDataList(folioDataList)
                .build();
    }

    private static FolioTransactionData createTransaction(BigDecimal units, BigDecimal investedAmount,
                                                          BigDecimal nav, BigDecimal currentAmount, BigDecimal currentNav, int yearsAgo) {
        FolioTransactionData transaction = new FolioTransactionData();
        transaction.setInvestmentDate(Date.from(Instant.now().minusSeconds(365L * 24 * 60 * 60 * yearsAgo)));
        transaction.setInvestedAmount(investedAmount);
        transaction.setUnits(units);
        transaction.setNav(nav);
        transaction.setCurrentAmount(currentAmount);
        transaction.setCurrentNav(currentNav);
        return transaction;
    }


}
