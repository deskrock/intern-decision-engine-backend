package ee.taltech.inbankbackend.config;

/**
 * Holds all necessary constants for the decision.
 */
public class Constants {
    public static final Long MINIMUM_LOAN_AMOUNT = 2000L;
    public static final Long MAXIMUM_LOAN_AMOUNT = 10000L;
    public static final Integer MAXIMUM_LOAN_PERIOD = 60;
    public static final Integer MINIMUM_LOAN_PERIOD = 12;
    public static final Long SEGMENT_1_CREDIT_MODIFIER = 100L;
    public static final Long SEGMENT_2_CREDIT_MODIFIER = 300L;
    public static final Long SEGMENT_3_CREDIT_MODIFIER = 1000L;
    public static final Integer MINIMUM_LOAN_AGE = 18;
    public static final Integer MAXIMUM_LOAN_AGE_MALE = 74;
    public static final Integer MAXIMUM_LOAN_AGE_FEMALE = 82;
}
