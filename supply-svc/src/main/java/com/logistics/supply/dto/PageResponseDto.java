package com.logistics.supply.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class PageResponseDto {

    private boolean hasNext;
    private List<?> data;

    PageResponseDto() {
        this.hasNext = false;
        this.data = Collections.emptyList();
    }

    public static <T extends Page> PageResponseDto wrapResponse(T input) {
        PageResponseDto p = new PageResponseDto();
        p.data = input.getContent();
        p.hasNext = input.hasNext();
        return p;
    }
}
