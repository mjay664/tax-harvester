package in.clear.tax_harvester.service.impl;

import in.clear.tax_harvester.client.SaveFeignClient;
import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.FundFolioData;
import in.clear.tax_harvester.dto.GraphResponseDTO;
import in.clear.tax_harvester.dto.MutualFundTransactionDTO;
import in.clear.tax_harvester.dto.OptimisationSuggestionResponse;
import in.clear.tax_harvester.dto.Product;
import in.clear.tax_harvester.service.PortfolioService;
import in.clear.tax_harvester.utils.FractionalOwnershipOptimisationStrategyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final SaveFeignClient saveFeignClient;
    private final GraphServiceImpl graphService;

    @Override
    public FolioDataResponse getFolioData(String email) {
        var saveResponse = saveFeignClient.getInvestmentTransactionResponse(email);

        var transactions = saveResponse.getResponse().getTransactions();

        var fundFolioDataList = consolidateTransactions(transactions);

        var folioDataResponse = new FolioDataResponse(fundFolioDataList);

        return FractionalOwnershipOptimisationStrategyUtil.getOptimisedStockSellingOrder(folioDataResponse);
    }

    @Override
    public OptimisationSuggestionResponse getOptimisationSuggestion(String email, String pan, int years) {

        var saveResponse = saveFeignClient.getInvestmentTransactionResponse(email);
        var transactions = saveResponse.getResponse().getTransactions();
        var fundFolioDataList = consolidateTransactions(transactions);
        var folioDataResponse = new FolioDataResponse(fundFolioDataList);
        GraphResponseDTO graphResponseDTO = graphService.getGraphData(folioDataResponse, years);


        return OptimisationSuggestionResponse
                .builder()
                .folioDataResponse(folioDataResponse)
                .graphResponseDTO(graphResponseDTO)
                .build();
    }

    private List<FundFolioData> consolidateTransactions(List<MutualFundTransactionDTO> transactions) {
        if (CollectionUtils.isEmpty(transactions)) {
            return List.of();
        }

        Map<Product, List<MutualFundTransactionDTO>> transactionMap = transactions.stream()
                                                                                  .collect(Collectors.groupingBy(MutualFundTransactionDTO::getProduct));
        var fundFolioDataList = new ArrayList<FundFolioData>();
        for(var entry: transactionMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }

            fundFolioDataList.add(createFundFolioData(entry.getKey(), entry.getValue()));
        }


        return fundFolioDataList;
    }

    private static FundFolioData createFundFolioData(Product product, List<MutualFundTransactionDTO> mutualFundTransactionDTOS) {
        var fundProduct = product.getReferProduct();

        var fundFolioData = new FundFolioData();

        var fundTransactions = mutualFundTransactionDTOS.stream()
                                    .map(MutualFundTransactionDTO::toFolioTransactionData)
                                    .toList();

        fundFolioData.setFolioTransactionDataList(new ArrayList<>(fundTransactions));
        fundFolioData.setFundName(fundProduct.getFundName());
        fundFolioData.setIsinCode(fundProduct.getIsinCode());
        fundFolioData.setInvestedAmount(fundTransactions.stream()
                                            .map(FolioTransactionData::getInvestedAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
        fundFolioData.setCurrentAmount(fundTransactions.stream()
                                            .map(FolioTransactionData::getCurrentAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
        fundFolioData.setUnits(fundTransactions.stream().map(FolioTransactionData::getUnits)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
        fundFolioData.setFundType(fundProduct.getFundType());
        return fundFolioData;
    }
}
