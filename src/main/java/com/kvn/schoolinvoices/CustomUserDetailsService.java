package com.kvn.schoolinvoices;

import com.kvn.schoolinvoices.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    AppUser user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not exists: " + email));
    return new CustomUserDetails(user);
  }
}