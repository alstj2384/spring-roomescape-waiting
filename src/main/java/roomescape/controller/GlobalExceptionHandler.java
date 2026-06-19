package roomescape.controller;

import common.exception.ErrorCode;
import common.exception.ErrorResponse;
import common.exception.RoomEscapeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    public static final String UNEXPECTED_ERROR = "예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    public static final String DATABASE_ERROR = "데이터 베이스 관련 오류가 발생했습니다. 관리자에게 문의해주세요";

    @ExceptionHandler(RoomEscapeException.class)
    public ResponseEntity<ErrorResponse> roomEscapeExceptionHandle(RoomEscapeException e) {
        log.info("도메인 관련 오류가 발생했습니다.", e);
        return ResponseEntity.status(mapHttpStatus(e.getErrorCode()))
                .body(ErrorResponse.create(e.getMessage()));
    }

    private HttpStatus mapHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_NAME_LENGTH, INVALID_THEME_NAME_LENGTH, INVALID_THUMBNAIL_URL -> HttpStatus.BAD_REQUEST;
            case THEME_NOT_FOUND, RESERVATION_NOT_FOUND, RESERVATION_TIME_NOT_FOUND, SLOT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_RESERVATION, RESERVATION_TIME_IN_USE, THEME_IN_USE -> HttpStatus.CONFLICT;
            case PAST_DATE_NOT_ALLOWED, PAST_RESERVATION_NOT_ALLOWED -> HttpStatus.UNPROCESSABLE_ENTITY;
            case INVALID_RANK_VALUE -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse methodArgumentNotValidExceptionHandle(MethodArgumentNotValidException e) {
        log.info("입력 값 검증 중 예외가 발생했습니다.", e);
        return ErrorResponse.create(e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse httpRequestMethodNotSupportedExceptionHandle(HttpRequestMethodNotSupportedException e) {
        log.info("지원하지 않는 HTTP 메서드입니다.", e);
        return ErrorResponse.create(e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse dataAccessExceptionHandle(DataAccessException e) {
        log.error("데이터베이스 관련 오류가 발생했습니다", e);
        return ErrorResponse.create(DATABASE_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse exceptionHandle(Exception e) {
        log.error("서버 내부 오류입니다.", e);
        return ErrorResponse.create(UNEXPECTED_ERROR);
    }
}
