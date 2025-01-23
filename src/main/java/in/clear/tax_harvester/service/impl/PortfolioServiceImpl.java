package in.clear.tax_harvester.service.impl;

import in.clear.tax_harvester.client.SaveFeignClient;
import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.FolioTransactionData;
import in.clear.tax_harvester.dto.FundFolioData;
import in.clear.tax_harvester.dto.MutualFundTransactionDTO;
import in.clear.tax_harvester.dto.Product;
import in.clear.tax_harvester.service.PortfolioService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final SaveFeignClient saveFeignClient;


    public PortfolioServiceImpl(SaveFeignClient saveFeignClient) {
        this.saveFeignClient = saveFeignClient;
    }

    @Override
    public FolioDataResponse getFolioData(String email) {
        var saveResponse = saveFeignClient.getInvestmentTransactionResponse(email);

        var transactions = saveResponse.getResponse().getTransactions();



        return null;
    }

    private List<FundFolioData> consolidateTransactions(List<MutualFundTransactionDTO> transactions) {
        if (CollectionUtils.isEmpty(transactions)) {
            return List.of();
        }

        Map<Product, List<MutualFundTransactionDTO>> transactionMap = transactions.stream()
                                                                                  .collect(Collectors.groupingBy(MutualFundTransactionDTO::getProduct));

        for(var entry: transactionMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }

            var fundProduct = entry.getKey().getReferProduct();

            var fundFolioData = new FundFolioData();

            var fundTransactions = entry.getValue().stream()
                                        .map(MutualFundTransactionDTO::toFolioTransactionData)
                                        .toList();

            fundFolioData.setFolioTransactionDataList(fundTransactions);
            fundFolioData.setFundName(fundProduct.getFundName());
            fundFolioData.setIsinCode(fundProduct.getIsinCode());
            fundFolioData.setInvestedAmount(fundTransactions.stream()
                                                .map(FolioTransactionData::getInvestedAmount)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add));

        }


        return null;
    }
}
