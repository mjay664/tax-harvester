package in.clear.tax_harvester.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentTransactionResponseV2DTO extends InvestmentTransactionBaseDTO{
    private List<GroupedAccumulatedTransactionsDTO> investmentTransactions=new ArrayList<>();
    private BigDecimal investedAmount=BigDecimal.ZERO;
    private BigDecimal currentValue=BigDecimal.ZERO;
    private BigDecimal redeemedValue=BigDecimal.ZERO;
    private BigDecimal returnValue=BigDecimal.ZERO;
    private BigDecimal returnPercentage=BigDecimal.ZERO;
    private BigDecimal dividendAmount=BigDecimal.ZERO;
    private String navLastUpdatedAt=null;

    public InvestmentTransactionResponseV2DTO(List<GroupedAccumulatedTransactionsDTO> groupedAccumulatedTransactions){
        if(Objects.nonNull(groupedAccumulatedTransactions) && !CollectionUtils.isEmpty(groupedAccumulatedTransactions)) {
            investedAmount = Optional.of(groupedAccumulatedTransactions).orElseGet(Collections::emptyList)
                                     .stream().map(GroupedAccumulatedTransactionsDTO::getInvestedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            currentValue = Optional.of(groupedAccumulatedTransactions).orElseGet(Collections::emptyList)
                                   .stream().map(InvestmentTransactionBaseDTO::getCurrentAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            navLastUpdatedAt = groupedAccumulatedTransactions
                    .stream().map(t -> t.getProduct().getReferProduct()
                                        .getUpdatedAt())
                    .max(Date::compareTo).get().toString();

            setCurrentAmount(currentValue);
            this.investmentTransactions = groupedAccumulatedTransactions;
        }
    }

    public List<MutualFundTransactionDTO> getTransactions() {
        if (CollectionUtils.isEmpty(investmentTransactions)) {
            investmentTransactions = new ArrayList<>();
        }
        return getInvestmentTransactions().stream().map(GroupedAccumulatedTransactionsDTO::getFolios)
                .flatMap(List::stream)
                .map(AccumulatedMFTrxnDTO::getResultantPurchases)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
