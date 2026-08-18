package com.kvn.schoolinvoices.service;

import com.kvn.schoolinvoices.*;
import com.kvn.schoolinvoices.dto.AppUserDto;
import com.kvn.schoolinvoices.dto.StudentDTO;
import com.kvn.schoolinvoices.entity.Student;
import com.kvn.schoolinvoices.exception.ResourceNotFoundException;
import com.kvn.schoolinvoices.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser createUser(AppUserDto appUserDto) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();


        RoleName role = null;
        if (appUserDto.getRole().equals("parent")) {

            role = RoleName.ROLE_PARENT;
        }
        Role userRole = roleRepository.findByName(role)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(RoleName.ROLE_PARENT).build()
                ));

        // Create admin user if not exist
        if (!userRepository.existsByEmail(appUserDto.getEmail())) {
            AppUser user = AppUser.builder()
                    .fullName(appUserDto.getFullName())
                    .email(appUserDto.getEmail())
                    .password(passwordEncoder.encode(appUserDto.getPassword()))
                    .dateOfBirth(LocalDate.parse(appUserDto.getDateOfBirth()))
                    .gender(appUserDto.getGender())
                    .mobileNo(appUserDto.getMobileNo())
                    .address(appUserDto.getAddress())
                    .role(appUserDto.getRole())
                    .roles(Set.of(userRole))
                    .createdBy(email)
                    .build();
            AppUser appUser =userRepository.save(user);
            return appUser;
        }
        else{

            throw new UserAlreadyExistsException("User with email " + appUserDto.getEmail() + " already exists.");
        }

        //return null;
        }


    public Page<AppUserDto> searchUsers(String search, Pageable pageable) {
        Page<AppUser> users = userRepository.searchUsers(search, pageable);

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No users found.");
        }

        return users.map(this::convertToDto);
     /*   return userRepository
                .searchUsers(search, pageable)
                .map(this::convertToDto);*/
    }

    private AppUserDto convertToDto(AppUser appUser) {

        return AppUserDto.builder()
                .fullName(appUser.getFullName())
                .email(appUser.getEmail())
                .password(appUser.getPassword())
                .mobileNo(appUser.getMobileNo())
                .gender(appUser.getGender())
                .dateOfBirth(String.valueOf(appUser.getDateOfBirth()))
                .address(appUser.getAddress())
                .role(appUser.getRole())
                .id(appUser.getId())
                .build();
    }
}
