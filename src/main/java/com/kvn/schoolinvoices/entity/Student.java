package com.kvn.schoolinvoices.entity;

import com.kvn.schoolinvoices.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "students")
@Getter
@Setter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "admission_no", nullable = false, unique = true)
    private String admissionNo;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "class_name")
    private String className;

    @Column(name = "section_name")
    private String sectionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StudentStatus status;

    @Column
    private String createdBy;


  /*  @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent; */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private AppUser user;

    @OneToMany(mappedBy = "student")
    private List<Invoice> invoices;
}