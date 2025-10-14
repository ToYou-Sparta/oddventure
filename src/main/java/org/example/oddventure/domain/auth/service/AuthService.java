package org.example.oddventure.domain.auth.service;

import org.example.oddventure.domain.auth.dto.request.LoginRequest;
import org.example.oddventure.domain.auth.dto.request.SignUpRequest;
import org.example.oddventure.domain.auth.dto.request.WithdrawRequest;
import org.example.oddventure.domain.auth.dto.response.LoginResponse;
import org.example.oddventure.domain.auth.dto.response.SignupResponse;

public interface AuthService {

    /**
     * 회원가입
     * @param request 회원가입 요청 정보
     * @return 회원가입 응답 정보
     */
    SignupResponse signup(SignUpRequest request);

    /**
     * 로그인
     * @param request 로그인 요청 정보
     * @return 로그인 응답 정보
     */
    LoginResponse login(LoginRequest request);

    /**
     * 로그아웃
     * @param token Access Token
     */
    void logout(String token);

    /**
     * 회원 탈퇴
     * @param token Access Token
     * @param request 회원 탈퇴 요청 정보 (비밀번호 확인)
     */
    void withdraw(String token, WithdrawRequest request);
}
