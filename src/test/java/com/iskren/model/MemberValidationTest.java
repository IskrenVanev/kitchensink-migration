package com.iskren.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MemberValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Member validMember() {
        Member m = new Member();
        m.setName("John Smith");
        m.setEmail("john@example.com");
        m.setPhoneNumber("2125551212");
        return m;
    }

    private Set<ConstraintViolation<Member>> violationsFor(Member m) {
        return validator.validate(m);
    }

    private boolean hasViolationOn(Set<ConstraintViolation<Member>> violations, String field) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    // --- Valid member ---

    @Test
    void validMember_producesNoViolations() {
        assertThat(violationsFor(validMember())).isEmpty();
    }

    // --- Name constraints ---

    @Test
    void nullName_producesViolationOnName() {
        Member m = validMember();
        m.setName(null);
        assertThat(hasViolationOn(violationsFor(m), "name")).isTrue();
    }

    @Test
    void emptyName_producesViolationOnName() {
        Member m = validMember();
        m.setName("");
        assertThat(hasViolationOn(violationsFor(m), "name")).isTrue();
    }

    @Test
    void nameTooLong_producesViolationOnName() {
        Member m = validMember();
        m.setName("a".repeat(26));
        assertThat(hasViolationOn(violationsFor(m), "name")).isTrue();
    }

    @Test
    void nameAtMaxLength_isValid() {
        Member m = validMember();
        m.setName("a".repeat(25));
        assertThat(hasViolationOn(violationsFor(m), "name")).isFalse();
    }

    @Test
    void nameWithDigits_producesViolationWithCustomMessage() {
        Member m = validMember();
        m.setName("John123");
        Set<ConstraintViolation<Member>> violations = violationsFor(m);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("name") &&
                v.getMessage().equals("Must not contain numbers"));
    }

    @Test
    void nameWithOnlyDigits_producesViolation() {
        Member m = validMember();
        m.setName("12345");
        assertThat(hasViolationOn(violationsFor(m), "name")).isTrue();
    }

    // --- Email constraints ---

    @Test
    void nullEmail_producesViolationOnEmail() {
        Member m = validMember();
        m.setEmail(null);
        assertThat(hasViolationOn(violationsFor(m), "email")).isTrue();
    }

    @Test
    void blankEmail_producesViolationOnEmail() {
        Member m = validMember();
        m.setEmail("   ");
        assertThat(hasViolationOn(violationsFor(m), "email")).isTrue();
    }

    @Test
    void emailMissingAtSign_producesViolationOnEmail() {
        Member m = validMember();
        m.setEmail("notanemail");
        assertThat(hasViolationOn(violationsFor(m), "email")).isTrue();
    }

    @Test
    void emailMissingDomain_producesViolationOnEmail() {
        Member m = validMember();
        m.setEmail("user@");
        assertThat(hasViolationOn(violationsFor(m), "email")).isTrue();
    }

    // --- Phone number constraints ---

    @Test
    void nullPhoneNumber_producesViolationOnPhoneNumber() {
        Member m = validMember();
        m.setPhoneNumber(null);
        assertThat(hasViolationOn(violationsFor(m), "phoneNumber")).isTrue();
    }

    @Test
    void phoneTooShort_producesViolationOnPhoneNumber() {
        Member m = validMember();
        m.setPhoneNumber("123456789");
        assertThat(hasViolationOn(violationsFor(m), "phoneNumber")).isTrue();
    }

    @Test
    void phoneTooLong_producesViolationOnPhoneNumber() {
        Member m = validMember();
        m.setPhoneNumber("1234567890123");
        assertThat(hasViolationOn(violationsFor(m), "phoneNumber")).isTrue();
    }

    @Test
    void phoneAtMinLength_isValid() {
        Member m = validMember();
        m.setPhoneNumber("1234567890");
        assertThat(hasViolationOn(violationsFor(m), "phoneNumber")).isFalse();
    }

    @Test
    void phoneAtMaxLength_isValid() {
        Member m = validMember();
        m.setPhoneNumber("123456789012");
        assertThat(hasViolationOn(violationsFor(m), "phoneNumber")).isFalse();
    }

    @Test
    void phoneWithNonDigits_producesViolationOnPhoneNumber() {
        Member m = validMember();
        m.setPhoneNumber("123456789X");
        assertThat(hasViolationOn(violationsFor(m), "phoneNumber")).isTrue();
    }

    @Test
    void phoneWithLettersOnly_producesViolationOnPhoneNumber() {
        Member m = validMember();
        m.setPhoneNumber("abcdefghij");
        assertThat(hasViolationOn(violationsFor(m), "phoneNumber")).isTrue();
    }
}
