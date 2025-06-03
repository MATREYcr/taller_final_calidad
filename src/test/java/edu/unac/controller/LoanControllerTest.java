package edu.unac.controller;

import edu.unac.domain.Device;
import edu.unac.domain.Loan;
import edu.unac.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.unac.domain.DeviceStatus;
import edu.unac.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        loanRepository.deleteAll();
        deviceRepository.deleteAll();
    }
    @Test
    void testRegisterLoan() throws Exception {
        Device device = new Device();
        device.setName("Laptop");
        device.setStatus(DeviceStatus.AVAILABLE);
        device.setAddedDate(System.currentTimeMillis());
        device = deviceRepository.save(device);

        Loan loan = new Loan();
        loan.setDeviceId(device.getId());

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loan)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId", is(device.getId().intValue())))
                .andExpect(jsonPath("$.returned", is(false)));
    }

    @Test
    void testGetAllLoans() throws Exception {
        Device device = new Device();
        device.setName("Tablet");
        device.setStatus(DeviceStatus.AVAILABLE);
        device.setAddedDate(System.currentTimeMillis());
        device = deviceRepository.save(device);

        Loan loan1 = new Loan();
        loan1.setDeviceId(device.getId());
        loan1.setStartDate(System.currentTimeMillis());
        loan1.setReturned(false);
        loanRepository.save(loan1);

        Loan loan2 = new Loan();
        loan2.setDeviceId(device.getId());
        loan2.setStartDate(System.currentTimeMillis());
        loan2.setReturned(false);
        loanRepository.save(loan2);

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetLoanById() throws Exception {
        Device device = new Device();
        device.setName("Monitor");
        device.setStatus(DeviceStatus.AVAILABLE);
        device.setAddedDate(System.currentTimeMillis());
        device = deviceRepository.save(device);

        Loan loan = new Loan();
        loan.setDeviceId(device.getId());
        loan.setStartDate(System.currentTimeMillis());
        loan.setReturned(false);
        loan = loanRepository.save(loan);

        mockMvc.perform(get("/api/loans/" + loan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId", is(device.getId().intValue())));
    }

    @Test
    void testMarkAsReturned() throws Exception {
        Device device = new Device();
        device.setName("Projector");
        device.setStatus(DeviceStatus.LOANED);
        device.setAddedDate(System.currentTimeMillis());
        device = deviceRepository.save(device);

        Loan loan = new Loan();
        loan.setDeviceId(device.getId());
        loan.setStartDate(System.currentTimeMillis());
        loan.setReturned(false);
        loan = loanRepository.save(loan);

        mockMvc.perform(put("/api/loans/" + loan.getId() + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returned", is(true)));
    }

    @Test
    void testGetLoansByDeviceId() throws Exception {
        Device device = new Device();
        device.setName("Tablet E");
        device.setStatus(DeviceStatus.AVAILABLE);
        device.setAddedDate(System.currentTimeMillis());
        device = deviceRepository.save(device);

        for (int i = 0; i < 2; i++) {
            Loan loan = new Loan();
            loan.setDeviceId(device.getId());
            loan.setStartDate(System.currentTimeMillis());
            loan.setReturned(false);
            loanRepository.save(loan);
        }

        mockMvc.perform(get("/api/loans/device/" + device.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testRegisterLoanWithUnavailableDevice() throws Exception {
        Device device = new Device();
        device.setName("Laptop");
        device.setStatus(DeviceStatus.LOANED);
        device.setAddedDate(System.currentTimeMillis());
        device = deviceRepository.save(device);

        Loan loan = new Loan();
        loan.setDeviceId(device.getId());

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loan)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMarkAsReturned_AlreadyReturned() throws Exception {
        Device device = new Device();
        device.setName("Monitor");
        device.setStatus(DeviceStatus.LOANED);
        device.setAddedDate(System.currentTimeMillis());
        device = deviceRepository.save(device);

        Loan loan = new Loan();
        loan.setDeviceId(device.getId());
        loan.setStartDate(System.currentTimeMillis());
        loan.setEndDate(System.currentTimeMillis());
        loan.setReturned(true);
        loan = loanRepository.save(loan);

        mockMvc.perform(put("/api/loans/" + loan.getId() + "/return"))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetLoan_NotFound() throws Exception {
        mockMvc.perform(get("/api/loans/9999"))
                .andExpect(status().isNotFound());
    }
    @Test
    void markAsReturned_NotFound() throws Exception {

        mockMvc.perform(put("/api/loans/{id}/return", 9999L))
                .andExpect(status().isNotFound());
    }

}