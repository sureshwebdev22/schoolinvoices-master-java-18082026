package com.kvn.schoolinvoices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="invoices")
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @Column(unique = true)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="student_id")
    private Student student;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @Column
    private Long parentId;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount = BigDecimal.ZERO;

    private BigDecimal balanceAmount;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @OneToMany(mappedBy="invoice",
            cascade=CascadeType.ALL,
            orphanRemoval=true)
    private List<InvoiceItem> invoiceItems = new ArrayList<>();

    @OneToMany(mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    // getters/setters
}