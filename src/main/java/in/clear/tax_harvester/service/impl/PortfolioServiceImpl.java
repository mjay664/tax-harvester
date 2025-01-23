package in.clear.tax_harvester.service.impl;

import in.clear.tax_harvester.client.SaveFeignClient;
import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.service.PortfolioService;
import in.clear.tax_harvester.service.TaxHarvesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final SaveFeignClient saveFeignClient;

    @Override
    public FolioDataResponse getFolioData(String email) {
        var saveResponse = saveFeignClient.getInvestmentTransactionResponse(email);


        return null;
    }
}
