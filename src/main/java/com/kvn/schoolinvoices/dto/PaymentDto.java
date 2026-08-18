package com.kvn.schoolinvoices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private BigDecimal amount;

    private LocalDate paymentDate;

    private String paymentMode;

    private String transactionReference;

    private String remarks;

    private Long studentId;

    private String studentName;

    private String invoiceNumber;
}
