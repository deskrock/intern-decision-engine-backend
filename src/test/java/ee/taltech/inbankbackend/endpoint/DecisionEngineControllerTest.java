package ee.taltech.inbankbackend.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.inbankbackend.exceptions.*;
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

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class, MockitoExtension.class})
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

    @Test
    void givenSegment3Request_whenRequestDecision_thenReturnsMaxLoan() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010998", 4000L, 12))
                .thenReturn(new Decision(10000, 12));

        DecisionRequest request = new DecisionRequest("49002010998", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").value(10000))
                .andExpect(jsonPath("$.loanPeriod").value(12));
    }

    @Test
    void givenSegment2Request_whenRequestDecision_thenReturnsApprovedLoan() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010987", 4000L, 12))
                .thenReturn(new Decision(3600, 12));

        DecisionRequest request = new DecisionRequest("49002010987", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanAmount").value(3600))
                .andExpect(jsonPath("$.loanPeriod").value(12));
    }

    @Test
    void givenSegment1Request_whenRequestDecision_thenReturnsAdjustedPeriod() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010976", 4000L, 12))
                .thenReturn(new Decision(4000, 40));

        DecisionRequest request = new DecisionRequest("49002010976", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanAmount").value(4000))
                .andExpect(jsonPath("$.loanPeriod").value(40));
    }

    @Test
    void givenInvalidPersonalCode_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan("12345678901", 4000L, 12))
                .thenThrow(new InvalidPersonalCodeException("Invalid personal ID code!"));

        DecisionRequest request = new DecisionRequest("12345678901", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Invalid personal ID code!"));
    }

    @Test
    void givenInvalidLoanAmount_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010998", 1000L, 12))
                .thenThrow(new InvalidLoanAmountException("Invalid loan amount!"));

        DecisionRequest request = new DecisionRequest("49002010998", 1000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan amount!"));
    }

    @Test
    void givenInvalidLoanPeriod_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010998", 4000L, 10))
                .thenThrow(new InvalidLoanPeriodException("Invalid loan period!"));

        DecisionRequest request = new DecisionRequest("49002010998", 4000L, 10);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan period!"));
    }

    @Test
    void givenDebtor_whenRequestDecision_thenReturnsNotFound() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010965", 4000L, 12))
                .thenThrow(new NoValidLoanException("No valid loan found due to debt!"));

        DecisionRequest request = new DecisionRequest("49002010965", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("No valid loan found due to debt!"));
    }

    @Test
    void givenUnexpectedError_whenRequestDecision_thenReturnsInternalServerError() throws Exception {
        when(decisionEngine.calculateApprovedLoan("49002010998", 4000L, 12))
                .thenThrow(new RuntimeException("Unexpected error"));

        DecisionRequest request = new DecisionRequest("49002010998", 4000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessage").value("An unexpected error occurred"));
    }
}