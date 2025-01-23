package in.clear.tax_harvester.constant;

import java.util.Locale;
import java.util.Set;

public interface FundType {
    String EQUITY = "EQUITY";
    String ELSS = "ELSS";
    String DEBT = "DEBT";
    String HYBRID = "HYBRID";
    String METALS = "METALS";
    String GOLD = "GOLD";
    String INDEX = "INDEX";

    static boolean isEquityRelated(String fundType) {
        return Set.of(EQUITY, ELSS, INDEX).contains(fundType.toUpperCase(Locale.ROOT));
    }
}
