package in.clear.tax_harvester.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupedAccumulatedTransactionsDTO extends InvestmentTransactionBaseDTO{
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
}
