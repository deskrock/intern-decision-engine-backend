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
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
public class DecisionEngineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DecisionEngine decisionEngine;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    /**
     * This method tests the /loan/decision endpoint with valid inputs.
     */
    @Test
    public void givenValidRequest_whenRequestDecision_thenReturnsExpectedResponse()
            throws Exception, InvalidLoanPeriodException, NoValidLoanException, InvalidPersonalCodeException,
            InvalidLoanAmountException {
        Decision decision = new Decision(1000, 12, null);
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt())).thenReturn(decision);

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").value(1000))
                .andExpect(jsonPath("$.loanPeriod").value(12))
                .andExpect(jsonPath("$.errorMessage").isEmpty())
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == 1000;
        assert response.getLoanPeriod() == 12;
        assert response.getErrorMessage() == null;
    }

    /**
     * This test ensures that if an invalid personal code is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidPersonalCode_whenRequestDecision_thenReturnsBadRequest()
            throws Exception, InvalidLoanPeriodException, NoValidLoanException, InvalidPersonalCodeException,
            InvalidLoanAmountException {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt()))
                .thenThrow(new InvalidPersonalCodeException("Invalid personal code"));

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("Invalid personal code"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals("Invalid personal code");
    }

    /**
     * This test ensures that if an invalid loan amount is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidLoanAmount_whenRequestDecision_thenReturnsBadRequest()
            throws Exception, InvalidLoanPeriodException, NoValidLoanException, InvalidPersonalCodeException,
            InvalidLoanAmountException {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt()))
                .thenThrow(new InvalidLoanAmountException("Invalid loan amount"));

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan amount"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals("Invalid loan amount");
    }

    /**
     * This test ensures that if an invalid loan period is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidLoanPeriod_whenRequestDecision_thenReturnsBadRequest()
            throws Exception, InvalidLoanPeriodException, NoValidLoanException, InvalidPersonalCodeException,
            InvalidLoanAmountException {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt()))
                .thenThrow(new InvalidLoanPeriodException("Invalid loan period"));

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan period"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals("Invalid loan period");
    }

    /**
     * This test ensures that if no valid loan is found, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenNoValidLoan_whenRequestDecision_thenReturnsBadRequest()
            throws Exception, InvalidLoanPeriodException, NoValidLoanException, InvalidPersonalCodeException,
            InvalidLoanAmountException {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt()))
                .thenThrow(new NoValidLoanException("No valid loan available"));

        DecisionRequest request = new DecisionRequest("1234", 1000L, 12);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("No valid loan available"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals("No valid loan available");
    }

    /**
     * This test ensures that if an unexpected error occurs when processing the request, the controller returns
     * an HTTP Internal Server Error (500) response with the appropriate error message in the response body.
     */
    @Test
    public void givenUnexpectedError_whenRequestDecision_thenReturnsInternalServerError()
            throws Exception, InvalidLoanPeriodException, NoValidLoanException, InvalidPersonalCodeException,
            InvalidLoanAmountException {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt())).thenThrow(new RuntimeException());

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("An unexpected error occurred"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals("An unexpected error occurred");
    }
}
