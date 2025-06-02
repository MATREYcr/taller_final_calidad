package edu.unac.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.unac.domain.Device;
import edu.unac.domain.DeviceStatus;
import edu.unac.domain.Loan;
import edu.unac.repository.DeviceRepository;
import edu.unac.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private LoanRepository loanRepository;

    @BeforeEach
    void setup() {
        loanRepository.deleteAll();
        deviceRepository.deleteAll();
    }

    @Test
    void testCreateDeviceValidData() throws Exception {
        Device newDevice = new Device();
        newDevice.setName("Smartphone");
        newDevice.setType("Electronics");
        newDevice.setLocation("Warehouse");

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDevice)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Smartphone")))
                .andExpect(jsonPath("$.status", is(DeviceStatus.AVAILABLE.toString())));
    }

    @Test
    void testCreateDeviceInvalidName() throws Exception {
        Device invalidDevice = new Device();
        invalidDevice.setName("AB");
        invalidDevice.setType("Electronics");
        invalidDevice.setLocation("Warehouse");

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDevice)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateDeviceNullName() throws Exception {
        Device invalidDevice = new Device();
        invalidDevice.setName(null);
        invalidDevice.setType("Electronics");
        invalidDevice.setLocation("Warehouse");

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDevice)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllDevicesEmptyList() throws Exception {
        mockMvc.perform(get("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAllDevicesWithData() throws Exception {
        Device device1 = new Device();
        device1.setName("Laptop");
        device1.setType("Electronics");
        device1.setLocation("Room 1");

        Device device2 = new Device();
        device2.setName("Projector");
        device2.setType("Office");
        device2.setLocation("Room 2");

        deviceRepository.saveAll(List.of(device1, device2));

        mockMvc.perform(get("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Laptop")))
                .andExpect(jsonPath("$[1].name", is("Projector")));
    }

    @Test
    void testGetDeviceByIdFound() throws Exception {
        Device device = new Device();
        device.setName("Tablet");
        device.setType("Electronics");
        device.setLocation("Shelf B");
        deviceRepository.save(device);

        mockMvc.perform(get("/api/devices/{id}", device.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(device.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Tablet")));
    }

    @Test
    void testGetDeviceByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/devices/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateDeviceStatusSuccess() throws Exception {
        Device device = new Device();
        device.setName("Smart TV");
        device.setType("Electronics");
        device.setLocation("Showroom");
        device.setStatus(DeviceStatus.AVAILABLE);
        deviceRepository.save(device);

        mockMvc.perform(put("/api/devices/{id}/status", device.getId())
                        .param("status", DeviceStatus.MAINTENANCE.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(device.getId().intValue())))
                .andExpect(jsonPath("$.status", is(DeviceStatus.MAINTENANCE.toString())));
    }

    @Test
    void testUpdateDeviceStatusNotFound() throws Exception {
        mockMvc.perform(put("/api/devices/{id}/status", 999L)
                        .param("status", DeviceStatus.MAINTENANCE.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDeviceNoLoanHistory() throws Exception {
        Device device = new Device();
        device.setName("Desktop");
        device.setType("IT");
        device.setLocation("Office");
        deviceRepository.save(device);

        mockMvc.perform(delete("/api/devices/{id}", device.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/devices/{id}", device.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDeviceWithLoanHistory() throws Exception {
        Device device = new Device();
        device.setName("Server");
        device.setType("IT");
        device.setLocation("Data Center");
        deviceRepository.save(device);

        Loan loan = new Loan();
        loan.setBorrowedBy("User A");
        loan.setDeviceId(device.getId());
        loan.setStartDate(System.currentTimeMillis());
        loan.setReturned(false);
        loanRepository.save(loan);

        mockMvc.perform(delete("/api/devices/{id}", device.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/devices/{id}", device.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Server")));
    }
}