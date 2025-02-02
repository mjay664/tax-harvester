package in.clear.tax_harvester.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupedAccumulatedTransactionsDTO extends InvestmentTransactionBaseDTO {
    private Product product;
    private BigDecimal currentNAV = BigDecimal.ZERO;
    private BigDecimal currentUnits = BigDecimal.ZERO;
    private BigDecimal investedAmount = BigDecimal.ZERO;
    private BigDecimal purchaseNav = BigDecimal.ZERO;
    private BigDecimal purchaseUnits = BigDecimal.ZERO;
    private BigDecimal returnValue = BigDecimal.ZERO;
    private BigDecimal returnPercentage = BigDecimal.ZERO;
    private BigDecimal redeemedAmount = BigDecimal.ZERO;
    private BigDecimal redeemedUnits = BigDecimal.ZERO;
    private BigDecimal redeemableUnits = BigDecimal.ZERO;
    private BigDecimal redeemableAmount = BigDecimal.ZERO;
    private BigDecimal avgBuyingNAV = BigDecimal.ZERO;
    private BigDecimal dividendAmount = BigDecimal.ZERO;
    private List<AccumulatedMFTrxnDTO> folios;

    public GroupedAccumulatedTransactionsDTO(Product product, List<AccumulatedMFTrxnDTO> folios) {
        this.product = product;
        this.folios = folios;
    }

    public BigDecimal getInvestedAmount() {
        return investedAmount;
    }

    public Product getProduct() {
        return product;
    }

    public List<AccumulatedMFTrxnDTO> getFolios() {
        if (CollectionUtils.isEmpty(folios)) {
            folios = List.of();
        }

        return folios;
    }
}
