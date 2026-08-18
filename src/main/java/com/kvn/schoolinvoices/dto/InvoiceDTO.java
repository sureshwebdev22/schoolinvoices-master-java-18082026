package com.kvn.schoolinvoices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private Long invoiceID;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private Long studentId;

    private String invoiceNumber;

    private Long parentId;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount = BigDecimal.ZERO;

    private BigDecimal balanceAmount;


    private List<InvoiceItemDTO> invoiceItems;

    private StudentDTO studentDTO;

}