package ee.taltech.inbankbackend.config;

import lombok.experimental.UtilityClass;

/**
 * Holds all necessary constants for the decision engine.
 */
@UtilityClass
public class DecisionEngineConstants {
    public static final int MINIMUM_LOAN_AMOUNT = 2000;
    public static final int MAXIMUM_LOAN_AMOUNT = 10000;
    public static final int MAXIMUM_LOAN_PERIOD = 48;
    public static final int MINIMUM_LOAN_PERIOD = 12;
    public static final int SEGMENT_1_CREDIT_MODIFIER = 100;
    public static final int SEGMENT_2_CREDIT_MODIFIER = 300;
    public static final int SEGMENT_3_CREDIT_MODIFIER = 1000;
}
