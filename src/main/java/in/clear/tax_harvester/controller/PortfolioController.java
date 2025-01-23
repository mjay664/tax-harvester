package in.clear.tax_harvester.controller;


import in.clear.tax_harvester.dto.FolioDataResponse;
import in.clear.tax_harvester.dto.OptimisationSuggestionResponse;
import in.clear.tax_harvester.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tax-harvester")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/{email}/folio")
    public FolioDataResponse getFolio(@PathVariable("email") String email) {
        return portfolioService.getFolioData(email);
    }

    @GetMapping("/{email}/folio/optimisation")
    public OptimisationSuggestionResponse getOptimisationSuggestion(@PathVariable("email") String email,
                                                                    @RequestParam("pan") String pan,
                                                                    @RequestParam("pan") int years) {
        return portfolioService.getOptimisationSuggestion(email, pan, years);
    }
}
