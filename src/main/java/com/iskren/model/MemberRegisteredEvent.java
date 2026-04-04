package com.iskren.model;

public class MemberRegisteredEvent {

    private final Member member;

    public MemberRegisteredEvent(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }
}
