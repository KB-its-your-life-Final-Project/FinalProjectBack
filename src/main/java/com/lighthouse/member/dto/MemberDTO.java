package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lighthouse.member.vo.MemberVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private int id;
    private String name;
    private String email;
    private String pwd;
    private String kakaoId;
    private String googleId;
    private String phone;
    private Integer age;
    private String profileImg;
    private Integer createdType;
    private Date regDate;
    private String regIp;
    private String recentIp;
    private Integer isDisable;
    private Integer isDelete;


    public static MemberDTO of(MemberVO member){
        return MemberDTO.builder()
                .email(member.getEmail())
                .kakaoId(member.getKakaoId())
                .build();
    }

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
        dto.setKakaoId(member.getKakaoId());
        dto.setPhone(member.getPhone());
        dto.setAge(member.getAge());
        dto.setProfileImg(member.getProfileImg());
        dto.setCreatedType(member.getCreatedType());
        dto.setRegDate(member.getRegDate());
        dto.setIsDisable(member.getIsDisable());
        return dto;
    }
}
