package in.clear.tax_harvester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public abstract class InvestmentTransactionBaseDTO {
    private BigDecimal currentAmount = BigDecimal.ZERO;
    private BigDecimal xirr = BigDecimal.ZERO;
    private BigDecimal dividendAmount = BigDecimal.ZERO;

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }
}
