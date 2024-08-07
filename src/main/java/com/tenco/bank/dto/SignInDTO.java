package com.tenco.bank.dto;

import com.tenco.bank.repository.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SignInDTO {
	
	private String username;
	private String password;
	
	// 2단계 로직
	public User toUser() {
		return User.builder()
				.username(username)
				.password(password)
				.build();
	}
	
	// TODO - 추후 사진 업로드 
}
