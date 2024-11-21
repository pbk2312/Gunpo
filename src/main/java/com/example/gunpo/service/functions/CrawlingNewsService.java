package com.example.gunpo.service.functions;

import com.example.gunpo.constants.NewsConstants;
import com.example.gunpo.constants.NewsErrorMessage;
import com.example.gunpo.dto.NewsData;
import com.example.gunpo.exception.api.NewsDataFetchException;
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
            Document doc = fetchDocument();
            Elements newsElements = doc.select(NewsConstants.NEWS_SECTION_SELECTOR);

            for (Element newsElement : newsElements) {
                NewsData newsData = extractNewsData(newsElement);
                newsList.add(newsData);
            }
        } catch (IOException e) {
            throw new NewsDataFetchException(NewsErrorMessage.FETCH_DOCUMENT_ERROR.getMessage(), e);
        } catch (Exception e) {
            throw new NewsDataFetchException(NewsErrorMessage.PARSE_NEWS_ERROR.getMessage(), e);
        }
        return newsList;
    }

    private Document fetchDocument() throws IOException {
        return Jsoup.connect(NewsConstants.BASE_URL)
                .timeout(5000)
                .userAgent("Mozilla/5.0")
                .get();
    }

    private NewsData extractNewsData(Element newsElement) {
        String imageUrl = newsElement.select(".thumb_img img").attr("src");
        String title = newsElement.select(".title a").text();
        String summary = newsElement.select(".sbody a").text();
        String link = NewsConstants.BASE_URL + newsElement.select(".title a").attr("href");

        return new NewsData(title, imageUrl, summary, link);
    }

}
