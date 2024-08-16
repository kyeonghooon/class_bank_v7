package com.tenco.bank.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.tenco.bank.dto.KakaoProfile;
import com.tenco.bank.dto.OAuthToken;
import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.UserService;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final HttpSession session;

	@Value("${tenco.oauth-password}")
	private String tencoPassword;

	/**
	 * 회원 가입 페이지 요청
	 */
	@GetMapping("/sign-up")
	public String signUpPage() {
		return "user/signUp";
	}

	/**
	 * 회원 가입 로직 처리 요청
	 */
	@PostMapping("/sign-up")
	public String signUpProc(SignUpDTO dto) {
		if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		if (dto.getFullname() == null || dto.getFullname().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_FULLNAME, HttpStatus.BAD_REQUEST);
		}
		userService.createUser(dto);
		return "redirect:/user/sign-in";
	}

	/**
	 * 로그인 화면 요청
	 */
	@GetMapping("/sign-in")
	public String signInPage() {
		return "user/signIn";
	}

	/**
	 * 로그인 요청 처리
	 */
	@PostMapping("/sign-in")
	public String signProc(SignInDTO dto) {
		if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		User principal = userService.readUser(dto);
		session.setAttribute(Define.PRINCIPAL, principal);
		return "redirect:/account/list";
	}

	/**
	 * 로그아웃 처리
	 */
	@GetMapping("/logout")
	public String logout() {
		session.invalidate();
		return "redirect:/user/sign-in";
	}

	@GetMapping("/kakao")
	public String kakao(@RequestParam(name = "code") String code) {
		System.out.println("code : " + code);

		// POST - 카카오 토큰 요청
		// Header, body 구성
		RestTemplate rt1 = new RestTemplate();
		// 헤더 구성
		HttpHeaders header1 = new HttpHeaders();
		header1.add("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
		// 바디 구성
		MultiValueMap<String, String> params1 = new LinkedMultiValueMap<>();
		params1.add("grant_type", "authorization_code");
		params1.add("client_id", "5caa82add2f3068772bb57b15970151d");
		params1.add("redirect_uri", "http://localhost:8080/user/kakao");
		params1.add("code", code);

		// 헤더 + 바디 결합
		HttpEntity<MultiValueMap<String, String>> reqkakaoToken = new HttpEntity<>(params1, header1);

		// 통신 요청
		ResponseEntity<OAuthToken> response1 = rt1.exchange("https://kauth.kakao.com/oauth/token", HttpMethod.POST, reqkakaoToken, OAuthToken.class);
		System.out.println("response : " + response1);

		// 카카오 리소스서버 사용자 정보 가져오기
		RestTemplate rt2 = new RestTemplate();
		// 헤더
		HttpHeaders headers2 = new HttpHeaders();
		headers2.add("Authorization", "Bearer " + response1.getBody().getAccessToken());
		headers2.add("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
		// 본문 x

		// HTTP Entity 만들기
		HttpEntity<MultiValueMap<String, String>> reqkakaoInfo = new HttpEntity<>(headers2);
		ResponseEntity<KakaoProfile> response2 = rt2.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, reqkakaoInfo, KakaoProfile.class);
		KakaoProfile kakaoProfile = response2.getBody();
		// ------------ 카카오 사용자 정보 응답 완료 -------------

		// 최초 사용자라면 자동 회원 가입 처리 (우리 서버)
		// 회원가입 이력이 있는 사용자라면 바로세션 처리 (우리 서버)
		// 사전기반 --> 소셜 사용자는 비밀번호를 입력하는가? 안하는가?
		// 우리서버에 회원가입시에 --> password -> not null (무조건 만들어 넣어야 함 DB 정책)

		// 1. 회원가입 데이터 새성
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.username(kakaoProfile.getProperties().getNickname() + "_" + kakaoProfile.getId())
			.fullname("OAuth_" + kakaoProfile.getProperties().getNickname())
			.password(tencoPassword)
			.build();

		// 2. 우리사이트 최초 소셜 사용자 인지 판별
		User oldUser = userService.readUser(signUpDTO.getUsername());
		if (oldUser == null) {
			// 사용자가 최초 소셜 로그인 사용자 임
			userService.createUser(signUpDTO);
			oldUser = User.builder()
					.username(signUpDTO.getUsername())
					.fullname(signUpDTO.getFullname())
					.originFileName(kakaoProfile.getProperties().getProfileImage())
					.build();
		}
		
		// 자동 로그인 처리
		session.setAttribute(Define.PRINCIPAL, oldUser);
		return "redirect:/account/list";
	}

}
