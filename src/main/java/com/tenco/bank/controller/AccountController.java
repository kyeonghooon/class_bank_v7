package com.tenco.bank.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.HistoryAccount;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;
import com.tenco.bank.utils.Define;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

	// 계좌 생성 화면 요청 - DI 처리
	@Autowired
	private final AccountService accountService;

	/**
	 * 계좌 생성 페이지 요청
	 */
	@GetMapping("/save")
	public String savePage() {
		return "account/save";
	}

	/**
	 * 계좌 생성 기능 요청
	 * 
	 * @return : /account/list
	 */
	@PostMapping("/save")
	public String saveProc(SaveDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {
		if (dto.getNumber() == null || dto.getNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		if (dto.getBalance() == null || dto.getBalance() <= 0) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		accountService.createAccount(dto, principal.getId());
		return "redirect:/account/list";
	}

	/**
	 * 계좌 목록 화면 요청
	 * 
	 * @return list.jsp
	 */
	@GetMapping({ "/list", "/" })
	public String listPage(Model model, @SessionAttribute(Define.PRINCIPAL) User principal) {
		List<Account> accountList = accountService.readAccountListByUserId(principal.getId());
		if (accountList.isEmpty()) {
			model.addAttribute("accountList", null);
		} else {
			model.addAttribute("accountList", accountList);
		}
		return "account/list";
	}

	/**
	 * 출금 페이지 요청
	 * 
	 * @return withdrawal.jsp
	 */
	@GetMapping("/withdrawal")
	public String withdrawalPage() {
		return "account/withdrawal";
	}

	/**
	 * 출금 기능 요청
	 * 
	 * @param dto
	 * @return
	 */
	@PostMapping("/withdrawal")
	public String withdrawalProc(WithdrawalDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {
		// 유효성 검사 (자바 코드를 개발) --> 스프링 부트 @Valid 라이브러리가 존재
		if (dto.getAmount() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getAmount().longValue() <= 0) {
			throw new DataDeliveryException(Define.W_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getWAccountNumber() == null || dto.getWAccountNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		if (dto.getWAccountPassword() == null || dto.getWAccountPassword().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		accountService.updateAccountWithdraw(dto, principal.getId());
		return "redirect:/account/list";
	}

	/**
	 * 입금 페이지 요청
	 * 
	 * @return deposit.jsp
	 */
	@GetMapping("/deposit")
	public String depositPage() {
		return "account/deposit";
	}

	/**
	 * 입금 기능 요청
	 * 
	 * @return
	 */
	@PostMapping("/deposit")
	public String depositProc(DepositDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {
		if (dto.getAmount() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getAmount().longValue() <= 0) {
			throw new DataDeliveryException(Define.D_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getDAccountNumber() == null || dto.getDAccountNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		accountService.updateAccountDeposit(dto, principal.getId());
		return "redirect:/account/list";
	}

	// 이체 페이지 요청
	@GetMapping("/transfer")
	public String transferPage() {
		return "account/transfer";
	}

	// 이체 기능 처리 요청
	@PostMapping("/transfer")
	public String transferProc(TransferDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {
		// 유효성 검사 (자바 코드를 개발) --> 스프링 부트 @Valid 라이브러리가 존재
		if (dto.getAmount() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getAmount().longValue() <= 0) {
			throw new DataDeliveryException(Define.W_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getWAccountNumber() == null || dto.getWAccountNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		if (dto.getDAccountNumber() == null || dto.getDAccountNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		accountService.updateAccountTransfer(dto, principal.getId());
		return "redirect:/account/list";
	}

	/**
	 * 계좌 상세 보기 페이지 주소 설계 : localhost:8080/account/detail/1?type=all, deposit, withdrawal
	 * 
	 * @return
	 */
	@GetMapping("/detail/{accountId}")
	public String detail(@PathVariable(name = "accountId") Integer accountId, //
			@RequestParam(required = false, name = "type") String type, @RequestParam(defaultValue = "1", name = "page") int page, Model model) {
		// 유효성 검사
		List<String> validTypes = Arrays.asList("all", "deposit", "withdrawal");
		if (!validTypes.contains(type)) {
			throw new DataDeliveryException("유효하지 않은 접근 입니다.", HttpStatus.BAD_REQUEST);
		}
		Account account = accountService.readAccountById(accountId);
		int pageSize = 2; // 한페이지에 2개
		int offset = (page - 1) * pageSize;
		int totalHistories = accountService.countHistoryByAccountIdAndType(type, accountId);
		int totalPage = (int) Math.ceil((double) totalHistories / pageSize);
		int pageBlock = 5;
		int tenCount = (int) Math.ceil(((double) page / pageBlock) - 1) * pageBlock;
		int startPage = tenCount + 1;
		int endPage = (tenCount + 5) > totalPage ? totalPage : (tenCount + pageBlock);
		List<HistoryAccount> historyList = accountService.readHistoryByAccountId(type, accountId, offset, pageSize);
		model.addAttribute("type", type);
		model.addAttribute("currentPage", page);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("account", account);
		model.addAttribute("historyList", historyList);
		return "account/detail";
	}
}
