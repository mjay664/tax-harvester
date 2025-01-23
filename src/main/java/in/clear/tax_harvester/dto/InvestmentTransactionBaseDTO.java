package in.clear.tax_harvester.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class InvestmentTransactionBaseDTO {
    private BigDecimal currentAmount = BigDecimal.ZERO;
    private BigDecimal xirr = BigDecimal.ZERO;
    private BigDecimal dividendAmount = BigDecimal.ZERO;
}
