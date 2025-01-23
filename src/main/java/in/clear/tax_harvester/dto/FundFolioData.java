package in.clear.tax_harvester.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import in.clear.tax_harvester.constant.FundType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FundFolioData {
    private String fundName;

    private String isinCode;

    private BigDecimal currentAmount;

    private BigDecimal investedAmount;

    private BigDecimal units;

    private String fundType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal nav;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal currentNav;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal suggestedRedemptionAmount;

    @JsonIgnore
    private List<FolioTransactionData> folioTransactionDataList;

    // Data after calculating optimisations
    private Double profit;
    private Double amountToSell;

    @JsonIgnore
    public boolean isELSS() {
        return FundType.ELSS.equalsIgnoreCase(fundType);
    }
}
