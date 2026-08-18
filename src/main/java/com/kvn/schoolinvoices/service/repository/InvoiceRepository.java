package com.kvn.schoolinvoices.service.repository;

import com.kvn.schoolinvoices.entity.Invoice;
import com.kvn.schoolinvoices.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    @Query("SELECT COALESCE(MAX(i.invoiceId), 0) + 1 FROM Invoice i")
    Long getNextId();


    @Query("""


            SELECT s
            FROM Invoice s
            WHERE  (:search IS NULL OR
                   LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.status) LIKE LOWER(CONCAT('%', :search, '%')))
              
            """
)
    Page<Invoice> searchInvoices(
            @Param("search") String search,
            Pageable pageable);

    @Query("""


            SELECT s
            FROM Invoice s
            WHERE s.parentId =:id and (:search IS NULL OR
                   LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.status) LIKE LOWER(CONCAT('%', :search, '%')))
              
            """
    )
    Page<Invoice> searchInvoicesByUser(Long id, String search, Pageable pageable);
}