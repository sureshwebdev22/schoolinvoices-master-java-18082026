package com.kvn.schoolinvoices.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.support.JstlUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentRequest {

    private Long invoiceId;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private String paymentMode;

    private String transactionReference;

    private String remarks;

    private Long studentId;

    private String studentName;

    private String invoiceNumber;

    private Long parentId;

}