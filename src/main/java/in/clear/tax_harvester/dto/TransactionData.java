package in.clear.tax_harvester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionData {
    private double ltcgPerUnit;
    private double units;
    private double currentNav;
    private String transactionNumber;
    private Date investmentDate;
}
