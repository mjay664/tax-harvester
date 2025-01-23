package in.clear.tax_harvester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccumulatedMFTrxnDTO extends InvestmentTransactionBaseDTO{

    private Product product;

    private BigDecimal purchaseNav;

    private BigDecimal purchaseUnits;

    private BigDecimal purchaseAmount;

    private BigDecimal redeemableUnits;

    private BigDecimal redeemableAmount;

    private BigDecimal redeemedAmount;

    private BigDecimal redeemedUnits;

    private BigDecimal redeemedNav;

    private BigDecimal currentNav;

    private BigDecimal currentUnits;

    private BigDecimal investedAmount;

    private BigDecimal returnValue;

    private BigDecimal returnPercentage;

    private BigDecimal avgBuyingNAV;

    private Boolean isCumulative;

    private String txnDate;

    private String id;

    private BigDecimal costValue;

    private String comment;

    private Date firstInvestmentDate;

    private BigDecimal lockedUnits;

    private BigDecimal lockedAmount;

    private BigDecimal ongoingOrderUnits;

    private BigDecimal ongoingOrderAmount;

    private String navDate;

    private Date currentNavDate;

    List<MutualFundTransactionDTO> resultantPurchases;

    private Boolean isImported;
}
