package edu.unac.service;

import edu.unac.domain.Device;
import edu.unac.domain.DeviceStatus;
import edu.unac.domain.Loan;
import edu.unac.repository.DeviceRepository;
import edu.unac.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {

        private LoanRepository loanRepository;
        private DeviceRepository deviceRepository;
        private LoanService loanService;

        @BeforeEach
        void setUp() {
            loanRepository = mock(LoanRepository.class);
            deviceRepository = mock(DeviceRepository.class);
            loanService = new LoanService(loanRepository, deviceRepository);
        }


    @Test
    void registerLoan() {
        Device device = new Device();
        device.setId(1L);
        device.setStatus(DeviceStatus.AVAILABLE);

        Loan loan = new Loan();
        loan.setDeviceId(1L);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any())).thenReturn(device);
        loanService.registerLoan(loan);

        assertEquals(DeviceStatus.LOANED, device.getStatus());
    }

        @Test
        void deviceNotFound() {
            Loan loan = new Loan();
            loan.setDeviceId(111L);

            when(deviceRepository.findById(111L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> loanService.registerLoan(loan));
        }

        @Test
        void deviceNotAvailable() {
            Device device = new Device();
            device.setId(3504L);
            device.setStatus(DeviceStatus.LOANED);

            when(deviceRepository.findById(3504L)).thenReturn(Optional.of(device));

            Loan loan = new Loan();
            loan.setDeviceId(3504L);

            assertThrows(IllegalStateException.class, () -> loanService.registerLoan(loan));
        }

        @Test
        void markAsReturned() {
            Loan loan = new Loan();
            loan.setId(4L);
            loan.setReturned(false);

            Device device = new Device();
            device.setId(3030L);
            device.setStatus(DeviceStatus.LOANED);
            loan.setDeviceId(3030L);

            when(loanRepository.findById(4L)).thenReturn(Optional.of(loan));
            when(deviceRepository.findById(3030L)).thenReturn(Optional.of(device));
            loanService.markAsReturned(4L);

            assertTrue(loan.isReturned());
            assertEquals(DeviceStatus.AVAILABLE, device.getStatus());
        }

        @Test
        void markAsReturned_loanNotFound() {
            when(loanRepository.findById(22L)).thenReturn(Optional.empty());
            IllegalArgumentException u= assertThrows(IllegalArgumentException.class, () -> loanService.markAsReturned(22L));
            assertEquals("Loan not found", u.getMessage());

        }
        @Test
        void markAsReturned_deviceNotFound() {
            Loan loan = new Loan();
            loan.setId(100L);

            when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));
            when(deviceRepository.findById(1L)).thenReturn(Optional.empty());
            IllegalArgumentException u= assertThrows(IllegalArgumentException.class, () -> loanService.markAsReturned(100L));
            assertEquals("Device not found", u.getMessage());
        }

        @Test
        void markAsReturned_loanAlreadyReturned() {
            Loan loan = new Loan();
            loan.setId(1L);
            loan.setReturned(true);

            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            assertThrows(IllegalStateException.class, () -> loanService.markAsReturned(1L));

        }

        @Test
        void getAllLoans_returnsList() {
            List<Loan> list = Arrays.asList(new Loan(), new Loan());
            when(loanRepository.findAll()).thenReturn(list);
            assertEquals(2, loanService.getAllLoans().size());
        }

        @Test
        void getLoanById() {
            Loan loan = new Loan();
            loan.setId(5L);
            when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));
            Optional<Loan> result = loanService.getLoanById(5L);
            assertTrue(result.isPresent());
        }

        @Test
        void getLoansByDeviceId() {
            Loan loan = new Loan();
            Loan loan2 = new Loan();
            List<Loan> loans = List.of(loan, loan2);
            when(loanRepository.findByDeviceId(1211L)).thenReturn(loans);
            assertEquals(2, loanService.getLoansByDeviceId(1211L).size());
        }
    }

