package com.lighthouse.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {
    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<String> handleNoSuchElementException(NoSuchElementException e){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .header("Content-Type","text/plain;charset=UTF-8")
                .body("해당 ID의 요소가 없습니다.");
    }
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<String> handleException(Exception e) {

        StackTraceElement[] stackTrace = e.getStackTrace();
        String functionName = "unknown";
        
        if (stackTrace.length > 0) {
            functionName = stackTrace[0].getMethodName();
            log.error("Failed to run {}: {}", functionName, e.getMessage());
        }
        else {
            log.error("서버 오류 발생 {}", e.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type","text/plain;charset=UTF-8")
                .body("서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.");
    }
}
