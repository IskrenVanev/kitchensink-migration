package com.iskren.dto;

import com.iskren.model.Member;
import java.util.List;

public class MemberSearchResponseDTO {

    private List<Member> data;
    private long total;

    public MemberSearchResponseDTO(List<Member> data, long total) {
        this.data = data;
        this.total = total;
    }

    public List<Member> getData() {
        return data;
    }

    public long getTotal() {
        return total;
    }
}