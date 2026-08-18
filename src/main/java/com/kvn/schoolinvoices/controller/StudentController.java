package com.kvn.schoolinvoices.controller;


import com.kvn.schoolinvoices.dto.AppUserDto;
import com.kvn.schoolinvoices.dto.StudentDTO;
import com.kvn.schoolinvoices.service.StudentService;
import com.kvn.schoolinvoices.service.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/students")
public class StudentController {

   // @Autowired
 //   private StudentRepository studentRepository;

    @Autowired
    private StudentService studentService;

    @GetMapping("")
    public ResponseEntity<Page<StudentDTO>> getStudents(

            @RequestParam(required = false)
            String search,

            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "studentId",
                    direction = Sort.Direction.ASC)
            Pageable pageable) {

        return ResponseEntity.ok(
                studentService.searchStudents(
                        search,
                        pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {

        StudentDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @PostMapping("")
   // @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<StudentDTO> createStudent(
            @RequestBody StudentDTO studentDTO) {

        return ResponseEntity.ok(
                studentService.createStudent(studentDTO));
    }

    @PostMapping("/search")
 //   @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public Page<StudentDTO> searchStudents(
            @RequestBody StudentDTO searchDTO,
            @RequestParam("page") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return studentService.searchStudents(searchDTO, pageable);
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable("id") Long id,
            @RequestBody StudentDTO studentDTO) {

        return ResponseEntity.ok(
                studentService.updateStudent(id,studentDTO));
    }

}
