package com.kvn.schoolinvoices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceItemId;

    @ManyToOne
    @JoinColumn(name="invoice_id")
    private Invoice invoice;

    private String feeType;

    private BigDecimal amount;

    private BigDecimal discount;

    private BigDecimal total;

}