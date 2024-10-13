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
    public String boardCreatePost(
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        if (isAccessTokenMissing(accessToken)) {
            log.warn("Access token is missing.");
            return redirectToLogin();  // 로그인 페이지로 리다이렉션
        }

        return "board/new";
    }

    @GetMapping("/list")
    public String boardList(
            @RequestParam(defaultValue = "0") int page, // 기본 페이지 0
            @RequestParam(defaultValue = "10") int size, // 한 페이지에 10개의 게시물
            Model model) {

        Page<BoardDto> boardPage = getBoardPage(page, size);

        logBoardRequest(page, size, boardPage);

        checkFirstBoardDto(boardPage);

        // model에 페이지 정보와 게시물 목록 추가
        model.addAttribute("boardPage", boardPage);
        model.addAttribute("currentPage", page); // 현재 페이지 번호
        model.addAttribute("totalPages", boardPage.getTotalPages()); // 총 페이지 수

        return "board/list";  // 반환할 HTML 페이지
    }

    @GetMapping("/{id}")
    public String getBoardPost(@PathVariable Long id, Model model,
                               @CookieValue(value = "accessToken", required = false) String accessToken) {
        // 게시물 ID를 사용하여 게시물 정보를 가져옵니다.
        BoardDto boardDto = boardService.getPost(id,accessToken);

        if (boardDto == null) {
            // 게시물이 존재하지 않을 경우 적절한 처리
            return "redirect:/board/list"; // 목록 페이지로 리다이렉트
        }

        // 모델에 게시물 정보를 추가
        model.addAttribute("board", boardDto);

        return "board/detail"; // 게시물 상세 페이지로 반환
    }


    private boolean isAccessTokenMissing(String accessToken) {
        return accessToken == null;
    }

    private String redirectToLogin() {
        return "redirect:/login";
    }

    private Page<BoardDto> getBoardPage(int page, int size) {
        // Pageable 객체 생성 (page는 0부터 시작)
        PageRequest pageable = PageRequest.of(page, size);
        return boardService.getPosts(pageable);
    }

    private void logBoardRequest(int page, int size, Page<BoardDto> boardPage) {
        // 로그 출력: 현재 페이지, 페이지당 게시물 수, 총 게시물 수
        log.info("게시판 목록 요청: 현재 페이지 = {}, 페이지당 게시물 수 = {}, 총 게시물 수 = {}", page, size, boardPage.getTotalElements());
    }

    private void checkFirstBoardDto(Page<BoardDto> boardPage) {
        // 첫 번째 DTO가 있는지 확인하고 출력
        if (boardPage.hasContent()) {
            BoardDto firstBoardDto = boardPage.getContent().get(0);
            log.info("첫 번째 게시물 DTO: {}", firstBoardDto);
        } else {
            log.info("게시판에 게시물이 없습니다.");
        }
    }


}
