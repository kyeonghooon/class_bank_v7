package com.tenco.bank.service;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	/**
	 * 회원 등록 서비스 기능 트랜잭션 처리
	 * 
	 * @param dto
	 */
	@Transactional
	public void createUser(SignUpDTO dto) {

		int result = 0;
		try {
			
			// 코드 추가 부분
			// 회원 가입 요청시 사용자가 던진 비밀번호 값을 암호화 처리 해야 함
			String hashPwd = passwordEncoder.encode(dto.getPassword());
			dto.setPassword(hashPwd);
			
			result = userRepository.insert(dto.toUser());
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.EXIST_USER, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (result != 1) {
			throw new DataDeliveryException(Define.FAIL_TO_CREATE_USER, HttpStatus.BAD_REQUEST);
		}

	}

	public User readUser(SignInDTO dto) {
		User userEntity = null;
		
		try {
			userEntity = userRepository.findByUsername(dto.getUsername());
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}

		if (userEntity == null) {
			throw new DataDeliveryException(Define.FAIL_USER_LOGIN, HttpStatus.BAD_REQUEST);
		}
		if (!passwordEncoder.matches(dto.getPassword(), userEntity.getPassword())) {
			throw new DataDeliveryException(Define.FAIL_USER_LOGIN_PASSWORD, HttpStatus.BAD_REQUEST);
		}

		return userEntity;
	}

}
