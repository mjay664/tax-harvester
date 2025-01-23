package in.clear.tax_harvester.service;

import in.clear.tax_harvester.dto.FolioDataResponse;

public interface PortfolioService {
    FolioDataResponse getFolioData(String email);
}
