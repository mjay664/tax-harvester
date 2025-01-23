package in.clear.tax_harvester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimisationSuggestionResponse {
    private GraphResponseDTO graphResponseDTO;
    private FolioDataResponse folioDataResponse;
}
