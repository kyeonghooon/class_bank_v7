package com.tenco.bank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.utils.Define;

@Service
public class AccountService {

	// DI - 의존 주입
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private HistoryRepository historyRepository;

	@Transactional
	public void createAccount(SaveDTO dto, int principalId) {
		int result = 0;
		try {
			result = accountRepository.insert(dto.toAccount(principalId));
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.EXIST_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (result == 0) {
			throw new DataDeliveryException(Define.FAIL_TO_CREATE_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
	}

	@Transactional
	public List<Account> readAccountListByUserId(Integer principalId) {
		List<Account> accountListEntity = null;
		try {
			accountListEntity = accountRepository.findByUserId(principalId);
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}
		return accountListEntity;
	}

	// 한번에 모든 기능을 생각하는건 힘듦
	// 1. 계좌 존재 여부를 확인
	// 2. 본인 계좌 여부를 확인 -- 객체 상태값에서 비교
	// 3. 계좌 비번 확인 -- 객체 상태값에서 비교
	// 4. 잔액 여부 확인 -- 객체 상태값에서 확인
	// 5. 출금 처리 -- update
	// 6. 거래 내역 등록 -- insert(history)
	public void updateAccountWithdraw(WithdrawalDTO dto, Integer principalId) {
		// 1.
		Account accountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		if (accountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		accountEntity.checkOwner(principalId);
		accountEntity.checkPassword(dto.getWAccountPassword());
		accountEntity.checkBalance(dto.getAmount());
		// 5.
		accountEntity.withdraw(dto.getAmount());
		accountRepository.updateById(accountEntity);
		// 6.
		History history = History.builder()
			.amount(dto.getAmount())
			.wAccountId(accountEntity.getId())
			.wBalance(accountEntity.getBalance())
			.dAccountId(null)
			.dBalance(null)
			.build();
		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 한번에 모든 기능을 생각하는건 힘듦
	// 1. 계좌 존재 여부를 확인
	// 2. 본인 계좌 여부를 확인 -- 객체 상태값에서 비교
	// 3. 입금 처리 -- update
	// 4. 거래 내역 등록 -- insert(history)
	@Transactional
	public void updateAccountDeposit(DepositDTO dto, Integer principalId) {
		// 1.
		Account accountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		if (accountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2.
		accountEntity.checkOwner(principalId);
		// 3.
		accountEntity.deposit(dto.getAmount());
		accountRepository.updateById(accountEntity);
		// 4.
		History history = History.builder()
			.amount(dto.getAmount())
			.dAccountId(accountEntity.getId())
			.dBalance(accountEntity.getBalance())
			.wAccountId(null)
			.wBalance(null)
			.build();
		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 이체 기능 만들기
	// 1. 출금 계좌 존재 여부를 확인 -- select
	// 2. 입금 계좌 존재 여부 확인 -- select
	// 3. 출금 계좌 본인 계좌 여부를 확인 -- 객체 상태값에서 비교
	// 4. 출금 계좌 비밀 번호 확인 -- 객체 상태값에서 비교
	// 5. 출금 계좌 잔액 여부 확인 -- 객체 상태값에서 확인
	// 6. 입금 계좌 객체 상태값 변경 처리 (거래금액 증가)
	// 7. 입금 계좌 -- update
	// 8. 출금 계좌 객체 상태값 변경 처리 (잔액 - 거래금액)
	// 9. 출금 계좌 -- update
	// 10. 거래 내역 등록 -- insert(history)
	// 11. 트랜잭션 처리
	public void updateAccountTransfer(TransferDTO dto, Integer principalId) {
		// 1.
		Account wAccountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		if (wAccountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2.
		Account dAccountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		if (dAccountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 3.
		wAccountEntity.checkOwner(principalId);
		// 4.
		wAccountEntity.checkPassword(dto.getPassword());
		// 5.
		wAccountEntity.checkBalance(dto.getAmount());
		// 6.
		dAccountEntity.deposit(dto.getAmount());
		// 7.
		accountRepository.updateById(dAccountEntity);
		// 8.
		wAccountEntity.withdraw(dto.getAmount());
		// 9.
		accountRepository.updateById(wAccountEntity);
		// 10.
		History history = History.builder()
			.amount(dto.getAmount())
			.wAccountId(wAccountEntity.getId())
			.dAccountId(dAccountEntity.getId())
			.wBalance(wAccountEntity.getBalance())
			.dBalance(dAccountEntity.getBalance())
			.build();
		historyRepository.insert(history);

	}

}
