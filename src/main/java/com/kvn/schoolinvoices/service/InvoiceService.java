package com.kvn.schoolinvoices.service;

import com.kvn.schoolinvoices.AppUser;
import com.kvn.schoolinvoices.UserRepository;
import com.kvn.schoolinvoices.dto.InvoiceDTO;
import com.kvn.schoolinvoices.dto.InvoiceItemDTO;
import com.kvn.schoolinvoices.dto.StudentDTO;
import com.kvn.schoolinvoices.entity.Invoice;
import com.kvn.schoolinvoices.entity.InvoiceItem;
import com.kvn.schoolinvoices.entity.InvoiceStatus;
import com.kvn.schoolinvoices.entity.Student;
import com.kvn.schoolinvoices.exception.ResourceNotFoundException;
import com.kvn.schoolinvoices.service.repository.InvoiceRepository;
import com.kvn.schoolinvoices.service.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    public Invoice save(InvoiceDTO dto) {

        logger.info("Creating new invoice for student ID: {}", dto.getStudentId());

        Student student =
                studentRepository.findById(dto.getStudentId())
                        .orElseThrow();

        logger.debug("Found student: {} {}", student.getFirstName(), student.getLastName());

        Invoice invoice = new Invoice();

        invoice.setStudent(student);
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setParentId(dto.getParentId());

        List<InvoiceItem> items = new ArrayList<>();

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (InvoiceItemDTO itemDto : dto.getInvoiceItems()) {

            InvoiceItem item = new InvoiceItem();

            item.setInvoice(invoice);
            item.setFeeType(itemDto.getFeeType());

            item.setAmount(itemDto.getAmount());
            item.setDiscount(itemDto.getDiscount());

            BigDecimal total =
                    itemDto.getAmount().subtract(itemDto.getDiscount());

            item.setTotal(total);

            grandTotal = grandTotal.add(total);

            items.add(item);

        }

        invoice.setInvoiceItems(items);

        invoice.setTotalAmount(grandTotal);
        invoice.setBalanceAmount(grandTotal);

        logger.debug("Invoice total amount: {}", grandTotal);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        savedInvoice.setInvoiceNumber(
                "INV-" + LocalDate.now().getYear() + "-" +
                        String.format("%06d", savedInvoice.getInvoiceId())
        );

        logger.info("Invoice created successfully with number: {}", savedInvoice.getInvoiceNumber());

        return invoiceRepository.save(savedInvoice);

    }

    public String getNextInvoiceNumber() {

        Long nextId = invoiceRepository.getNextId();

        String invoiceNumber = String.format("INV-%d-%06d",
                Year.now().getValue(),
                nextId);

        logger.debug("Generated next invoice number: {}", invoiceNumber);

        return invoiceNumber;
    }


    public Page<InvoiceDTO> searchInvoices(String search, Pageable pageable) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        logger.info("Searching invoices for user: {} with search term: {}", email, search);

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Page<Invoice> invoicePage =null;
        if (user.getRole()!=null && user.getRole().equals("parent")){
            logger.debug("User is a parent, filtering invoices by user ID: {}", user.getId());
          //  return invoiceRepository.searchInvoicesByUser(user.getId(),search, pageable).map(this::convertToDto);
            invoicePage = invoiceRepository.searchInvoicesByUser(user.getId(),search, pageable);

        }
        else{
            logger.debug("User is an admin/staff, retrieving all invoices");
            invoicePage = invoiceRepository.searchInvoices(search, pageable);
         //   return invoiceRepository.searchInvoices(search, pageable).map(this::convertToDto);

        }
        if(invoicePage.isEmpty()){
            throw new ResourceNotFoundException("Invoices not found");
        }
        return invoicePage.map(this::convertToDto);

    }

    private InvoiceDTO convertToDto(Invoice invoice) {

        InvoiceDTO build = InvoiceDTO.builder().invoiceID(invoice.getInvoiceId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .totalAmount(invoice.getTotalAmount())
                .balanceAmount(invoice.getBalanceAmount())
                .paidAmount(invoice.getPaidAmount())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .studentId(invoice.getStudent().getStudentId())
                .invoiceItems(convertInvoiceItemsToDto(invoice.getInvoiceItems()))
                .studentDTO(convertStudentTodto(invoice.getStudent()))
                .parentId(invoice.getParentId())
                .build();
        return build;

    }

    private StudentDTO convertStudentTodto(Student student) {

        StudentDTO build = StudentDTO.builder().studentId(student.getStudentId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .admissionNo(student.getAdmissionNo())
                .status(String.valueOf(student.getStatus()))
                .sectionName(student.getSectionName())
                .className(student.getClassName()).build();
        return build;

    }

    private List<InvoiceItemDTO> convertInvoiceItemsToDto(List<InvoiceItem> invoiceItems) {
       return invoiceItems.stream().map(this::convertInvoiceItemsToDto1).toList();
    }

    private InvoiceItemDTO convertInvoiceItemsToDto1(InvoiceItem invoiceItem) {
        return InvoiceItemDTO.builder()
                .feeType(invoiceItem.getFeeType())
                .amount(invoiceItem.getAmount())
                .discount(invoiceItem.getDiscount())
                .build();
    }

    public Optional<InvoiceDTO> getInvoice(Long invoiceId) {
        logger.info("Fetching invoice with ID: {}", invoiceId);
        Optional<InvoiceDTO> invoiceDTO = invoiceRepository.findById(invoiceId).map(this::convertToDto);
        if (invoiceDTO.isPresent()) {
            logger.debug("Invoice found: {}", invoiceId);
        } else {
            logger.warn("Invoice not found with ID: {}", invoiceId);
        }
        return invoiceDTO;
       
    }
}