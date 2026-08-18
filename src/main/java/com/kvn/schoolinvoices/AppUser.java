package com.kvn.schoolinvoices;

import com.kvn.schoolinvoices.entity.Parent;
import com.kvn.schoolinvoices.entity.Payment;
import com.kvn.schoolinvoices.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
//Test12345678
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column
  private String gender;

  @Column(nullable = true)
  private String mobileNo;

  @Column(length = 100)
  private String role;

  @Column(length = 100)
  private String address;

  @Column(length = 100)
  private String createdBy;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

 /* @OneToMany(
          mappedBy = "user",
          cascade = CascadeType.ALL,
          orphanRemoval = true
  )
  private List<Parent> parents; */

  @OneToMany(
          mappedBy = "user",
          cascade = CascadeType.ALL,
          orphanRemoval = true
  )
  private List<Student> students;

  @OneToMany(
          mappedBy = "user",
          cascade = CascadeType.ALL,
          orphanRemoval = true
  )
  private List<Payment> payments;


}