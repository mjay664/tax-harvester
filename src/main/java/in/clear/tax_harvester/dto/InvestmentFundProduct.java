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
public class InvestmentFundProduct {

    private Long id;

    private Date updated_at;

    private String fundName;

    private String issuingAuthority;

    private int year;

    private String schemeCode;

    private String alternateSchemeCode;

    private String amfiSchemeCode;

    private float currentNav;

    private String amcCode;

    private String billdeskAmcCode;

    private int lockinPeriod;

    private float annualInterestRate;

    private Boolean enabled;

    private boolean sipEnabled;

    private boolean lumpSumEnabled;

    private String imageUrl;

    private String fundHouse;

    public Date getUpdated_at() {
        return updated_at;
    }
}
