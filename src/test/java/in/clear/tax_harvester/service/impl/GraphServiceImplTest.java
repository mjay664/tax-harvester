package in.clear.tax_harvester.service.impl;

import in.clear.tax_harvester.constant.FundType;
import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.FundFolioData;
import in.clear.tax_harvester.dto.GraphDataDTO;
import in.clear.tax_harvester.dto.GraphDataSetDTO;
import in.clear.tax_harvester.dto.GraphResponseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

class GraphServiceImplTest {

    @org.junit.jupiter.api.Test
    void getGraphData() {
        int simulationYears = 20;

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
                .fundType(FundType.EQUITY)
                .build());

        folioDataList.add(FundFolioData.builder()
                .fundName("Fund B")
                .isinCode("ISIN126")
                .fundType(FundType.EQUITY)
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
        transaction.setTransactionNumber(UUID.randomUUID().toString());
        return transaction;
    }
}