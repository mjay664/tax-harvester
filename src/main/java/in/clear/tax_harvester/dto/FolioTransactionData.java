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
    private Double profit;
    private Double amountToSell;
}
