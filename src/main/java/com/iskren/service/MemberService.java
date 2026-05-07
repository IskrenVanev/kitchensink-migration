package com.iskren.service;

import com.iskren.dto.MemberSearchResponseDTO;
import com.iskren.dto.MemberStatsDTO;
import com.iskren.model.Member;
import com.iskren.repository.MemberRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.websocket.Decoder.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

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
    public List<Member> hybridSearch(String q, String name, String email, int page, int size,
                                    String sortField, String order) {
        Query query;

        //decide strategy
        if (q != null && q.length() >= 3){
            //Text search
            TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(q);
            query = TextQuery.queryText(criteria).sortByScore();
        }
        else{
            //regex filtering
            query = new Query();
            List<Criteria> criteriaList = new ArrayList<>();

            if (name != null && !name.isBlank()){
                criteriaList.add(Criteria.where("name").regex(name, "i"));
            }

            if (email != null && !email.isBlank()){
                criteriaList.add(Criteria.where("email").regex(email, "i"));
            }

            //fallback if only short q is provided
            if ((name == null || name.isBlank()) && (email == null || email.isBlank()) && q != null && !q.isBlank()){
                criteriaList.add(new Criteria().orOperator(
                    Criteria.where("name").regex(q, "i"),
                    Criteria.where("email").regex(q, "i")
                ));
            }

            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }
        }

        //sorting
        if (!(query instanceof TextQuery)) {
            List<String> allowedFields = List.of("name", "email");

            if (!allowedFields.contains(sortField)){
                sortField = "name";
            }

            Sort sort = order.equalsIgnoreCase("desc")
                    ? Sort.by(sortField).descending()
                    : Sort.by(sortField).ascending();
            
            query.with(sort);
        }

        //pagination safety
        if (size > 50) size = 50;
        if (page < 0) page = 0;

        query.with(PageRequest.of(page, size));

        return mongoTemplate.find(query, Member.class);
    }

    @Transactional(readOnly = true)
    public MemberSearchResponseDTO searchWithFacet(String q, int page, int size) {

        if (size > 50) size = 50;
        if (page < 0) page = 0;

        List<AggregationOperation> operations = new ArrayList<>();

        // match logic
        if (q != null && !q.isBlank()) {
            if (q.length() >= 3) {
                operations.add(Aggregation.match(
                        TextCriteria.forDefaultLanguage().matching(q)
                ));
            } else {
                operations.add(Aggregation.match(
                        new Criteria().orOperator(
                                Criteria.where("name").regex(q, "i"),
                                Criteria.where("email").regex(q, "i")
                        )
                ));
            }
        }

        //facet
        operations.add(
                Aggregation.facet(
                        Aggregation.skip((long) page * size),
                        Aggregation.limit(size)
                ).as("data")
                .and(Aggregation.count().as("count"))
                .as("total")
        );

        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<Document> results =
                mongoTemplate.aggregate(aggregation, "members", Document.class);

        Document raw = results.getUniqueMappedResult();

        if (raw == null) {
            return new MemberSearchResponseDTO(List.of(), 0);
        }

        //data
        List<Document> documents = raw.getList("data", Document.class);
        if (documents == null) documents = List.of();

        List<Member> data = documents.stream()
                .map(doc -> mongoTemplate.getConverter().read(Member.class, doc))
                .toList();

        // total
        List<Document> totalList = raw.getList("total", Document.class);

        long total = 0;
        if (totalList != null && !totalList.isEmpty()) {
            total = ((Number) totalList.get(0).get("count")).longValue();
        }

        return new MemberSearchResponseDTO(data, total);
    }

    @Transactional(readOnly = true)
    public List<MemberStatsDTO> getMemberStats(){
        Aggregation aggregation = Aggregation.newAggregation(
            //extract domain from email
            Aggregation.project()
                .andExpression("substr(email, indexOfBytes(email, '@')+ 1, strLenBytes(email))")
                .as("domain"),

            //group by domain
            Aggregation.group("domain")
                .count().as("count"),

            //rename _id to domain
            Aggregation.project("count")
                .and("_id").as("domain")
        );
        
        AggregationResults<MemberStatsDTO> results = 
            mongoTemplate.aggregate(aggregation, "members", MemberStatsDTO.class);

        return results.getMappedResults();
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
