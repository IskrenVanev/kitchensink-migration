package com.iskren.service;

import com.iskren.model.Member;
import com.iskren.repository.MemberRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final Validator validator;
    private final MongoTemplate mongoTemplate;

    public MemberService(MemberRepository memberRepository, Validator validator, MongoTemplate mongoTemplate) {
        this.memberRepository = memberRepository;
        this.validator = validator;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional(readOnly = true)
    public List<Member> listAllMembers() {
        return memberRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Member> lookupMemberById(String id) {
        return memberRepository.findById(id);
    }

    @Transactional
    public void createMember(Member member) {
        validateMember(member);
        log.info("Registering " + member.getName());
        memberRepository.save(member);
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public List<Member> searchMembers(String name, String email,
                                  int page, int size,
                                  String sortField, String order) {
        Query query = new Query();

        List<Criteria> criteriaList = new ArrayList<>();

        //filtering
        if (name != null && !name.isBlank()) {
            criteriaList.add(Criteria.where("name").regex(name, "i"));
        }

        if (email != null && !email.isBlank()) {
            criteriaList.add(Criteria.where("email").regex(email, "i"));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        //sorting
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();
        query.with(sort);

        //pagination safety
        if (size > 50){
            size = 50;
        }

        if (page < 0) {
            page = 0;
        }

        //pagination
        query.with(PageRequest.of(page, size));

        return mongoTemplate.find(query, Member.class);
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
