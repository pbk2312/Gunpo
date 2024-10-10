package com.example.gunpo.controller.view;


import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.service.BoardService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/board")
public class BoardViewController {

    private final BoardService boardService;
    @GetMapping("/new")
    public String boardCreatePost(@CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response,
                                  @ModelAttribute BoardDto boardDto) {
        if (accessToken == null) {
            log.warn("Access token is missing.");
            return "redirect:/login";  // 로그인 페이지로 리다이렉션
        }


        return "board/new";
    }


    @GetMapping("/list")
    public String boardList(@RequestParam(defaultValue = "0") int page, // 기본 페이지 0
                            @RequestParam(defaultValue = "10") int size, // 한 페이지에 10개의 게시물
                            Model model) {
        // Pageable 객체 생성 (page는 0부터 시작)
        PageRequest pageable = PageRequest.of(page, size);

        // 게시물 가져오기
        Page<BoardDto> boardPage = boardService.getPosts(pageable);

        // 로그 출력: 현재 페이지, 페이지당 게시물 수, 총 게시물 수
        log.info("게시판 목록 요청: 현재 페이지 = {}, 페이지당 게시물 수 = {}, 총 게시물 수 = {}", page, size, boardPage.getTotalElements());

        // 첫 번째 DTO가 있는지 확인하고 출력
        if (boardPage.hasContent()) {
            BoardDto firstBoardDto = boardPage.getContent().get(0);
            log.info("첫 번째 게시물 DTO: {}", firstBoardDto);
        } else {
            log.info("게시판에 게시물이 없습니다.");
        }

        // model에 페이지 정보와 게시물 목록 추가
        model.addAttribute("boardPage", boardPage);
        model.addAttribute("currentPage", page); // 현재 페이지 번호
        model.addAttribute("totalPages", boardPage.getTotalPages()); // 총 페이지 수

        return "board/list";  // 반환할 HTML 페이지
    }

}
