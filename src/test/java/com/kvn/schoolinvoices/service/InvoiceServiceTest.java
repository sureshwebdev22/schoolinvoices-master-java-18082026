package com.kvn.schoolinvoices.service;

import com.kvn.schoolinvoices.AppUser;
import com.kvn.schoolinvoices.UserRepository;
import com.kvn.schoolinvoices.dto.InvoiceDTO;
import com.kvn.schoolinvoices.dto.InvoiceItemDTO;
import com.kvn.schoolinvoices.entity.Invoice;
import com.kvn.schoolinvoices.entity.InvoiceItem;
import com.kvn.schoolinvoices.entity.InvoiceStatus;
import com.kvn.schoolinvoices.entity.Student;
import com.kvn.schoolinvoices.service.repository.InvoiceRepository;
import com.kvn.schoolinvoices.service.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void save_createsInvoice_setsTotalsAndInvoiceNumber() {
        // prepare DTO with two items
        InvoiceItemDTO item1 = InvoiceItemDTO.builder().feeType("TUITION").amount(new BigDecimal("100.00")).discount(new BigDecimal("10.00")).build();
        InvoiceItemDTO item2 = InvoiceItemDTO.builder().feeType("LAB").amount(new BigDecimal("50.00")).discount(new BigDecimal("5.00")).build();

        InvoiceDTO dto = InvoiceDTO.builder()
                .studentId(11L)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .parentId(22L)
                .invoiceItems(List.of(item1, item2))
                .build();

        Student student = new Student();
        student.setStudentId(11L);
        student.setFirstName("John");

        when(studentRepository.findById(11L)).thenReturn(Optional.of(student));

        // mock save to assign id
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice arg = inv.getArgument(0);
            if (arg.getInvoiceId() == null) {
                arg.setInvoiceId(5L);
            }
            return arg;
        });

        Invoice saved = invoiceService.save(dto);

        assertNotNull(saved);
        // totals: (100-10) + (50-5) = 135
        assertEquals(new BigDecimal("135.00"), saved.getTotalAmount());
        assertEquals(InvoiceStatus.UNPAID, saved.getStatus());

        String expectedNumber = String.format("INV-%d-%06d", Year.now().getValue(), 5L);
        assertEquals(expectedNumber, saved.getInvoiceNumber());

        verify(invoiceRepository, atLeastOnce()).save(any(Invoice.class));
    }

    @Test
    void getNextInvoiceNumber_formatsUsingRepositoryNextId() {
        when(invoiceRepository.getNextId()).thenReturn(42L);

        String next = invoiceService.getNextInvoiceNumber();

        String expected = String.format("INV-%d-%06d", Year.now().getValue(), 42L);
        assertEquals(expected, next);
    }

    @Test
    void getInvoice_returnsDtoWhenFound() {
        Student student = new Student();
        student.setStudentId(2L);
        student.setFirstName("Amy");

        Invoice invoice = new Invoice();
        invoice.setInvoiceId(2L);
        invoice.setInvoiceNumber("INV-2026-000002");
        invoice.setTotalAmount(new BigDecimal("200.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(new BigDecimal("200.00"));
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(10));
        invoice.setStudent(student);
        invoice.setInvoiceItems(List.of(new InvoiceItem()));

        when(invoiceRepository.findById(2L)).thenReturn(Optional.of(invoice));

        Optional<InvoiceDTO> dtoOpt = invoiceService.getInvoice(2L);

        assertTrue(dtoOpt.isPresent());
        InvoiceDTO dto = dtoOpt.get();
        assertEquals(2L, dto.getInvoiceID());
        assertEquals(invoice.getInvoiceNumber(), dto.getInvoiceNumber());
        assertEquals(invoice.getTotalAmount(), dto.getTotalAmount());
    }

    @Test
    void searchInvoices_parentRole_usesUserSpecificRepository() {
        String email = "parent@example.com";
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, null));

        AppUser user = new AppUser();
        user.setId(7L);
        user.setEmail(email);
        user.setRole("parent");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(invoiceRepository.searchInvoicesByUser(eq(7L), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        invoiceService.searchInvoices("", Pageable.unpaged());

        verify(invoiceRepository, times(1)).searchInvoicesByUser(eq(7L), anyString(), any(Pageable.class));
    }
}
