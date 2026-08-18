package com.kvn.schoolinvoices;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByEmail(String email);
  boolean existsByEmail(String email);

  @Query("""
            SELECT s
            FROM AppUser s
            WHERE (:search IS NULL OR
                   LOWER(s.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.mobileNo) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.address) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.gender) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
  Page<AppUser> searchUsers(
          @Param("search") String search,
          Pageable pageable);

  Page<AppUser> findByFullNameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndMobileNoContainingIgnoreCaseAndAddressContainingIgnoreCase(
          String fullName, String email, String mobileNo, String address, Pageable pageable
        );


}