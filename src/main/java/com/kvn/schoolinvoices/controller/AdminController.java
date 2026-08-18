package com.kvn.schoolinvoices.controller;

import com.kvn.schoolinvoices.AppUser;
import com.kvn.schoolinvoices.dto.AppUserDto;
import com.kvn.schoolinvoices.dto.StudentDTO;
import com.kvn.schoolinvoices.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  @Autowired
  private UserService userService;

  @GetMapping("/dashboard")
  @PreAuthorize("hasRole('SCHOOL_ADMIN')")
  public ResponseEntity<String> dashboard() {
 //test

    return ResponseEntity.ok("Hello from ADMIN dashboard");
  }

  @PostMapping("/user")
  @PreAuthorize("hasRole('SCHOOL_ADMIN')")
  public ResponseEntity<AppUser> createUser(@RequestBody AppUserDto appUserDto) {
    return ResponseEntity.ok(    userService.createUser(appUserDto));

  }

  @GetMapping("/user")
  public ResponseEntity<Page<AppUserDto>> getStudents(

          @RequestParam(required = false)
          String search,

          @PageableDefault(
                  page = 0,
                  size = 10,
                  sort = "email",
                  direction = Sort.Direction.ASC)
          Pageable pageable) {

    return ResponseEntity.ok(
            userService.searchUsers(
                    search,
                    pageable));
  }


}