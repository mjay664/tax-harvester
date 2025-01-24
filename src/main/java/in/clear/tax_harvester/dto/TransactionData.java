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
public class TransactionData {
    private BigDecimal ltcgPerUnit;
    private BigDecimal units;
    private BigDecimal currentNav;
    private String transactionNumber;
    private Date investmentDate;
}
