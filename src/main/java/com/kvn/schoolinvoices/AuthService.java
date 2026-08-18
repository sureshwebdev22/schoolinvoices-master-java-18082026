package com.kvn.schoolinvoices;

import com.kvn.schoolinvoices.exception.ResourceNotFoundException;
import com.kvn.schoolinvoices.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  @Transactional
  public AuthResponse register(RegisterRequest request) {

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("Email is already in use");
      }


      Set rolesreq =  new HashSet();
      rolesreq.add("ROLE_SCHOOL_ADMIN");
      request.setRoles(rolesreq);



    Set<Role> roles = resolveRoles(request.getRoles());

    AppUser user = AppUser.builder()
        .fullName(request.getFullName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .roles(roles).role("schooladmin")
        .build();

    AppUser saved = userRepository.save(user);

    CustomUserDetails userDetails = new CustomUserDetails(saved);
    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .build();
  }

  public AuthResponse login(LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );

  if(!authentication.isAuthenticated())    {
  throw new ResourceNotFoundException("User not found");
  }



    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    String fullName = userDetails.getFullName();
    String role1 = userDetails.getAuthorities()
            .stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse(null);



    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)

        .tokenType("Bearer")
            .role(role1).fullName(fullName)
        .build();
  }

  public AuthResponse refreshToken(RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();
    String username = jwtService.extractUsername(refreshToken);

    AppUser user = userRepository.findByEmail(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    CustomUserDetails userDetails = new CustomUserDetails(user);

    if (!jwtService.isTokenValid(refreshToken, userDetails)) {
      throw new IllegalArgumentException("Invalid refresh token");
    }

    String newAccessToken = jwtService.generateAccessToken(userDetails);

    // Optionally: issue a new refresh token as well (here we keep same refresh token)
    return AuthResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .build();
  }

  private Set<Role> resolveRoles(Set<String> roleNames) {
    Set<Role> roles = new HashSet<>();

    if (roleNames == null || roleNames.isEmpty()) {
      Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
          .orElseThrow(() -> new IllegalStateException("ROLE_USER not configured"));
      roles.add(userRole);
      return roles;
    }

    for (String roleNameStr : roleNames) {
      RoleName roleName = RoleName.valueOf(roleNameStr);
      Role role = roleRepository.findByName(roleName)
          .orElseThrow(() -> new IllegalStateException(roleName + " not configured"));
      roles.add(role);
    }
    return roles;
  }
}