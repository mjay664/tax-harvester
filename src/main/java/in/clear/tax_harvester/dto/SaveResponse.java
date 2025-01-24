package in.clear.tax_harvester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveResponse {
    private InvestmentTransactionResponseV2DTO response;

    public FolioDataResponse toFolioDataResponse() {
        var transactions = response.getTransactions();
        var fundFolioDataList = consolidateTransactions(transactions);
        return new FolioDataResponse(fundFolioDataList);
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
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP));
        fundFolioData.setCurrentAmount(fundTransactions.stream()
                                                       .map(FolioTransactionData::getCurrentAmount)
                                                       .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP));
        fundFolioData.setUnits(fundTransactions.stream().map(FolioTransactionData::getUnits)
                                               .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP));
        fundFolioData.setFundType(fundProduct.getFundType());
        return fundFolioData;
    }


}
