package ee.taltech.inbankbackend.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import ee.taltech.inbankbackend.service.Decision;
import ee.taltech.inbankbackend.service.DecisionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class holds integration tests for the DecisionEngineController endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class DecisionEngineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DecisionEngine decisionEngine;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
    }

    /**
     * Tests the /loan/decision endpoint with valid inputs for segment 3.
     */
    @Test
    void givenValidSegment3Request_whenRequestDecision_thenReturnsApprovedLoan() throws Exception {
        Decision decision = new Decision(10000, 12);
        when(decisionEngine.calculateApprovedLoan("49002010998", 4000L, 12)).thenReturn(decision);

        DecisionRequest request = new DecisionRequest("49002010998", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").value(10000))
                .andExpect(jsonPath("$.loanPeriod").value(12))
                .andExpect(jsonPath("$.errorMessage").isEmpty());
    }

    /**
     * Tests that an invalid personal code returns HTTP 400 with an error message.
     */
    @Test
    void givenInvalidPersonalCode_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan("12345678901", 4000L, 12))
                .thenThrow(new InvalidPersonalCodeException("Invalid personal ID code!"));

        DecisionRequest request = new DecisionRequest("12345678901", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("Invalid personal ID code!"));
    }

    /**
     * Tests that an invalid loan amount returns HTTP 400 with an error message.
     */
    @Test
    void givenInvalidLoanAmount_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010998", 1000L, 12))
                .thenThrow(new InvalidLoanAmountException("Invalid loan amount!"));

        DecisionRequest request = new DecisionRequest("49002010998", 1000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan amount!"));
    }

    /**
     * Tests that an invalid loan period returns HTTP 400 with an error message.
     */
    @Test
    void givenInvalidLoanPeriod_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010998", 4000L, 10))
                .thenThrow(new InvalidLoanPeriodException("Invalid loan period!"));

        DecisionRequest request = new DecisionRequest("49002010998", 4000L, 10);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan period!"));
    }

    /**
     * Tests that no valid loan found returns HTTP 404 with an error message.
     */
    @Test
    void givenNoValidLoan_whenRequestDecision_thenReturnsNotFound() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010965", 4000L, 12))
                .thenThrow(new NoValidLoanException("No valid loan found due to debt!"));

        DecisionRequest request = new DecisionRequest("49002010965", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("No valid loan found due to debt!"));
    }

    /**
     * Tests that an unexpected error returns HTTP 500 with an error message.
     */
    @Test
    void givenUnexpectedError_whenRequestDecision_thenReturnsInternalServerError() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010998", 4000L, 12))
                .thenThrow(new RuntimeException("Unexpected error"));

        DecisionRequest request = new DecisionRequest("49002010998", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("An unexpected error occurred"));
    }
}