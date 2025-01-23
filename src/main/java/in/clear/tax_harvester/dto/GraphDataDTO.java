package in.clear.tax_harvester.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphDataDTO {
    private String time;
    private BigDecimal amountOfTax;
    private BigDecimal profitBooked;
}
