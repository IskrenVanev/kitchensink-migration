package com.iskren.controller;

import com.iskren.kitchensink_modernized.KitchensinkModernizedApplication;
import com.iskren.model.Member;
import com.iskren.service.MemberService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = KitchensinkModernizedApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class MemberResourceRESTControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    private Member member(String id, String name, String email) {
        Member m = new Member();
        m.setId(id);
        m.setName(name);
        m.setEmail(email);
        m.setPhoneNumber("2125551212");
        return m;
    }

    // --- GET /rest/members ---

    @Test
    void GET_members_returns200WithJsonArray() throws Exception {
        when(memberService.listAllMembers()).thenReturn(List.of(
                member("1", "Alice", "alice@example.com"),
                member("2", "Bob", "bob@example.com")
        ));

        mockMvc.perform(get("/rest/members").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void GET_members_whenNoneExist_returns200WithEmptyArray() throws Exception {
        when(memberService.listAllMembers()).thenReturn(List.of());

        mockMvc.perform(get("/rest/members").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void GET_members_responseIncludesAllMemberFields() throws Exception {
        when(memberService.listAllMembers()).thenReturn(List.of(
                member("1", "Alice", "alice@example.com")
        ));

        mockMvc.perform(get("/rest/members").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("2125551212"));
    }

    // --- GET /rest/members/{id} ---

    @Test
    void GET_memberById_whenFound_returns200WithMember() throws Exception {
        when(memberService.lookupMemberById("1")).thenReturn(
                Optional.of(member("1", "Alice", "alice@example.com"))
        );

        mockMvc.perform(get("/rest/members/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void GET_memberById_whenNotFound_returns404() throws Exception {
        when(memberService.lookupMemberById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/rest/members/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // --- POST /rest/members ---

    @Test
    void POST_validMember_returns200() throws Exception {
        doNothing().when(memberService).createMember(any());

        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"alice@example.com","phoneNumber":"2125551212"}
                                """))
                .andExpect(status().isOk());

        verify(memberService).createMember(any());
    }

    @Test
    void POST_duplicateEmail_returns409WithEmailErrorField() throws Exception {
        doThrow(new ValidationException("Unique Email Violation"))
                .when(memberService).createMember(any());

        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"taken@example.com","phoneNumber":"2125551212"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.email").value("Email taken"));
    }

    @Test
    void POST_invalidPayload_emptyName_returns400WithNameField() throws Exception {
        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"alice@example.com","phoneNumber":"2125551212"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void POST_invalidPayload_badEmailFormat_returns400WithEmailField() throws Exception {
        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"not-an-email","phoneNumber":"2125551212"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void POST_invalidPayload_phoneTooShort_returns400WithPhoneNumberField() throws Exception {
        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"alice@example.com","phoneNumber":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").exists());
    }

    @Test
    void POST_invalidPayload_nameContainsDigits_returns400WithNameField() throws Exception {
        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice123","email":"alice@example.com","phoneNumber":"2125551212"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Must not contain numbers"));
    }

    @Test
    void POST_constraintViolationFromService_returns400() throws Exception {
        doThrow(new ConstraintViolationException(new HashSet<>()))
                .when(memberService).createMember(any());

        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"alice@example.com","phoneNumber":"2125551212"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_serviceThrowsGenericException_returns400WithErrorField() throws Exception {
        doThrow(new RuntimeException("Unexpected error"))
                .when(memberService).createMember(any());

        mockMvc.perform(post("/rest/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"alice@example.com","phoneNumber":"2125551212"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unexpected error"));
    }
}
