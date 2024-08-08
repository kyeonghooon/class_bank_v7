package com.tenco.bank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.model.Account;

@Service
public class AccountService {

	// DI - 의존 주입
	@Autowired
	private AccountRepository accountRepository;
	
	@Transactional
	public void createAccount(SaveDTO dto, int principalId) {
		int result = 0;
		try {
			result = accountRepository.insert(dto.toAccount(principalId));
		} catch (DataAccessException e) {
			throw new DataDeliveryException("중복된 계좌번호를 사용할 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (result == 0) {
			throw new DataDeliveryException("계좌생성 실패", HttpStatus.BAD_REQUEST);
		}
	}
	
	public List<Account> readAccountListByUserId(Integer principalId) {
		List<Account> accountListEntity = null; // null ??? --> isEmpty()로 받으려면 최소한 객체는 있어야되는거아닌가? 
		try {
			accountListEntity = accountRepository.findByUserId(principalId);
		} catch (DataAccessException e) {
			throw new DataDeliveryException("잘못된 처리 입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}
		return accountListEntity;
	}

}
