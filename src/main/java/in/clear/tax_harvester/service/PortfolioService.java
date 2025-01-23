package in.clear.tax_harvester.service;

import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.OptimisationSuggestionResponse;

public interface PortfolioService {
    FolioDataResponse getFolioData(String email);

    OptimisationSuggestionResponse getOptimisationSuggestion(String email, String pan, int years);
}
