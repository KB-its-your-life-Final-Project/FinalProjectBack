package com.lighthouse.youthProgram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthProgramDTO {
    private Long id;               // PK
    private String bbsSn;          // 게시판 일련번호
    private String pstSn;          // 게시물 일련번호
    private String pstSeSn;        // 게시물 구분 일련번호
    private String pstSeNm;        // 게시물 유형 이름 (취업지원, 직업훈련, 대외활동, 청년뉴스, 청년인터뷰, 정책소식, 소식, 쉬운 용어 풀이, 기타 등)
    private String pstTtl;         // 게시물 제목
    private String pstWholCn;      // 게시물 전체 내용 (링크 포함)
    private String pstInqCnt;      // 게시물 조회수
    private String pstUrlAddr;     // 게시물 URL 주소 (nullable)
    private String atchFile;       // 첨부 파일 (Base64)
    private String frstRgtrNm;     // 최초 등록자 명
    private String frstRegDt;      // 최초 등록 일시
    private String lastMdfrNm;     // 최종 수정자 명
    private String lastMdfcnDt;    // 최종 수정 일시
}
