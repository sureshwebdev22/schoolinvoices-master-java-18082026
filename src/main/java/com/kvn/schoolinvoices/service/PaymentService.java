package com.kvn.schoolinvoices.service;


import com.kvn.schoolinvoices.AppUser;
import com.kvn.schoolinvoices.UserRepository;
import com.kvn.schoolinvoices.dto.PaymentDto;
import com.kvn.schoolinvoices.dto.PaymentRequest;
import com.kvn.schoolinvoices.dto.StudentDTO;
import com.kvn.schoolinvoices.entity.Invoice;
import com.kvn.schoolinvoices.entity.InvoiceStatus;
import com.kvn.schoolinvoices.entity.Payment;
import com.kvn.schoolinvoices.entity.Student;
import com.kvn.schoolinvoices.exception.ResourceNotFoundException;
import com.kvn.schoolinvoices.service.repository.InvoiceRepository;
import com.kvn.schoolinvoices.service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository    userRepository;

    public Payment savePayment(PaymentRequest request) {

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        BigDecimal balance = invoice.getBalanceAmount();

        if (request.getAmount().compareTo(balance) > 0) {
            throw new RuntimeException("Payment amount exceeds balance amount");
        }

        Payment payment = new Payment();

        payment.setInvoice(invoice);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMode(request.getPaymentMode());
        payment.setTransactionReference(request.getTransactionReference());
        payment.setRemarks(request.getRemarks());
        payment.setStudentName(request.getStudentName());
        payment.setStudentId(request.getStudentId());
        payment.setInvoiceNumber(request.getInvoiceNumber());

        AppUser appUser = new AppUser();
        appUser.setId(request.getParentId());
        payment.setUser(appUser);

        paymentRepository.save(payment);

        BigDecimal paid = invoice.getPaidAmount().add(request.getAmount());

        invoice.setPaidAmount(paid);

        invoice.setBalanceAmount(
                invoice.getTotalAmount().subtract(paid));

        if (invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);

        return payment;
    }

    public List<Payment> getPaymentHistory(Long invoiceId) {
        return paymentRepository.findByInvoiceInvoiceIdOrderByPaymentDateDesc(invoiceId);

    }

    public Page<PaymentDto> searchPayments(String search, Pageable pageable) {
        {
            String email = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            logger.info("Searching Payments with search term: {} for user: {}", search, email);


            AppUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("User not found with email: {}", email);
                        return new ResourceNotFoundException("User not found");
                    });

            if (user.getRole()!=null && user.getRole().equals("parent")){
                logger.info("Searching Payments for parent user ID: {}", user.getId());
                Page<Payment> payments = paymentRepository
                        .searchPaymentsByUser(search,user.getId(), pageable);
                if (payments == null || payments.isEmpty()) {
                    throw new ResourceNotFoundException("No Payments found for parent user");
                }
                return payments.map(this::convertToDto);
            }
            else{
                logger.info("Searching Payments with search term: {} for admin/other role", search);
                Page<Payment> Payments = paymentRepository
                        .searchPayments(search, pageable);
                if (Payments == null || Payments.isEmpty()) {
                    throw new ResourceNotFoundException("No Payments found for parent user");
                }
                return Payments.map(this::convertToDto);
            }
        }
    }

    private PaymentDto convertToDto(Payment payment) {
        return PaymentDto.builder().paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .transactionReference(payment.getTransactionReference())
                .remarks(payment.getRemarks())
                .studentName(payment.getStudentName())
                .studentId(payment.getStudentId())
                .invoiceNumber(payment.getInvoiceNumber())
                .build();
    }
}