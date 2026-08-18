package com.kvn.schoolinvoices.controller;


import com.kvn.schoolinvoices.dto.AppUserDto;
import com.kvn.schoolinvoices.dto.ParentDTO;
import com.kvn.schoolinvoices.dto.ParentSearchDTO;
import com.kvn.schoolinvoices.entity.Parent;
import com.kvn.schoolinvoices.service.ParentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/schooladmin")

public class ParentController {

    @Autowired
    private ParentService parentService;

    @PostMapping("/parents")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<Map<String,String>> createParent(
            @RequestBody ParentDTO dto) {

             return parentService.createParent(dto);
    }


    @PostMapping("/parents/search")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public Page<AppUserDto> searchParents(
            @RequestBody AppUserDto searchDTO,
            @RequestParam("page") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return parentService.searchParents(searchDTO, pageable);
    }
}
