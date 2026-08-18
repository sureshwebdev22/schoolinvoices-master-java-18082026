package com.kvn.schoolinvoices.service;

import com.kvn.schoolinvoices.AppUser;
import com.kvn.schoolinvoices.UserRepository;
import com.kvn.schoolinvoices.dto.AppUserDto;
import com.kvn.schoolinvoices.dto.ParentDTO;
import com.kvn.schoolinvoices.entity.Parent;
import com.kvn.schoolinvoices.exception.ResourceNotFoundException;
import com.kvn.schoolinvoices.service.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;

    public ResponseEntity<Map<String,String>> createParent(ParentDTO dto) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();


        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException( "User not found"));

        Parent parent = new Parent();
        parent.setFatherName(dto.getFatherName());
        parent.setMotherName(dto.getMotherName());
        parent.setAddress(dto.getAddress());
        parent.setUser(user);

        parentRepository.save(parent);
        return ResponseEntity.ok(
                Map.of("message", "Student deleted successfully")
        );


        //   return null;
    }

    public Page<AppUserDto> searchParents(
            AppUserDto appUserDto,
            Pageable pageable) {
        Page<AppUser> users = userRepository
                .findByFullNameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndMobileNoContainingIgnoreCaseAndAddressContainingIgnoreCase(
                        appUserDto.getFullName(),
                        appUserDto.getEmail(),
                        appUserDto.getMobileNo(),
                        appUserDto.getAddress(),
                        pageable);

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No parent users found.");
        }

        return users.map(appUser -> new AppUserDto(
                appUser.getId(),
                appUser.getFullName(),
                appUser.getEmail(),
                appUser.getMobileNo(),
                appUser.getAddress()));
        /*

        return   userRepository.findByFullNameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndMobileNoContainingIgnoreCaseAndAddressContainingIgnoreCase(
                appUserDto.getFullName(), appUserDto.getEmail(), appUserDto.getMobileNo(),
                appUserDto.getAddress(), pageable
        ).map(appUser -> new AppUserDto(appUser.getId(), appUser.getFullName(), appUser.getEmail(), appUser.getMobileNo(), appUser.getAddress()));
    */}
}