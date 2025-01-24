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

        return FractionalOwnershipOptimisationStrategyUtil.getOptimisedStockSellingOrder(saveResponse.toFolioDataResponse());
    }

    @Override
    public OptimisationSuggestionResponse getOptimisationSuggestion(String email, String pan, int years) {

        var saveResponse = saveFeignClient.getInvestmentTransactionResponse(email);
        GraphResponseDTO graphResponseDTO = graphService.getGraphData(saveResponse.toFolioDataResponse(), years);

        return OptimisationSuggestionResponse
                .builder()
                .folioDataResponse(FractionalOwnershipOptimisationStrategyUtil.getOptimisedStockSellingOrder(saveResponse.toFolioDataResponse()))
                .graphResponseDTO(graphResponseDTO)
                .build();
    }


}
