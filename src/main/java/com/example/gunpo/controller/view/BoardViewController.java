package com.example.gunpo.controller.view;


import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.exception.board.InvalidPostIdException;
import com.example.gunpo.exception.location.NeighborhoodVerificationException;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.service.board.BoardService;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.validator.member.AuthenticationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final AuthenticationService authenticationService;

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
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model) {
        try {
            // 사용자가 로그인된 상태인지 확인
            Member member = authenticationService.getUserDetails(accessToken);
            authenticationValidator.validateNeighborhoodVerification(member);

            // 게시물 페이지 불러오기
            Page<BoardDto> boardPage = getBoardPage(page, size);

            logBoardRequest(page, size, boardPage);

            checkFirstBoardDto(boardPage);

            // model에 페이지 정보와 게시물 목록 추가
            model.addAttribute("boardPage", boardPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", boardPage.getTotalPages());

            return "board/list";
        } catch (UnauthorizedException e) {
            // 사용자가 로그인하지 않은 경우 /login으로 리디렉션
            return "redirect:/login";
        } catch (NeighborhoodVerificationException e) {
            // 동네 인증이 되지 않은 경우 /location으로 리디렉션
            return "redirect:/neighborhoodVerification";
        }
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
        try {
            BoardDto boardDto = boardService.getPost(id, accessToken);
            model.addAttribute("board", boardDto);
            return "board/update";
        } catch (InvalidPostIdException e) {
            return "board/list";
        } catch (Exception e) {
            return "error";
        }
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
