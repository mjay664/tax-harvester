package in.clear.tax_harvester.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class InvestmentTransactionBaseDTO {
    private BigDecimal currentAmount = BigDecimal.ZERO;
    private BigDecimal xirr = BigDecimal.ZERO;
    private BigDecimal dividendAmount = BigDecimal.ZERO;
}
