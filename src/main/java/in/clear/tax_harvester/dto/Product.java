package in.clear.tax_harvester.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private Long id;

    private InvestmentFundProduct referProduct;

    public InvestmentFundProduct getReferProduct() {
        return referProduct;
    }
}
