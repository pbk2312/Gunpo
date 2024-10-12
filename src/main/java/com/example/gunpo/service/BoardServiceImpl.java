package com.example.gunpo.service;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.domain.Category;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.CannotFindBoardException;
import com.example.gunpo.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final MemberService memberService;
    private final RedisService redisService;
    private final ImageService imageService;

    // 게시물 작성
    @Override
    public Long createPost(BoardDto boardDto, String accessToken, List<MultipartFile> images) {
        log.info("게시물 작성 요청 - 제목: {}, 사용자 토큰: {}", boardDto.getTitle(), accessToken);
        return saveBoard(boardDto, accessToken, images);
    }

    public Long saveBoard(BoardDto boardDto, String accessToken, List<MultipartFile> images) {
        Member member = getUserDetails(accessToken); // 사용자 정보 조회
        Board board = createBoardFromDto(boardDto, member); // 게시물 객체 생성

        saveImages(images, board); // 이미지 저장
        Long boardId = saveBoardToRepository(board); // 게시물 저장
        saveViewCount(boardId); // 조회수 저장

        log.info("게시물 저장 완료 - 게시물 ID: {}", boardId);
        return boardId; // 게시물 ID 반환
    }

    private Member getUserDetails(String accessToken) {
        return memberService.getUserDetails(accessToken); // 사용자 정보 조회
    }

    private void saveImages(List<MultipartFile> images, Board board) {
        imageService.saveImages(images, board); // 이미지 저장
    }


    private void saveViewCount(Long boardId) {
        redisService.saveViewCountToRedis(boardId); // 조회수 저장
    }


    private static Board createBoardFromDto(BoardDto boardDto, Member member) {
        return Board.builder()
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .viewCount(0)  // 초기 조회수 설정
                .author(member)
                .category(Category.valueOf(boardDto.getCategory().name()))
                .build();
    }

    private Long saveBoardToRepository(Board board) {
        return boardRepository.save(board).getId();
    }

    @Override
    public Page<BoardDto> getPosts(Pageable pageable) {
        return convertToDto(fetchPosts(pageable));
    }

    private Page<Board> fetchPosts(Pageable pageable) {
        return descendingAndGetboardPage(pageable);
    }

    private Page<Board> descendingAndGetboardPage(Pageable pageable) {
        Pageable sortedByCreatedAtDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        log.info("게시물 정렬 기준 설정 및 조회 시작");
        return boardRepository.findAll(sortedByCreatedAtDesc);
    }

    // 게시물 가져오기
    @Override
    public BoardDto getPost(Long postId) {
        Board board = findBoard(postId);
        return convertToDto(board);
    }

    private BoardDto convertToDto(Board board) {
        if (board == null) return null;

        List<String> imagePaths = board.getImages().stream()
                .map(BoardImage::getImagePath)
                .collect(Collectors.toList());

        return BoardDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorId(board.getAuthor().getId())
                .nickname(board.getAuthor().getNickname())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .viewCount(board.getViewCount())
                .imagePaths(imagePaths)  // 이미지 경로 추가
                .category(board.getCategory())
                .build();
    }
    // Board 최신 정보로 가져오게 정렬 기준 확립 및 가져오기
    // 게시물 Repository 에서 가져오기

    private Board findBoard(Long postId) {
        log.info("게시물 조회 - 게시물 ID: {}", postId);
        return boardRepository.findById(postId).orElseThrow(() -> {
            log.error("CannotFindBoardException 발생 - 존재하는 Board를 찾을 수 없습니다.");
            return new CannotFindBoardException("존재하는 Board를 찾을 수 없습니다.");
        });
    }

    @Override
    public void updatePost(BoardDto boardDto) {
        log.info("게시물 업데이트 요청 - 제목: {}", boardDto.getTitle());
        // Update logic can be implemented here
    }

    @Override
    public void deletePost(Long postId) {
        log.info("게시물 삭제 요청 - 게시물 ID: {}", postId);
        // Delete logic can be implemented here
    }

    private Page<BoardDto> convertToDto(Page<Board> boardPage) {
        log.info("게시물 DTO 변환 시작 - 게시물 수: {}", boardPage.getNumberOfElements());
        return boardPage.map(this::convertToDto);
    }

}