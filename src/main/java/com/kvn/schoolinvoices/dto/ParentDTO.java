package com.kvn.schoolinvoices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentDTO {

  //  private Long parentId;
    private String fatherName;
    private String motherName;
    private String address;
}