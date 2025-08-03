package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lighthouse.member.entity.Member;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDTO {
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

    //클라이언트에 보낼 수 있는 데이터
    public static MemberResponseDTO toUser(Member member) {
        return MemberResponseDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .kakaoId(member.getKakaoId())
                .googleId(member.getGoogleId())
                .phone(member.getPhone())
                .age(member.getAge())
                .profileImg(member.getProfileImg())
                .createdType(member.getCreatedType())
                .regDate(member.getRegDate())
                .build();
    }

    // 관리자에게 보내는 데이터
    public static MemberResponseDTO toAdmin(Member member) {
        return MemberResponseDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .kakaoId(member.getKakaoId())
                .googleId(member.getGoogleId())
                .phone(member.getPhone())
                .age(member.getAge())
                .profileImg(member.getProfileImg())
                .createdType(member.getCreatedType())
                .regDate(member.getRegDate())
                .regIp(member.getRegIp())
                .recentIp(member.getRecentIp())
                .isDisable(member.getIsDisable())
                .isDelete(member.getIsDelete())
                .build();
    }
}
