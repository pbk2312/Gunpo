package com.example.gunpo.service;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.domain.Category;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.CannotFindBoardException;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.repository.BoardRepository;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        Long boardId = saveBoardToRepository(board); // 게시물 저장
        saveViewCount(boardId); // 조회수 0부터 저장
        saveImages(images, board); // 이미지 저장

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
    @Transactional
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
    @Transactional
    public BoardDto getPost(Long postId, String accessToken) {
        Member member = memberService.getUserDetails(accessToken); // 사용자 정보 조회
        String userId = member.getId().toString(); // 사용자 ID 가져오기

        redisService.incrementViewCountIfNotExists(postId, userId);
        // 게시물 조회 및 반환
        Board board = findBoard(postId);
        return convertToDto(board);
    }


    private BoardDto convertToDto(Board board) {
        if (board == null) {
            return null;
        }
        int countFromRedis = redisService.getViewCountFromRedis(board.getId());
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
                .viewCount(countFromRedis)
                .imagePaths(imagePaths)  // 이미지 경로 추가
                .category(board.getCategory())
                .build();
    }

    private Board findBoard(Long postId) {
        log.info("게시물 조회 - 게시물 ID: {}", postId);
        return boardRepository.findById(postId).orElseThrow(() -> {
            log.error("CannotFindBoardException 발생 - 존재하는 Board를 찾을 수 없습니다.");
            return new CannotFindBoardException("존재하는 Board를 찾을 수 없습니다.");
        });
    }


    @Override
    @Transactional
    public void updatePost(BoardDto boardDto, List<MultipartFile> newImages, List<String> deleteImages,
                           String accessToken) {

        log.info("게시물 업데이트 요청 - 제목: {}, 내용: {}, 카테고리: {}", boardDto.getTitle(), boardDto.getContent(), boardDto.getCategory());
        // 기존 게시물 조회 및 작성자 권한 확인
        Board existingBoard = findBoardWithAuthorization(boardDto.getId(), accessToken);

        // 삭제할 이미지 처리
        processDeletedImages(deleteImages, existingBoard);

        // 새 이미지 처리
        List<BoardImage> updatedImages = processNewImages(newImages, existingBoard);

        // 게시물 업데이트
        Board updatedBoard = createUpdatedBoard(existingBoard, boardDto, updatedImages);



        saveUpdatedBoard(updatedBoard);
    }

    // 기존 게시물 조회 및 작성자 권한 확인
    private Board findBoardWithAuthorization(Long postId, String accessToken) {
        Board board = findBoard(postId);
        verifyAuthor(board, accessToken);
        return board;
    }

    // 작성자 권한 확인
    private void verifyAuthor(Board board, String accessToken) {
        Member member = memberService.getUserDetails(accessToken);
        if (!board.getAuthor().getEmail().equals(member.getEmail())) {
            log.error("게시물 수정 권한이 없습니다. 작성자: {}, 요청자: {}", board.getAuthor().getEmail(), member.getEmail());
            throw new UnauthorizedException("게시물 수정 권한이 없습니다.");
        }
    }

    // 이미지 삭제 처리
    private void processDeletedImages(List<String> deleteImages, Board board) {
        if (deleteImages != null && !deleteImages.isEmpty()) {
            log.info("이미지 삭제 시작 - 삭제할 이미지 수: {}", deleteImages.size());
            for (String imagePath : deleteImages) {
                imageService.deleteImage(imagePath);
                board.getImages().removeIf(image -> image.getImagePath().equals(imagePath));
                log.info("이미지 삭제 완료 - 경로: {}", imagePath);
            }
        }
    }

    // 새 이미지 처리
    private List<BoardImage> processNewImages(List<MultipartFile> newImages, Board board) {
        // 기존 이미지가 있는 경우 해당 이미지 유지
        List<BoardImage> existingImages = new ArrayList<>(board.getImages());

        // 새로운 이미지가 있으면 추가
        if (newImages != null && !newImages.isEmpty()) {
            log.info("새 이미지 저장 시작");
            List<BoardImage> newSavedImages = imageService.saveImages(newImages, board);
            existingImages.addAll(newSavedImages); // 기존 이미지에 새 이미지를 추가
        }

        return existingImages; // 기존 이미지와 새로운 이미지를 모두 포함한 리스트 반환
    }

    // 업데이트된 게시물 객체 생성
    private Board createUpdatedBoard(Board existingBoard, BoardDto boardDto, List<BoardImage> updatedImages) {
        return Board.builder()
                .id(existingBoard.getId())
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .createdAt(existingBoard.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .viewCount(existingBoard.getViewCount())
                .author(existingBoard.getAuthor())
                .category(boardDto.getCategory())
                .images(updatedImages)
                .build();
    }

    private void saveUpdatedBoard(Board updatedBoard) {
        boardRepository.save(updatedBoard);
        log.info("게시물 업데이트 완료 - 게시물 ID: {}", updatedBoard.getId());
    }

    @Override
    public void deletePost(Long postId, String accessToken) {
        log.info("게시물 삭제 요청 - 게시물 ID: {}", postId);

        // 게시물 조회 후 없으면 예외 발생
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CannotFindBoardException("게시물을 찾을 수 없습니다. ID: " + postId));

        // 작성자 권한 확인 (토큰을 통해 요청자가 작성자인지 확인)
        verifyAuthor(board, accessToken);

        // 게시물 삭제
        boardRepository.delete(board);

        log.info("게시물이 성공적으로 삭제되었습니다. ID: {}", postId);
    }


    private Page<BoardDto> convertToDto(Page<Board> boardPage) {
        log.info("게시물 DTO 변환 시작 - 게시물 수: {}", boardPage.getNumberOfElements());
        return boardPage.map(this::convertToDto);
    }

}
