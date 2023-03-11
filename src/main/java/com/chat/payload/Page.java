package com.chat.payload;

import lombok.Data;

@Data
public class Page {
    private long totalElements;
    private int numberOfElements;
    private int number;
    private int totalPages;

    public static Page toPage(org.springframework.data.domain.Page page) {
        com.chat.payload.Page _page = new com.chat.payload.Page();
        _page.setNumber(page.getNumber());
        _page.setNumberOfElements(page.getNumberOfElements());
        _page.setTotalElements(page.getTotalElements());
        _page.setTotalPages(page.getTotalPages());
        return _page;
    }
}
