package com.iskren.config;

import com.iskren.model.Member;
import com.iskren.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;

    public DataSeeder(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) {
        if (memberRepository.count() == 0) {
            Member member = new Member();
            member.setName("John Smith");
            member.setEmail("john.smith@mailinator.com");
            member.setPhoneNumber("2125551212");
            memberRepository.save(member);
        }
    }
}
