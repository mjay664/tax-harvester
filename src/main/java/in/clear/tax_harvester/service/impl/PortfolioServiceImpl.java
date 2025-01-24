package in.clear.tax_harvester.service.impl;

import in.clear.tax_harvester.client.SaveFeignClient;
import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.ListGraphResponseDTO;
import in.clear.tax_harvester.dto.OptimisationSuggestionResponse;
import in.clear.tax_harvester.service.PortfolioService;
import in.clear.tax_harvester.utils.FractionalOwnershipOptimisationStrategyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


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
        ListGraphResponseDTO listGraphResponseDTO = graphService.getGraphData(saveResponse.toFolioDataResponse(), years);


        return OptimisationSuggestionResponse
                .builder()
                .folioDataResponse(FractionalOwnershipOptimisationStrategyUtil.getOptimisedStockSellingOrder(saveResponse.toFolioDataResponse()))
                .listGraphResponse(listGraphResponseDTO)
                .build();
    }


}
