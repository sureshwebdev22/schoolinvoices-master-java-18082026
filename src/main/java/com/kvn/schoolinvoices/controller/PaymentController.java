package com.kvn.schoolinvoices.controller;


import com.kvn.schoolinvoices.dto.PaymentDto;
import com.kvn.schoolinvoices.dto.PaymentRequest;
import com.kvn.schoolinvoices.dto.StudentDTO;
import com.kvn.schoolinvoices.entity.Payment;
import com.kvn.schoolinvoices.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Payment> savePayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.savePayment(request));
    }

    @GetMapping("/invoice/{invoiceId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Payment>> getPaymentHistory(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(invoiceId));
    }

    @GetMapping("")
    public ResponseEntity<Page<PaymentDto>> getPayments(@RequestParam(required = false) String searchText, @RequestParam("page") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);


        return ResponseEntity.ok(
                paymentService.searchPayments(
                        searchText,
                        pageable));
    }

}