package com.example.gunpo.controller.view;


import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.exception.board.InvalidPostIdException;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.service.board.BoardService;
import com.example.gunpo.validator.member.AuthenticationValidator;
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
    private final AuthenticationValidator authenticationValidator;

    @GetMapping("/new")
    public String boardCreatePost(
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        try {
            authenticationValidator.validateAccessToken(accessToken);
            return "board/new";
        } catch (UnauthorizedException e) {
            return "board/list";
        }
    }

    @GetMapping("/list")
    public String boardList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<BoardDto> boardPage = getBoardPage(page, size);

        logBoardRequest(page, size, boardPage);

        checkFirstBoardDto(boardPage);

        // model에 페이지 정보와 게시물 목록 추가
        model.addAttribute("boardPage", boardPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", boardPage.getTotalPages());

        return "board/list";
    }

    @GetMapping("/{id}")
    public String getBoardPost(@PathVariable Long id, Model model,
                               @CookieValue(value = "accessToken", required = false) String accessToken) {
        try {
            BoardDto boardDto = boardService.getPost(id, accessToken);
            model.addAttribute("board", boardDto);
            return "board/detail";
        } catch (InvalidPostIdException | CannotFindBoardException e) {
            return "board/list";
        }
    }

    @GetMapping("/update/{id}")
    public String updateBoardPage(@PathVariable Long id, Model model,
                                  @CookieValue(value = "accessToken", required = false) String accessToken
    ) {

        BoardDto boardDto = boardService.getPost(id, accessToken);

        if (boardDto == null) {

            return "redirect:/board/list";
        }

        model.addAttribute("board", boardDto);

        return "board/update";


    }

    private Page<BoardDto> getBoardPage(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return boardService.getPosts(pageable);
    }

    private void logBoardRequest(int page, int size, Page<BoardDto> boardPage) {
        log.info("게시판 목록 요청: 현재 페이지 = {}, 페이지당 게시물 수 = {}, 총 게시물 수 = {}", page, size, boardPage.getTotalElements());
    }

    private void checkFirstBoardDto(Page<BoardDto> boardPage) {
        if (boardPage.hasContent()) {
            BoardDto firstBoardDto = boardPage.getContent().get(0);
            log.info("첫 번째 게시물 DTO: {}", firstBoardDto);
        } else {
            log.info("게시판에 게시물이 없습니다.");
        }
    }

}
