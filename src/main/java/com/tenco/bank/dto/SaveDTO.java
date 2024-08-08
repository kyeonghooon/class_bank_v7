package com.tenco.bank.dto;

import com.tenco.bank.repository.model.Account;

import lombok.Data;

@Data
public class SaveDTO {
	
	private String number;
	private String password;
	private Long balance;
	
	public Account toAccount(Integer userId) { 
		return Account.builder()
				.number(this.number)
				.password(this.password)
				.balance(this.balance)
				.userId(userId)
				.build();
	}
	
}
