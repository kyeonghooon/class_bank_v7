package com.tenco.bank.dto;

import org.springframework.web.multipart.MultipartFile;

import com.tenco.bank.repository.model.User;

import lombok.Data;

@Data
public class SignUpDTO {

	private String username;
	private String password;
	private String fullname;
	private MultipartFile mFile;
	private String originFileName;
	private String uploadFileName;

	// 2단계 로직
	public User toUser() {
		return User.builder()
			.username(username)
			.password(password)
			.fullname(fullname)
			.originFileName(originFileName)
			.uploadFileName(uploadFileName)
			.build();
	}

	// TODO - 추후 사진 업로드
}
