package com.smart.parking.payment;

import com.smart.parking.auth.Role;
import com.smart.parking.auth.User;
import com.smart.parking.parking.ParkingSpace;
import com.smart.parking.reservation.Reservation;
import com.smart.parking.reservation.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private RestTemplate restTemplate;
    @Mock private ReservationRepository reservationRepository;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Reservation testReservation;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test Driver")
                .email("driver@example.com")
                .password("hashed")
                .role(Role.DRIVER)
                .plateNumber("RAB123")
                .build();

        ParkingSpace parkingSpace = new ParkingSpace();
        parkingSpace.setId(1L);
        parkingSpace.setName("Downtown Parking");
        parkingSpace.setPricePerHour(BigDecimal.valueOf(5000));

        testReservation = Reservation.builder()
                .id(1L)
                .user(testUser)
                .parkingSpace(parkingSpace)
                .slotCount(2)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .paid(false)
                .build();

        // Set frontend URL
        ReflectionTestUtils.setField(paymentService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(paymentService, "flwSecretKey", "test-secret-key");
    }

    @Test
    void initiatePayment_ShouldReturnPaymentLink_WhenFlutterwaveRespondsSuccessfully() {
        // Arrange
        Map<String, Object> mockResponse = Map.of(
                "data", Map.of("link", "https://flutterwave.com/pay/test-link")
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                contains("payments"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String paymentLink = paymentService.initiatePayment(testReservation);

        // Assert
        assertNotNull(paymentLink);
        assertEquals("https://flutterwave.com/pay/test-link", paymentLink);
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void initiatePayment_ShouldThrowException_WhenFlutterwaveResponseIsEmpty() {
        // Arrange
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(Map.of(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.initiatePayment(testReservation);
        });

        assertEquals("Failed to generate payment link", exception.getMessage());
    }

    @Test
    void processWebhook_ShouldMarkReservationAsPaid_WhenStatusIsSuccessful() {
        // Arrange
        FlutterwaveEvent event = new FlutterwaveEvent();
        FlutterwaveEvent.EventData data = new FlutterwaveEvent.EventData();
        data.setStatus("successful");
        data.setTxRef("KP-1");
        data.setAmount(10000.0);
        data.setId(123L);
        event.setData(data);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // Act
        paymentService.processWebhook(event);

        // Assert
        assertTrue(testReservation.isPaid());
        verify(reservationRepository, times(1)).save(testReservation);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void processWebhook_ShouldSavePaymentRecord_WithCorrectDetails() {
        // Arrange
        FlutterwaveEvent event = new FlutterwaveEvent();
        FlutterwaveEvent.EventData data = new FlutterwaveEvent.EventData();
        data.setStatus("successful");
        data.setTxRef("KP-1");
        data.setAmount(10000.0);
        data.setId(456L);
        event.setData(data);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // Act
        paymentService.processWebhook(event);

        // Assert
        verify(paymentRepository, times(1)).save(argThat(payment ->
                payment.getAmount().equals(BigDecimal.valueOf(10000.0)) &&
                payment.getStatus().equals("SUCCESS") &&
                payment.getTransactionId().equals("456")
        ));
    }

    @Test
    void processWebhook_ShouldThrowException_WhenReservationNotFound() {
        // Arrange
        FlutterwaveEvent event = new FlutterwaveEvent();
        FlutterwaveEvent.EventData data = new FlutterwaveEvent.EventData();
        data.setStatus("successful");
        data.setTxRef("KP-999");
        event.setData(data);

        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processWebhook(event);
        });

        assertEquals("Reservation not found", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processWebhook_ShouldNotProcessPayment_WhenStatusIsNotSuccessful() {
        // Arrange
        FlutterwaveEvent event = new FlutterwaveEvent();
        FlutterwaveEvent.EventData data = new FlutterwaveEvent.EventData();
        data.setStatus("failed");
        data.setTxRef("KP-1");
        event.setData(data);

        // Act
        paymentService.processWebhook(event);

        // Assert
        assertFalse(testReservation.isPaid());
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
