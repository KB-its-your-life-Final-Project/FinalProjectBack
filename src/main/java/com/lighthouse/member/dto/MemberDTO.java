package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lighthouse.security.vo.AuthVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lighthouse.member.vo.MemberVO;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
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

    //클라이언트에 보낼 수 있는 데이터
    public static MemberDTO toUser(MemberVO member) {
        MemberDTO dto = new MemberDTO();
        dto.setId(member.getId());
        dto.setName(member.getName());
        dto.setEmail(member.getEmail());
        dto.setPhone(member.getPhone());
        dto.setAge(member.getAge());
        dto.setProfileImg(member.getProfileImg());
        dto.setRegDate(member.getRegDate());
        dto.setIsDisable(member.getIsDisable());
        return dto;
    }

    // 관리자에게 보내는 데이터
    public static MemberDTO toAdmin(MemberVO member) {
        MemberDTO dto = new MemberDTO();
        dto.setId(member.getId());
        dto.setName(member.getName());
        dto.setEmail(member.getEmail());
        dto.setKakaoUserId(member.getKakaoUserId());
        dto.setPhone(member.getPhone());
        dto.setAge(member.getAge());
        dto.setProfileImg(member.getProfileImg());
        dto.setCreatedType(member.getCreatedType());
        dto.setRegDate(member.getRegDate());
        dto.setIsDisable(member.getIsDisable());
        return dto;
    }
}
