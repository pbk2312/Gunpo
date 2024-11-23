package com.example.gunpo.constants.errorMessage;

public enum BoardErrorMessage {
    MISSING_TITLE_AND_CONTENT("게시물 제목 및 내용은 필수입니다."),
    UNAUTHORIZED_MODIFY("게시물 수정,삭제 권한이 없습니다."),
    UNAUTHORIZED_MODIFY_COMMENT("댓글 수정,삭제 권한이 없습니다"),
    INVALID_POST_ID("유효하지 않은 게시물 ID입니다."),
    POST_NOT_FOUND("게시물을 찾을 수 없습니다."),
    INVALID_PAGEABLE("페이지 번호와 크기는 양수여야 합니다."),
    IMAGE_NOT_NULL("이미지 객체는 null일 수 없습니다."),
    COMMENT_NOT_NULL("댓글 객체는 null일 수 없습니다."),
    COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.");


    private final String message;

    BoardErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
