package com.kvn.schoolinvoices.service.repository;

import com.kvn.schoolinvoices.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    @Query(value = """
            SELECT s
            FROM Payment s
            WHERE s.user.id = :userId AND (
                   :search IS NULL OR
                   LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.studentName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                         LOWER(s.paymentMode) LIKE LOWER(CONCAT('%', :search, '%')) 
                  )
            """)
    Page<Payment> searchPaymentsByUser(@Param("search") String search, @Param("userId") Long userId, Pageable pageable);

    @Query(value = """
            SELECT s
            FROM Payment s
            WHERE (:search IS NULL OR
                   LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.studentName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.paymentMode) LIKE LOWER(CONCAT('%', :search, '%')) 
                  )
            """)
    Page<Payment> searchPayments(@Param("search") String search, Pageable pageable);
}