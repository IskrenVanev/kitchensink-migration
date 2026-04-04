package com.iskren.service;

import com.iskren.model.Member;
import com.iskren.model.MemberRegisteredEvent;
import com.iskren.repository.MemberRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final Validator validator;
    private final ApplicationEventPublisher eventPublisher;

    public MemberService(MemberRepository memberRepository, Validator validator, ApplicationEventPublisher eventPublisher) {
        this.memberRepository = memberRepository;
        this.validator = validator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<Member> listAllMembers() {
        return memberRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Member> lookupMemberById(long id) {
        return memberRepository.findById(id);
    }

    @Transactional
    public void createMember(Member member) {
        validateMember(member);
        log.info("Registering " + member.getName());
        memberRepository.save(member);
        eventPublisher.publishEvent(new MemberRegisteredEvent(member));
    }

    private void validateMember(Member member) {
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        if (!violations.isEmpty()) {
            log.debug("Validation completed. violations found: " + violations.size());
            throw new ConstraintViolationException(new HashSet<>(violations));
        }

        if (emailAlreadyExists(member.getEmail())) {
            throw new ValidationException("Unique Email Violation");
        }
    }

    private boolean emailAlreadyExists(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }
}
