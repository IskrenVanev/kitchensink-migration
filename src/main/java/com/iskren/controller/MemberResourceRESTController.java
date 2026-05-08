package com.iskren.controller;

import com.iskren.dto.MemberOrderSummaryDTO;
import com.iskren.dto.MemberSearchResponseDTO;
import com.iskren.dto.MemberStatsDTO;
import com.iskren.model.Member;
import com.iskren.service.MemberService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/rest/members")
public class MemberResourceRESTController {

    private final MemberService memberService;

    public MemberResourceRESTController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Member> listAllMembers() {
        return memberService.listAllMembers();
    }

    @GetMapping(path = "/search-advanced", produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberSearchResponseDTO searchAdvanced(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return memberService.searchWithFacet(q, page, size);
    }

    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Member> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order) {

        return memberService.hybridSearch(q, name, email, page, size, sort, order);
    }

    @GetMapping(path = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MemberStatsDTO> getMemberStats() {
        return memberService.getMemberStats();
    }

    @GetMapping(path = "/with-orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Document> getMembersWithOrders() {
        return memberService.getMembersWithOrders();
    }

    @GetMapping(path = "/orders-unwind")
    public List<Document> getOrdersUnwind() {
        return memberService.getMembersOrdersUnwind();
    }

    @GetMapping(path = "/orders-summary")
    public List<MemberOrderSummaryDTO> getOrderSummary() {
        return memberService.getMemberOrderSummary();
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Member> lookupMemberById(@PathVariable String id) {
        return memberService.lookupMemberById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createMember(@Valid @RequestBody Member member) {
        memberService.createMember(member);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        Map<String, String> response = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                response.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> response = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                response.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("email", "Email taken"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }
}
