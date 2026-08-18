
package com.kvn.schoolinvoices;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public CommandLineRunner initData() {
    return args -> {
      // Create roles if not exist
      Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
          .orElseGet(() -> roleRepository.save(
              Role.builder().name(RoleName.ROLE_USER).build()
          ));

      Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
          .orElseGet(() -> roleRepository.save(
              Role.builder().name(RoleName.ROLE_ADMIN).build()
          ));

      Role schoolAdminRole = roleRepository.findByName(RoleName.ROLE_SCHOOL_ADMIN)
              .orElseGet(() -> roleRepository.save(
                      Role.builder().name(RoleName.ROLE_SCHOOL_ADMIN).build()
              ));

      // Create admin user if not exist
      if (!userRepository.existsByEmail("admin@example.com")) {
        AppUser admin = AppUser.builder()
            .fullName("Admin User")
            .email("admin@example.com")
            .password(passwordEncoder.encode("admin123"))
            .roles(Set.of(userRole, adminRole))
            .build();

        userRepository.save(admin);
      }

      // Create admin user if not exist
      if (!userRepository.existsByEmail("schooladmin@example.com")) {
        AppUser schoolAdmin = AppUser.builder()
                .fullName("Admin User")
                .email("schooladmin@example.com")
                .password(passwordEncoder.encode("schooladmin123"))
                .roles(Set.of(schoolAdminRole))
                .build();

        userRepository.save(schoolAdmin);
      }
    };
  }
}
