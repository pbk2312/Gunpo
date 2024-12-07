package com.example.gunpo.controller.view;


import com.example.gunpo.domain.board.Comment;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.board.BoardDto;
import com.example.gunpo.dto.board.CommentDto;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.exception.board.InvalidPostIdException;
import com.example.gunpo.exception.location.NeighborhoodVerificationException;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.service.board.BoardService;
import com.example.gunpo.service.board.CommentService;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.validator.member.AuthenticationValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/board")
public class BoardViewController {

    private final BoardService boardService;
    private final AuthenticationValidator authenticationValidator;
    private final AuthenticationService authenticationService;
    private final CommentService commentService;

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
            Model model,
            RedirectAttributes redirectAttributes) {
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
            // 로그인하지 않은 경우 메시지 추가 후 /login으로 리디렉션
            redirectAttributes.addFlashAttribute("errorMessage", "지역 커뮤니티 게시판은 로그인이 필요합니다.");
            return "redirect:/login";
        } catch (NeighborhoodVerificationException e) {
            // 동네 인증이 되지 않은 경우 메시지 추가 후 /neighborhoodVerification으로 리디렉션
            redirectAttributes.addFlashAttribute("errorMessage", "지역 커뮤니티 게시판은 동네 인증이 필요합니다.");
            return "redirect:/neighborhoodVerification";
        }
    }

    @GetMapping("/{id}")
    public String getBoardPost(@PathVariable Long id, Model model,
                               @CookieValue(value = "accessToken", required = false) String accessToken) {
        try {
            // 게시물 정보 가져오기
            BoardDto boardDto = boardService.getPost(id, accessToken);
            model.addAttribute("board", boardDto);

            List<Comment> comments = commentService.getComments(id);

            // 댓글을 CommentDto 리스트로 변환
            List<CommentDto> commentDtos = comments.stream()
                    .map(CommentDto::from)
                    .toList();
            // 댓글을 모델에 추가
            model.addAttribute("comments", commentDtos);

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

    // 본인 작성 글 보기
    @GetMapping("/memberPost")
    public String memberPostListPage(@CookieValue(value = "accessToken", required = false) String accessToken,
                                     Model model) {

        // 로그인한 사용자의 게시물 목록 조회
        List<BoardDto> listByMember = boardService.getPostListByMember(accessToken);

        model.addAttribute("boardList", listByMember);
        return "board/list_member";
    }

    // 특정 닉네임 사용자의 게시물 보기
    @GetMapping("/memberPost/{nickname}")
    public String memberPostListPageByNickname(@PathVariable("nickname") String nickname, // 닉네임을 URL Path로 받음
                                               @CookieValue(value = "accessToken", required = false) String accessToken,
                                               Model model) {

        // 닉네임을 통해 다른 사용자가 작성한 게시물 조회
        List<BoardDto> listByMember = boardService.getPostListByAuthorName(nickname);

        // 게시물 목록을 모델에 추가
        model.addAttribute("boardList", listByMember);
        model.addAttribute("nickname", nickname); // 조회한 닉네임도 모델에 추가 (뷰에서 사용)

        return "board/list_member"; // 뷰 이름 반환
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
