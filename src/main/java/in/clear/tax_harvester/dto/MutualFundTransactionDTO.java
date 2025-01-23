package in.clear.tax_harvester.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MutualFundTransactionDTO extends InvestmentTransactionBaseDTO{

    private String transactionNumber;

    private String transactionType;

    private String transactionMode;

    private BigDecimal nav;

    private BigDecimal units;

    private BigDecimal amount;

    private BigDecimal loadAmount;

    private Date transactionDate;

    private Date navDate;

    private Date processDate;

    private String transactionDescription;

    private String fundName;
    
    private BigDecimal currentNav;

    private boolean isSipTrxn;

    private String category;

    private String subCategory;

    private String imageUrl;

    private Product product;

    private Date maturityDate;

    private String orderItemExternalId;

    @JsonIgnore
    private String formattedNavDate;

    @JsonIgnore
    private String formattedMaturityDate;

    @JsonIgnore
    private BigDecimal currentAmount;

    private BigDecimal availableUnits;

    @Override
    public BigDecimal getCurrentAmount() {
        return currentNav.multiply(units);
    }

    public FolioTransactionData toFolioTransactionData() {
        return FolioTransactionData.builder()
                .transactionNumber(transactionNumber)
                .nav(nav)
                .units(units)
                .investedAmount(Optional.ofNullable(amount).orElse(BigDecimal.ZERO))
                .investmentDate(transactionDate)
                .currentNav(currentNav)
                .currentAmount(getCurrentAmount())
                .build();
    }

}