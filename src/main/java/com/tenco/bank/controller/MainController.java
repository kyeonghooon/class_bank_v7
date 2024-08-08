package com.tenco.bank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@GetMapping({"/main-page", "/index"})
	public String mainPage() {
		return "main";
	}
	
}
