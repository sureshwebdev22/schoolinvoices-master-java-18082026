package com.kvn.schoolinvoices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvn.schoolinvoices.CustomUserDetailsService;
import com.kvn.schoolinvoices.JwtAuthenticationFilter;
import com.kvn.schoolinvoices.JwtService;
import com.kvn.schoolinvoices.dto.StudentDTO;
import com.kvn.schoolinvoices.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Use a plain ObjectMapper instance to avoid needing additional auto-configured test
    // dependencies in the @WebMvcTest slice (keeps the test self-contained).
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StudentService studentService;



    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
    @Test
    void getStudents_returnsPage() throws Exception {
        StudentDTO dto = StudentDTO.builder().studentId(1L).firstName("John").build();
        when(studentService.searchStudents(eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/students").param("search", "").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getStudentById_returnsStudent() throws Exception {
        StudentDTO dto = StudentDTO.builder().studentId(2L).firstName("Amy").build();
        when(studentService.getStudentById(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/students/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(2));
    }

    @Test
    void createStudent_returnsCreated() throws Exception {
        StudentDTO input = StudentDTO.builder().firstName("New").build();
        StudentDTO saved = StudentDTO.builder().studentId(3L).firstName("New").build();
        when(studentService.createStudent(any(StudentDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(3));
    }

    @Test
    void searchStudents_post_returnsPage() throws Exception {
        StudentDTO search = StudentDTO.builder().firstName("X").build();
        StudentDTO dto = StudentDTO.builder().studentId(4L).firstName("X").build();
        when(studentService.searchStudents(any(StudentDTO.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(post("/api/students/search")
                .param("page","0")
                .param("size","10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(search)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void updateStudent_returnsUpdated() throws Exception {
        StudentDTO input = StudentDTO.builder().firstName("Upd").build();
        StudentDTO updated = StudentDTO.builder().studentId(5L).firstName("Upd").build();
        when(studentService.updateStudent(eq(5L), any(StudentDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/students/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(5));
    }
}
