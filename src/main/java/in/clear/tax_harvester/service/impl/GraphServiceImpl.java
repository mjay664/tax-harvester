package in.clear.tax_harvester.service.impl;

import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.GraphDataDTO;
import in.clear.tax_harvester.dto.GraphDataSetDTO;
import in.clear.tax_harvester.dto.GraphResponseDTO;
import in.clear.tax_harvester.service.GraphService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GraphServiceImpl implements GraphService {

    private static final BigDecimal ANNUAL_GROWTH_RATE = new BigDecimal("0.10");
    private static final BigDecimal EXEMPTION_LIMIT = new BigDecimal("125000");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");


    public GraphResponseDTO getGraphData(int years, String email, String pan) {
        List<FolioTransactionData> initialTransactions = getEligibleTransactions(years, email, pan);

        // Create the dataset for both strategies
        return GraphResponseDTO.builder()
                .dataSets(List.of(
                        generateDataSetForExistingStrategy(new ArrayList<>(initialTransactions), years),
                        generateDataSetForOurStrategy(new ArrayList<>(initialTransactions), years)
                ))
                .build();
    }

    public GraphResponseDTO getGraphData(List<FolioTransactionData> initialTransactions, int years) {
        // Create the dataset for both strategies
        return GraphResponseDTO.builder()
                .dataSets(List.of(
                        generateDataSetForExistingStrategy(new ArrayList<>(initialTransactions), years),
                        generateDataSetForOurStrategy(new ArrayList<>(initialTransactions), years)
                ))
                .build();
    }

    private List<FolioTransactionData> getEligibleTransactions(int time, String email, String pan) {
        // Mock implementation - ideally gets eligible transactions from the database or service

        return List.of(); // Populate with real data
    }

    private GraphDataSetDTO generateDataSetForExistingStrategy(List<FolioTransactionData> currentTrxns, int years) {
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

    public GraphDataSetDTO generateDataSetForOurStrategy(List<FolioTransactionData> currentTrxns, int years) {
        return null;
    }


    public GraphDataSetDTO generateDataSetForOurStrategy2(List<FolioTransactionData> currentTrxns, int years) {
        List<GraphDataDTO> data = new ArrayList<>();
        BigDecimal totalTaxSaved = BigDecimal.ZERO;
        BigDecimal totalProfitBooked = BigDecimal.ZERO;

        for (int year = 0; year < years; year++) {
            BigDecimal annualProfitBooked = BigDecimal.ZERO;
            BigDecimal annualTaxSaved = BigDecimal.ZERO;
            List<FolioTransactionData> newTransactions = new ArrayList<>();

            for (int i = 0; i < currentTrxns.size(); i++) {
                FolioTransactionData transaction = currentTrxns.get(i);
                BigDecimal initialNav = transaction.getNav();
                BigDecimal units = transaction.getUnits();
                BigDecimal currentNav = initialNav.multiply(BigDecimal.ONE.add(ANNUAL_GROWTH_RATE).pow(year + 1));
                BigDecimal currentValue = currentNav.multiply(units).setScale(2, RoundingMode.HALF_UP);
                BigDecimal potentialProfit = currentValue.subtract(transaction.getInvestedAmount());

                if (annualProfitBooked.compareTo(EXEMPTION_LIMIT) < 0 && potentialProfit.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal requiredProfit = EXEMPTION_LIMIT.subtract(annualProfitBooked);
                    BigDecimal unitsToSell = requiredProfit.divide(currentNav.subtract(initialNav), RoundingMode.HALF_UP).min(units);
                    BigDecimal profitFromUnits = unitsToSell.multiply(currentNav.subtract(initialNav));

                    // Update the annual profit booked and the tax liability
                    if (unitsToSell.compareTo(BigDecimal.ZERO) > 0) {
                        annualProfitBooked = annualProfitBooked.add(profitFromUnits);
                        BigDecimal taxLiability = profitFromUnits.multiply(TAX_RATE);
                        annualTaxSaved = annualTaxSaved.add(taxLiability);
                    }

                    // Determine the remaining units
                    BigDecimal remainingUnits = units.subtract(unitsToSell);

                    if (remainingUnits.compareTo(BigDecimal.ZERO) > 0) {
                        // Update the transaction
                        transaction.setUnits(remainingUnits);
                    } else {
                        // Completely sold, remove it from the list
                        currentTrxns.remove(i);
                        i--; // Adjust index since we remove an element
                    }

                    // Add new transaction for repurchased units only if we sold some units
                    if (unitsToSell.compareTo(BigDecimal.ZERO) > 0) {
                        FolioTransactionData newTransaction = new FolioTransactionData();
                        newTransaction.setInvestmentDate(new Date());
                        newTransaction.setInvestedAmount(unitsToSell.multiply(currentNav));
                        newTransaction.setUnits(unitsToSell);
                        newTransaction.setNav(currentNav);
                        newTransactions.add(newTransaction);
                    }
                }
            }

            currentTrxns.addAll(newTransactions);

            totalProfitBooked = totalProfitBooked.add(annualProfitBooked);
            totalTaxSaved = totalTaxSaved.add(annualTaxSaved);

            data.add(new GraphDataDTO("Year " + (year + 1), annualTaxSaved, annualProfitBooked));
        }

        return new GraphDataSetDTO(data,"Our Strategy (Tax Liabilities Saved)");
    }


    public static void main(String[] args) {
        // Simulation parameters
        int simulationYears = 10;
        String dummyEmail = "test@example.com";
        String dummyPan = "ABCDE1234F";

        // Instantiate the Graph service implementation
        GraphServiceImpl graphService = new GraphServiceImpl();

        // Define some dummy transactions
        List<FolioTransactionData> dummyTransactions = createDummyTransactions();

        // Now invoke the method under test
        GraphResponseDTO response = graphService.getGraphData(dummyTransactions, simulationYears);

        // Check the output
        for (GraphDataSetDTO dataSet : response.getDataSets()) {
            System.out.println("Strategy: " + dataSet.getLabel());
            for (GraphDataDTO data : dataSet.getData()) {
                System.out.println(data.getTime() + ": Tax Liability - " + data.getAmountOfTax());
            }
        }
    }

    private static List<FolioTransactionData> createDummyTransactions() {
        List<FolioTransactionData> transactions = new ArrayList<>();

        // Let's create some dummy transactions with various NAV, units, and invested amounts
        transactions.add(createTransaction(new BigDecimal("500"), new BigDecimal("100000"), new BigDecimal("200"), new BigDecimal("100000"), new BigDecimal("200"), 0));
        transactions.add(createTransaction(new BigDecimal("300"), new BigDecimal("150000"), new BigDecimal("500"), new BigDecimal("165000"), new BigDecimal("550"),1));
        transactions.add(createTransaction(new BigDecimal("200"), new BigDecimal("200000"), new BigDecimal("1000"), new BigDecimal("242000"), new BigDecimal("1210"),2));

        return transactions;
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
