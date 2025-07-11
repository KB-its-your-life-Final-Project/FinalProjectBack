package com.lighthouse.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lighthouse.security.vo.CustomUser;
import com.lighthouse.security.dto.AuthResultDTO;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.security.util.JsonResponse;
import com.lighthouse.security.util.JwtProcessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProcessor jwtProcessor;

    private AuthResultDTO makeAuthResult(CustomUser user){
        String username = user.getUsername();

        String token = jwtProcessor.generateToken(username);
        return new AuthResultDTO(token, MemberDTO.of(user.getMember()));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomUser user = (CustomUser)authentication.getPrincipal();

        AuthResultDTO authResult = makeAuthResult(user);
        JsonResponse.send(response, authResult);

    }
}
