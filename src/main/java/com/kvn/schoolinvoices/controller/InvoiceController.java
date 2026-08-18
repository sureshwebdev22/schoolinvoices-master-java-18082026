package com.kvn.schoolinvoices.controller;

import com.kvn.schoolinvoices.dto.InvoiceDTO;
import com.kvn.schoolinvoices.dto.StudentDTO;
import com.kvn.schoolinvoices.entity.Invoice;
import com.kvn.schoolinvoices.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/schooladmin")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/invoices")
    public ResponseEntity<Invoice> createInvoice(
            @RequestBody InvoiceDTO dto){

        return ResponseEntity.ok(invoiceService.save(dto));

    }

    @GetMapping("/invoices/next-number")
    public ResponseEntity<Map<String,String>> getNextInvoiceNumber() {
        return ResponseEntity.ok(Map.of("nextInvoiceNumber",invoiceService.getNextInvoiceNumber()));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<InvoiceDTO>> viewInvoices(@RequestParam(required = false) String search, @RequestParam("page") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(invoiceService.searchInvoices(search, pageable));

    }

    @GetMapping("/invoices/{invoiceId}")
    public  ResponseEntity<InvoiceDTO> getInvoice(@PathVariable("invoiceId") Long invoiceId){
        //invoiceService.getInvoice(invoiceId).get();

        return ResponseEntity.ok(invoiceService.getInvoice(invoiceId).get());
    }

}