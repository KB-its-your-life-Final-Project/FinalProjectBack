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

import com.lighthouse.security.util.JwtCookieManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProcessor jwtProcessor;
    private final JwtCookieManager jwtCookieManager;

    private AuthResultDTO makeAuthResult(CustomUser user){
        String username = user.getUsername();

        String token = jwtProcessor.generateAccessToken(username);
        return new AuthResultDTO(token, MemberDTO.of(user.getMember()));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication auth) throws IOException {
        CustomUser user = (CustomUser) auth.getPrincipal();

        jwtCookieManager.setTokensToCookies(resp, user.getUsername());

        // 사용자 정보 + access token (body에도 보낼 수 있으나, JWT는 쿠키에만 포함해도 됨)
        AuthResultDTO authResult = makeAuthResult(user);
        JsonResponse.send(resp, authResult);

    }
}
