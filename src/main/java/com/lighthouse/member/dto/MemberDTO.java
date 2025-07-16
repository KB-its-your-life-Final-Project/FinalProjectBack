package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lighthouse.security.vo.AuthVO;
import com.lighthouse.member.vo.MemberVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private String username;
    private Date regDate;
    private Date updateDate;
    private List<String> roles;
    private List<String> authList;

    public static MemberDTO of(MemberVO member){
        return MemberDTO.builder()
                .username(member.getUsername())
                .regDate(member.getRegDate()).updateDate(member.getUpdateDate())
                        .authList(member.getAuthList().stream().map(a -> a.getAuth()).toList())
                .build();
    }
}
