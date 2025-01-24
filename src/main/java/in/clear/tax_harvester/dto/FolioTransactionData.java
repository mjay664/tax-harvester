package in.clear.tax_harvester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolioTransactionData {
    private Date investmentDate;

    private BigDecimal investedAmount;

    private BigDecimal units;

    private BigDecimal nav;

    private BigDecimal currentNav;

    private BigDecimal currentAmount;

    private String transactionNumber;

    // Data after calculating optimisations
    private BigDecimal profit;
    private BigDecimal amountToSell;

    public BigDecimal getCurrentAmount() {
        if (currentAmount == null) {
            currentAmount =  units.multiply(currentNav);
        }
        if (currentAmount == null) {
            currentAmount = BigDecimal.ZERO;
        }
        return currentAmount;
    }

    public BigDecimal getInvestedAmount() {
        if (investedAmount == null) {
            investedAmount = units.multiply(nav);
        }

        if (investedAmount == null) {
            investedAmount = BigDecimal.ZERO;
        }
        return investedAmount;
    }
}
