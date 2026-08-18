package com.kvn.schoolinvoices.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private Long studentId;
    private String admissionNo;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dob;
    private String className;
    private String sectionName;
    private String status;
    private Long parentId;
    private String parentName;
    private List<InvoiceDTO> invoiceDTOList;


    public StudentDTO(Long studentId, String admissionNo, String firstName, String lastName, String className, String sectionName ,String parentName) {
        this.studentId = studentId;
        this.admissionNo =admissionNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.className = className;
        this.sectionName = sectionName;
        this.parentName = parentName;
    }
}