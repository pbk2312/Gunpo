package com.example.gunpo.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public  class NewsData {
    private final String title;
    private final String imageUrl;
    private final String summary;
    private final String link;

    public NewsData(String title, String imageUrl, String summary, String link) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.summary = summary;
        this.link = link;
    }

}
