package com.tenco.bank.dto;

import com.tenco.bank.repository.model.User;

import lombok.Data;

@Data
public class SignUpDTO {
	
	private String username;
	private String password;
	private String fullname;
	
	// 2단계 로직
	public User toUser() {
		return User.builder()
				.username(username)
				.password(password)
				.fullname(fullname)
				.build();
	}
	
	// TODO - 추후 사진 업로드 
}
