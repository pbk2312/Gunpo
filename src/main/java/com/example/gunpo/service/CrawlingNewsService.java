package com.example.gunpo.service;

import com.example.gunpo.dto.NewsData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrawlingNewsService {

    public List<NewsData> getAllNews() {
        List<NewsData> newsList = new ArrayList<>();

        try {
            // 웹 페이지 연결
            Document doc = Jsoup.connect("https://www.mediagunpo.co.kr").get();

            // 메인 뉴스 섹션 선택
            Elements newsElements = doc.select("#main_news121 .news_box");

            // 각 뉴스 아이템에서 데이터 추출
            for (Element newsElement : newsElements) {
                // 이미지 URL 추출
                String imageUrl = newsElement.select(".thumb_img img").attr("src");

                // 뉴스 제목 추출
                String title = newsElement.select(".title a").text();

                // 본문 내용의 일부 추출
                String summary = newsElement.select(".sbody a").text();

                // 링크 추출
                String link = "https://www.mediagunpo.co.kr" + newsElement.select(".title a").attr("href");

                // 추출한 정보를 NewsData 객체에 담아 리스트에 추가
                newsList.add(new NewsData(title, imageUrl, summary, link));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsList;
    }


}
