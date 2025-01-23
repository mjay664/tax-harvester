package in.clear.tax_harvester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundFolioData {
    private String fundName;

    private String isinCode;

    private BigDecimal currentAmount;

    private BigDecimal investedAmount;

    private BigDecimal units;

    private BigDecimal nav;

    private BigDecimal currentNav;

    private BigDecimal suggestedRedemptionAmount;

    private List<FolioTransactionData> folioTransactionDataList;

    private Double profit;
}
