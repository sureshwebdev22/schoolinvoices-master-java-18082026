package com.kvn.schoolinvoices.service.repository;

import com.kvn.schoolinvoices.entity.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Page<Parent> findByFatherNameContainingIgnoreCaseAndMotherNameContainingIgnoreCaseAndAddressContainingIgnoreCase(
            String fatherName,
            String motherName,
            String address,
            Pageable pageable);
}