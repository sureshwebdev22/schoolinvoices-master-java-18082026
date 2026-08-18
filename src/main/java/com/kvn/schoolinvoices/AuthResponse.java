package com.kvn.schoolinvoices;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

  private String accessToken;
  private String refreshToken;
  private String tokenType; // e.g. "Bearer"
  private String role;
  private String fullName;

}