package com.iskren.service;

import com.iskren.model.Member;
import com.iskren.model.MemberRegisteredEvent;
import com.iskren.repository.MemberRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Validator validator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, validator, eventPublisher);
    }

    private Member validMember() {
        Member m = new Member();
        m.setName("Jane Doe");
        m.setEmail("jane@example.com");
        m.setPhoneNumber("2125551234");
        return m;
    }

    // --- listAllMembers ---

    @Test
    void listAllMembers_delegatesToRepositoryAndReturnsResult() {
        Member alice = validMember();
        alice.setName("Alice");
        Member bob = validMember();
        bob.setName("Bob");
        when(memberRepository.findAllByOrderByNameAsc()).thenReturn(List.of(alice, bob));

        List<Member> result = memberService.listAllMembers();

        assertThat(result).containsExactly(alice, bob);
        verify(memberRepository).findAllByOrderByNameAsc();
    }

    @Test
    void listAllMembers_whenRepositoryIsEmpty_returnsEmptyList() {
        when(memberRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        List<Member> result = memberService.listAllMembers();

        assertThat(result).isEmpty();
    }

    // --- lookupMemberById ---

    @Test
    void lookupMemberById_whenFound_returnsMemberInOptional() {
        Member m = validMember();
        when(memberRepository.findById("1")).thenReturn(Optional.of(m));

        Optional<Member> result = memberService.lookupMemberById("1");

        assertThat(result).contains(m);
    }

    @Test
    void lookupMemberById_whenNotFound_returnsEmptyOptional() {
        when(memberRepository.findById("99")).thenReturn(Optional.empty());

        Optional<Member> result = memberService.lookupMemberById("99");

        assertThat(result).isEmpty();
    }

    // --- createMember: happy path ---

    @Test
    void createMember_withValidMember_savesToRepository() {
        Member m = validMember();
        when(validator.validate(m)).thenReturn(Set.of());
        when(memberRepository.findByEmail(m.getEmail())).thenReturn(Optional.empty());

        memberService.createMember(m);

        verify(memberRepository).save(m);
    }

    @Test
    void createMember_withValidMember_publishesMemberRegisteredEvent() {
        Member m = validMember();
        when(validator.validate(m)).thenReturn(Set.of());
        when(memberRepository.findByEmail(m.getEmail())).thenReturn(Optional.empty());

        memberService.createMember(m);

        ArgumentCaptor<MemberRegisteredEvent> captor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getMember()).isEqualTo(m);
    }

    // --- createMember: validation failure ---

    @Test
    @SuppressWarnings("unchecked")
    void createMember_withConstraintViolations_throwsConstraintViolationException() {
        Member m = validMember();
        ConstraintViolation<Member> violation = mock(ConstraintViolation.class);
        when(validator.validate(m)).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> memberService.createMember(m))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createMember_withConstraintViolations_doesNotSaveOrPublish() {
        Member m = validMember();
        ConstraintViolation<Member> violation = mock(ConstraintViolation.class);
        when(validator.validate(m)).thenReturn(Set.of(violation));

        try {
            memberService.createMember(m);
        } catch (ConstraintViolationException ignored) {
        }

        verify(memberRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // --- createMember: duplicate email ---

    @Test
    void createMember_withDuplicateEmail_throwsValidationException() {
        Member m = validMember();
        when(validator.validate(m)).thenReturn(Set.of());
        when(memberRepository.findByEmail(m.getEmail())).thenReturn(Optional.of(new Member()));

        assertThatThrownBy(() -> memberService.createMember(m))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createMember_withDuplicateEmail_doesNotSaveOrPublish() {
        Member m = validMember();
        when(validator.validate(m)).thenReturn(Set.of());
        when(memberRepository.findByEmail(m.getEmail())).thenReturn(Optional.of(new Member()));

        try {
            memberService.createMember(m);
        } catch (ValidationException ignored) {
        }

        verify(memberRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createMember_emailUniquenessIsCheckedAfterConstraintValidation() {
        Member m = validMember();
        when(validator.validate(m)).thenReturn(Set.of());
        when(memberRepository.findByEmail(m.getEmail())).thenReturn(Optional.empty());

        memberService.createMember(m);

        verify(validator).validate(m);
        verify(memberRepository).findByEmail(m.getEmail());
    }
}
