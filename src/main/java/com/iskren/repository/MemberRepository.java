package com.iskren.repository;

import com.iskren.model.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberRepository extends MongoRepository<Member, String> {

    Optional<Member> findByEmail(String email);

    List<Member> findAllByOrderByNameAsc();
}
