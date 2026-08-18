package com.kvn.schoolinvoices;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

  @NotBlank
  private String fullName;

  @Email
  @NotBlank
  private String email;

  @NotBlank
  @Size(min = 4, message = "Password must be at least 4au characters")
  private String password;

  // Optional: allow passing roles, otherwise default to ROLE_USER
  private Set<String> roles;
}