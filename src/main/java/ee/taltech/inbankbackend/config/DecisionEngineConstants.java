package ee.taltech.inbankbackend.config;
import java.time.Period;
/**
 * Holds all necessary constants for the decision engine.
 */
public class DecisionEngineConstants {
    public static final Integer MINIMUM_LOAN_AMOUNT = 2000;
    public static final Integer MAXIMUM_LOAN_AMOUNT = 10000;
    public static final Integer MAXIMUM_LOAN_PERIOD = 60;
    public static final Integer MINIMUM_LOAN_PERIOD = 12;
    public static final Integer SEGMENT_1_CREDIT_MODIFIER = 100;
    public static final Integer SEGMENT_2_CREDIT_MODIFIER = 300;
    public static final Integer SEGMENT_3_CREDIT_MODIFIER = 1000;
    
    public static final Period EXPECTED_LIFETIME_MEN = Period.of(73, 7, 0);
    public static final Period EXPECTED_LIFETIME_WOMEN = Period.of(82, 4, 0);
    public static final Period ESTONIA_LEGAL_AGE = Period.of(18, 0, 0);

    public static final String UNDERAGED_CLIENT_MESSAGE = "The client is underaged";
    public static final String OVERAGED_CLIENT_MESSAGE = "The client exceeds the maximum allowed age for this loan";
}
