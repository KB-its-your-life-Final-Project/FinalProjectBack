package com.lighthouse.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;

import java.util.NoSuchElementException;

// @RestControllerAdvice
// @Slf4j
// public class CommonExceptionAdvice {
//     @ExceptionHandler(NoSuchElementException.class)
//     protected ResponseEntity<String> handleNoSuchElementException(NoSuchElementException e){
//         return ResponseEntity
//                 .status(HttpStatus.NOT_FOUND)
//                 .header("Content-Type","text/plain;charset=UTF-8")
//                 .body("해당 ID의 요소가 없습니다.");
//     }
//     @ExceptionHandler(Exception.class)
//     protected ResponseEntity<String> handleException(Exception e) {
//         log.error("서버 오류 발생 {}", e.getMessage());
//         return ResponseEntity
//                 .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                 .header("Content-Type","text/plain;charset=UTF-8")
//                 .body("서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.");
//     }
// }

//우리 프로젝트는 JSON으로 응답해야하기 때문에 수정됨. 별도의 이상 없을 시 위에 주석처리된 내용은 삭제 예정
@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {
    
    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<ApiResponse<String>> handleNoSuchElementException(NoSuchElementException e) {
        log.warn("요청한 데이터를 찾을 수 없습니다: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.ESTATE_NOT_FOUND));
    }
    
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("서버 오류 발생 {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND));
    }
}
