package com.tenco.bank.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	/**
	 * 회원 등록 서비스 기능 트랜잭션 처리
	 * 
	 * @param dto
	 */
	@Transactional
	public void createUser(SignUpDTO dto) {

		int result = 0;
		if (!dto.getMFile().isEmpty()) {
			String[] fileNames = uploadFile(dto.getMFile());
			dto.setOriginFileName(fileNames[0]);
			dto.setUploadFileName(fileNames[1]);
		}
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
	
	/**
	 * 서버 운영체제에 파일 업로드 기능
	 * mFile.getgetOriginalFilename() : 사용자가 입력한 파일명
	 * uploadFileName : 서버 컴퓨터에 저장될 파일명
	 * @return
	 */
	private String[] uploadFile(MultipartFile mFile) {
		
		if(mFile.getSize() > Define.MAX_FILE_SIZE) {
			throw new DataDeliveryException("파일 크기는 20MB 이상 클 수 없습니다.", HttpStatus.BAD_REQUEST);
		}
		
		// 코드 수정
		// File - getAbsolutePath() : 파일 시스템의 절대 경로를 나타냅니다.
		// (리눅스 또는 MacOS)에 맞춰서 절대 경로를 생성 시킬 수 있다.
		String saveDerectory = new File(uploadDir).getAbsolutePath();
		System.out.println("saveDerectory : " + saveDerectory);
		File directory = new File(saveDerectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		// 파일 이름 생성(중복 이름 예방)
		String uploadFileName = UUID.randomUUID() + "_" + mFile.getOriginalFilename();
		
		// 파일 전체경로 + 새로 생성한 파일명
		String uploadPath = saveDerectory + File.separator + uploadFileName;
		System.err.println("--------------------------");
		System.out.println(uploadPath);
		System.err.println("--------------------------");
		File destination = new File(uploadPath);
		
		// 반드시 수행
		try {
			mFile.transferTo(destination);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			throw new DataDeliveryException("파일 업로드 중에 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		return new String[] {mFile.getOriginalFilename(), uploadFileName};
	}

}
