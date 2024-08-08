package com.tenco.bank.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;

import jakarta.servlet.http.HttpSession;

@Controller // IoC 대상(싱글톤으로 관리)
@RequestMapping("/account")
public class AccountController {

	// 계좌 생성 화면 요청 - DI 처리
	private final HttpSession session;
	private final AccountService accountService;

	// @Autowired
	public AccountController(AccountService accountService, HttpSession session) {
		this.accountService = accountService;
		this.session = session;
	}

	/**
	 * 계좌 생성 페이지 요청 주소 설계 : http://localhost:8080/account/save
	 * 
	 * @return
	 */
	@GetMapping("/save")
	public String savePage() {

		// 1. 인증 검사가 필요(account 전체가 필요함)
		User principal = (User) session.getAttribute("principal");
		if (principal == null) {
			throw new UnAuthorizedException("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED);
		}
		return "account/save";
	}

	/**
	 * 계좌 생성 기능 요청
	 * 주소 설계 : http://localhost:8080/account/save
	 * @return : redirect:/account/list.jsp
	 */
	@PostMapping("/save")
	public String saveProc(SaveDTO dto) {
		// 1. 데이터 추출 (파싱 전략)
		// 2. 인증 검사
		User principal = (User) session.getAttribute("principal");
		if (principal == null) {
			throw new UnAuthorizedException("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED);
		}
		// 3. 유효성 검사
		if (dto.getNumber() == null || dto.getNumber().trim().isEmpty()) {
			throw new DataDeliveryException("계좌 번호를 입력하시오", HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
			throw new DataDeliveryException("계좌 비밀번호를 입력하시오", HttpStatus.BAD_REQUEST);
		}
		if (dto.getBalance() == null || dto.getBalance() <= 0) {
			throw new DataDeliveryException("잔액을 입력하시오", HttpStatus.BAD_REQUEST);
		}
		// 4. 서비스 호출
		accountService.createAccount(dto, principal.getId());
		// 5. 새로운 페이지 이동처리
		return "redirect:/account/list";
	}
	
	/**
	 * 계좌 목록 화면 요청
	 * 주소설계 : http://localhost:8080/account/list, ..../
	 * @return list.jsp
	 */
	@GetMapping({"/list", "/"})
	public String listPage(Model model) {
		// 1. 인증검사
		User principal = (User) session.getAttribute("principal");
		if (principal == null) {
			throw new UnAuthorizedException("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED);
		}
		// 2. 유효성 검사
		// 3. 서비스 호출
		List<Account> accountList = accountService.readAccountListByUserId(principal.getId());
		if (accountList.isEmpty()) {
			model.addAttribute("accountList", null);
		} else {
			model.addAttribute("accountList", accountList);
		}
		return "account/list";
	}

}
