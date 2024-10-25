package com.example.gunpo.config;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

public class WebCrawler {

    public void crawl() {
        try {
            // 1. 웹 페이지 연결
            Document doc = Jsoup.connect("https://www.mediagunpo.co.kr/").get();

            // 2. 페이지의 뉴스 제목과 본문 선택
            Elements newsItems = doc.select("dd.news1, dd.news2"); // news1과 news2 클래스를 포함한 dd 태그 선택

            // 3. 요소에서 제목과 본문 텍스트 추출
            for (Element item : newsItems) {
                // 제목 추출
                String title = item.select(".title a").text();
                // 본문 추출
                String body = item.select(".sbody a").text();

                System.out.println("Title: " + title);
                System.out.println("Body: " + body);
                System.out.println("----------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
