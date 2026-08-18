package com.kvn.schoolinvoices.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserDto {

   private String email;
   private String password;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String mobileNo;
    private String role;
    private String address;
    private Long id;

    public AppUserDto(Long id,String fullName, String email, String mobileNo, String address) {
        this.id=id;
        this.fullName =fullName;
        this.email = email;
        this.mobileNo = mobileNo;
        this.address = address;

    }
}
