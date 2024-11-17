package com.example.gunpo.controller.view;

import com.example.gunpo.dto.NewsData;
import com.example.gunpo.service.CrawlingNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CrawlingController {

    private final CrawlingNewsService crawlingNewsService;

    @Autowired
    public CrawlingController(CrawlingNewsService crawlingNewsService) {
        this.crawlingNewsService = crawlingNewsService;
    }

    @GetMapping("/news")
    public String getNewsPage(Model model) {
        List<NewsData> newsList = crawlingNewsService.getAllNews();
        model.addAttribute("newsList", newsList);
        return "newsList";
    }

}
