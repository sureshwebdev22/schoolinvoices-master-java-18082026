package com.kvn.schoolinvoices.service;

import com.kvn.schoolinvoices.AppUser;
import com.kvn.schoolinvoices.UserRepository;
import com.kvn.schoolinvoices.dto.AppUserDto;
import com.kvn.schoolinvoices.dto.InvoiceDTO;
import com.kvn.schoolinvoices.dto.StudentDTO;
import com.kvn.schoolinvoices.entity.Invoice;
import com.kvn.schoolinvoices.entity.Parent;
import com.kvn.schoolinvoices.entity.Student;
import com.kvn.schoolinvoices.entity.StudentStatus;
import com.kvn.schoolinvoices.exception.ResourceNotFoundException;
import com.kvn.schoolinvoices.service.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;


    public Page<StudentDTO> searchStudents(
            String search,
            Pageable pageable) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        logger.info("Searching students with search term: {} for user: {}", search, email);

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        if (user.getRole()!=null && user.getRole().equals("parent")){
            logger.info("Searching students for parent user ID: {}", user.getId());
            Page<Student> students = studentRepository
                    .searchStudentsByUser(search,user.getId(), pageable);
            if (students == null || students.isEmpty()) {
                throw new ResourceNotFoundException("No students found for parent user");
            }
            return students.map(this::convertToDto);
        }
        else{
            logger.info("Searching students with search term: {} for admin/other role", search);
            Page<Student> students = studentRepository
                    .searchStudents(search, pageable);
            if (students == null || students.isEmpty()) {
                throw new ResourceNotFoundException("No students found for parent user");
            }
            return students.map(this::convertToDto);
        }
    }


    private StudentDTO convertToDto(Student student) {


        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .admissionNo(student.getAdmissionNo())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .gender(student.getGender())
                .parentName(        student.getUser().getFullName())
                .parentId(student.getUser().getId())

            //    .dob(LocalDate.parse(student.getDob()))
                .className(student.getClassName())
                .sectionName(student.getSectionName())
                .status(student.getStatus().name())
            //    .invoiceDTOList(convertInvoicestDTOList(student.getInvoices()))
               .build();
    }

    private List<InvoiceDTO> convertInvoicestDTOList(List<Invoice> invoices) {
        return invoices.stream()
                .map(this::convertToDto1)
                .collect(Collectors.toList());
    }

    private InvoiceDTO convertToDto1(Invoice invoice) {
        return InvoiceDTO.builder().invoiceID(invoice.getInvoiceId()).invoiceDate(invoice.getInvoiceDate()).
                dueDate(invoice.getDueDate()).invoiceNumber(invoice.getInvoiceNumber()).build();
    }

    public StudentDTO getStudentById(Long id) {
        logger.info("Retrieving student with ID: {}", id);
        return studentRepository.findById(id).map(this::convertToDto)
                .orElseThrow(() -> {
                    logger.error("Student not found with id: {}", id);
                    return new ResourceNotFoundException("Student not found with id: " + id);
                });
    }

    public StudentDTO updateStudent(Long id, StudentDTO student) {
        logger.info("Updating student with ID: {}", id);
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Student not found with id: {}", id);
                    return new ResourceNotFoundException("Student not found with id: " + id);
                });

        existingStudent.setAdmissionNo(student.getAdmissionNo());
        existingStudent.setFirstName(student.getFirstName());
        existingStudent.setLastName(student.getLastName());
        existingStudent.setGender(student.getGender());
        existingStudent.setClassName(student.getClassName());
        existingStudent.setSectionName(student.getSectionName());
        existingStudent.setStatus(StudentStatus.valueOf(student.getStatus()));

        studentRepository.save(existingStudent);
        logger.info("Student with ID: {} updated successfully", id);

        return student;
    }

    public StudentDTO createStudent(StudentDTO dto) {
        logger.info("Creating new student with admission number: {}", dto.getAdmissionNo());

        if (studentRepository.existsByAdmissionNo(dto.getAdmissionNo())) {
            logger.warn("Admission Number already exists: {}", dto.getAdmissionNo());
            throw new ResourceNotFoundException("Admission Number already exists");
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Student student = new Student();
        student.setAdmissionNo(dto.getAdmissionNo());
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setGender(dto.getGender());
        student.setClassName(dto.getClassName());
        student.setSectionName(dto.getSectionName());
        student.setStatus(StudentStatus.valueOf(dto.getStatus()));
        student.setCreatedBy(email);

        AppUser appUser = new AppUser();
        appUser.setId(dto.getParentId());
        student.setUser(appUser);

        Student savedStudent = studentRepository.save(student);
        logger.info("Student created successfully with ID: {}", savedStudent.getStudentId());

        dto.setStudentId(savedStudent.getStudentId());

        return dto;
    }

    @Transactional
    public  void deleteStudent(Long id){
        logger.info("Deleting student with ID: {}", id);
        studentRepository.deleteById(id);
        logger.info("Student with ID: {} deleted successfully", id);
    }

    public Page<StudentDTO> searchStudents(StudentDTO searchDTO, Pageable pageable) {
        logger.info("Searching students with admission number: {}, first name: {}, last name: {}",
            searchDTO.getAdmissionNo(), searchDTO.getFirstName(), searchDTO.getLastName());

        Page<StudentDTO> map = studentRepository.findByAdmissionNoContainingIgnoreCaseAndFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                searchDTO.getAdmissionNo(), searchDTO.getFirstName(), searchDTO.getLastName()
                , pageable
        ).map(student -> new StudentDTO(student.getStudentId(), student.getAdmissionNo(), student.getFirstName(), student.getLastName(),
                student.getClassName(), student.getSectionName(),student.getUser().getFullName()));

        logger.info("Found {} students", map.getTotalElements());
        return map;

    }


   /* public Page<StudentDTO> searchParents(StudentDTO searchDTO, Pageable pageable) {
        return   studentRepository.findByAdmissionNoContainingIgnoreCaseAndFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                searchDTO.getAdmissionNo(), searchDTO.getFirstName(), searchDTO.getLastName(),
                , pageable
        ).map(student -> new StudentDTO(student.getStudentId(),student.getAdmissionNo(), student.getFirstName(), student.getLastName(),
                student.getClassName(),student.getSectionName());

        return null;
    }*/
}