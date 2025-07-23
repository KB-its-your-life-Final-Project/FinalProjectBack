package com.lighthouse.security.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;

import javax.servlet.http.HttpServletRequest;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginEmailDTO {
    private String email;
    private String password;

    public static LoginEmailDTO of(HttpServletRequest request){
        ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(request.getInputStream(), LoginEmailDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("email 또는 password가 없습니다.");
        }
    }
}
