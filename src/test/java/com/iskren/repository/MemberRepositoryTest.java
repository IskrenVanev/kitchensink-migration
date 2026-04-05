package com.iskren.repository;

import com.iskren.kitchensink_modernized.KitchensinkModernizedApplication;
import com.iskren.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = KitchensinkModernizedApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void cleanDatabase() {
        memberRepository.deleteAll();
    }

    private Member buildMember(String name, String email) {
        Member m = new Member();
        m.setName(name);
        m.setEmail(email);
        m.setPhoneNumber("2125551212");
        return m;
    }

    private Member persist(String name, String email) {
        return memberRepository.save(buildMember(name, email));
    }

    // --- findByEmail ---

    @Test
    void findByEmail_whenMemberExists_returnsPresentOptional() {
        persist("Alice", "alice@example.com");

        Optional<Member> result = memberRepository.findByEmail("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice");
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findByEmail_whenEmailNotFound_returnsEmptyOptional() {
        Optional<Member> result = memberRepository.findByEmail("nobody@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_isCaseSensitive() {
        persist("Alice", "alice@example.com");

        Optional<Member> result = memberRepository.findByEmail("ALICE@example.com");

        assertThat(result).isEmpty();
    }

    // --- findAllByOrderByNameAsc ---

    @Test
    void findAllByOrderByNameAsc_returnsMembersAlphabetically() {
        persist("Charlie", "charlie@example.com");
        persist("Alice", "alice@example.com");
        persist("Bob", "bob@example.com");

        List<Member> result = memberRepository.findAllByOrderByNameAsc();

        assertThat(result).extracting(Member::getName)
                .containsExactly("Alice", "Bob", "Charlie");
    }

    @Test
    void findAllByOrderByNameAsc_whenNoMembers_returnsEmptyList() {
        List<Member> result = memberRepository.findAllByOrderByNameAsc();

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByOrderByNameAsc_withSingleMember_returnsThatMember() {
        persist("Alice", "alice@example.com");

        List<Member> result = memberRepository.findAllByOrderByNameAsc();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice");
    }

    // --- save and findById ---

    @Test
    void save_persistsMember_andAssignsId() {
        Member m = buildMember("Dave", "dave@example.com");

        Member saved = memberRepository.save(m);

        assertThat(saved.getId()).isNotNull();
        assertThat(memberRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findById_whenNotFound_returnsEmptyOptional() {
        Optional<Member> result = memberRepository.findById(9999L);

        assertThat(result).isEmpty();
    }
}
