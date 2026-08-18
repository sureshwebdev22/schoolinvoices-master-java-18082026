package com.kvn.schoolinvoices.service;

import com.kvn.schoolinvoices.dto.PaymentRequest;
import com.kvn.schoolinvoices.entity.Invoice;
import com.kvn.schoolinvoices.entity.InvoiceStatus;
import com.kvn.schoolinvoices.entity.Payment;
import com.kvn.schoolinvoices.service.repository.InvoiceRepository;
import com.kvn.schoolinvoices.service.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void savePayment_fullPayment_setsInvoiceStatusToPaid() {
        // Arrange
        Long invoiceId = 1L;
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(invoiceId);
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(new BigDecimal("100.00"));
        invoice.setStatus(InvoiceStatus.UNPAID);

        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentDate(LocalDate.now());
        request.setPaymentMode("CREDIT_CARD");
        request.setTransactionReference("TXN123456");
        request.setRemarks("Full payment");

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPaymentId(1L);
            return payment;
        });

        // Act
        Payment savedPayment = paymentService.savePayment(request);

        // Assert
        assertNotNull(savedPayment);
        assertEquals(new BigDecimal("100.00"), savedPayment.getAmount());
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(0, invoice.getBalanceAmount().compareTo(BigDecimal.ZERO));
        assertEquals(new BigDecimal("100.00"), invoice.getPaidAmount());

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void savePayment_partialPayment_setsInvoiceStatusToPartiallPaid() {
        // Arrange
        Long invoiceId = 2L;
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(invoiceId);
        invoice.setTotalAmount(new BigDecimal("500.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(new BigDecimal("500.00"));
        invoice.setStatus(InvoiceStatus.UNPAID);

        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(new BigDecimal("200.00"));
        request.setPaymentDate(LocalDate.now());
        request.setPaymentMode("BANK_TRANSFER");
        request.setTransactionReference("TXN234567");
        request.setRemarks("Partial payment");

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPaymentId(1L);
            return payment;
        });

        // Act
        Payment savedPayment = paymentService.savePayment(request);

        // Assert
        assertNotNull(savedPayment);
        assertEquals(new BigDecimal("200.00"), savedPayment.getAmount());
        assertEquals(InvoiceStatus.PARTIALLY_PAID, invoice.getStatus());
        assertEquals(0, invoice.getBalanceAmount().compareTo(new BigDecimal("300.00")));
        assertEquals(0, invoice.getPaidAmount().compareTo(new BigDecimal("200.00")));

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void savePayment_multiplePartialPayments_accumulatesAndUpdatesStatus() {
        // Arrange
        Long invoiceId = 3L;
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(invoiceId);
        invoice.setTotalAmount(new BigDecimal("300.00"));
        invoice.setPaidAmount(new BigDecimal("100.00"));
        invoice.setBalanceAmount(new BigDecimal("200.00"));
        invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);

        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(new BigDecimal("200.00"));
        request.setPaymentDate(LocalDate.now());
        request.setPaymentMode("CHEQUE");
        request.setTransactionReference("CHQ987654");

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPaymentId(2L);
            return payment;
        });

        // Act
        Payment savedPayment = paymentService.savePayment(request);

        // Assert
        assertNotNull(savedPayment);
        assertEquals(new BigDecimal("200.00"), savedPayment.getAmount());
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(0, invoice.getBalanceAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, invoice.getPaidAmount().compareTo(new BigDecimal("300.00")));

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void savePayment_invoiceNotFound_throwsRuntimeException() {
        // Arrange
        Long invoiceId = 999L;
        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(new BigDecimal("100.00"));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.savePayment(request));

        assertEquals("Invoice not found", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void savePayment_amountExceedsBalance_throwsRuntimeException() {
        // Arrange
        Long invoiceId = 4L;
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(invoiceId);
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(new BigDecimal("100.00"));

        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(new BigDecimal("150.00"));
        request.setPaymentDate(LocalDate.now());
        request.setPaymentMode("CREDIT_CARD");

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.savePayment(request));

        assertEquals("Payment amount exceeds balance amount", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void savePayment_paymentDetailsArePersisted() {
        // Arrange
        Long invoiceId = 5L;
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(invoiceId);
        invoice.setTotalAmount(new BigDecimal("250.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(new BigDecimal("250.00"));

        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(new BigDecimal("250.00"));
        request.setPaymentDate(LocalDate.of(2026, 7, 22));
        request.setPaymentMode("ONLINE");
        request.setTransactionReference("ONLINE2026072200001");
        request.setRemarks("Online payment for tuition");

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPaymentId(1L);
            return payment;
        });

        // Act
        Payment savedPayment = paymentService.savePayment(request);

        // Assert
        assertEquals(new BigDecimal("250.00"), savedPayment.getAmount());
        assertEquals(LocalDate.of(2026, 7, 22), savedPayment.getPaymentDate());
        assertEquals("ONLINE", savedPayment.getPaymentMode());
        assertEquals("ONLINE2026072200001", savedPayment.getTransactionReference());
        assertEquals("Online payment for tuition", savedPayment.getRemarks());
        assertEquals(invoice, savedPayment.getInvoice());

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void getPaymentHistory_returnsPaymentList() {
        // Arrange
        Long invoiceId = 6L;
        Payment payment1 = new Payment();
        payment1.setPaymentId(1L);
        payment1.setAmount(new BigDecimal("100.00"));
        payment1.setPaymentDate(LocalDate.of(2026, 7, 20));

        Payment payment2 = new Payment();
        payment2.setPaymentId(2L);
        payment2.setAmount(new BigDecimal("50.00"));
        payment2.setPaymentDate(LocalDate.of(2026, 7, 21));

        List<Payment> paymentList = List.of(payment2, payment1);

        when(paymentRepository.findByInvoiceInvoiceIdOrderByPaymentDateDesc(invoiceId))
                .thenReturn(paymentList);

        // Act
        List<Payment> result = paymentService.getPaymentHistory(invoiceId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(payment2, result.get(0));
        assertEquals(payment1, result.get(1));

        verify(paymentRepository, times(1))
                .findByInvoiceInvoiceIdOrderByPaymentDateDesc(invoiceId);
    }

    @Test
    void getPaymentHistory_returnsEmptyList_whenNoPaymentsExist() {
        // Arrange
        Long invoiceId = 7L;

        when(paymentRepository.findByInvoiceInvoiceIdOrderByPaymentDateDesc(invoiceId))
                .thenReturn(List.of());

        // Act
        List<Payment> result = paymentService.getPaymentHistory(invoiceId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(paymentRepository, times(1))
                .findByInvoiceInvoiceIdOrderByPaymentDateDesc(invoiceId);
    }

    @Test
    void getPaymentHistory_returnsSinglePayment() {
        // Arrange
        Long invoiceId = 8L;
        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setAmount(new BigDecimal("500.00"));
        payment.setPaymentDate(LocalDate.now());

        when(paymentRepository.findByInvoiceInvoiceIdOrderByPaymentDateDesc(invoiceId))
                .thenReturn(List.of(payment));

        // Act
        List<Payment> result = paymentService.getPaymentHistory(invoiceId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(payment, result.get(0));

        verify(paymentRepository, times(1))
                .findByInvoiceInvoiceIdOrderByPaymentDateDesc(invoiceId);
    }

}
