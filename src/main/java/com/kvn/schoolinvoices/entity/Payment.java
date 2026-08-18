package com.kvn.schoolinvoices.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kvn.schoolinvoices.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne
    @JoinColumn(name="invoice_id")
    @JsonIgnore
    private Invoice invoice;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private String paymentMode;

    private String transactionReference;

    private String remarks;

    private Long studentId;

    private String studentName;

    private String invoiceNumber;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private AppUser user;
}