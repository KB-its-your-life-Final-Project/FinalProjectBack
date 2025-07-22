package com.lighthouse.member.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lighthouse.security.vo.AuthVO;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MemberVO {
    private Integer id;     //아이디
    private String name;    //이름
    private String email;   //이메일
    private String pwd;     //비밀번호
    private Integer kakaoUserId;    //카카오 식별자
    private String phone;   //폰번
    private Integer age;    //나이
    private String profileImg;    //프로필 경로
    private Integer createdType;    //만들어진 타입
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime regDate;     //회원가입일

    private String regIp;       //작성ip
    private String recentIp;       //최근 접속 ip
    private Integer isDisable;       //활성화여부
    private Integer isDelete;    //삭제여부


    private List<AuthVO> authList;
}
