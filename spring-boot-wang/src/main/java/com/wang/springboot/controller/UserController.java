package com.wang.springboot.controller;

import com.wang.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wangd1
 */
@RestController
public class UserController {

	@Resource
	private UserService userService;

	@GetMapping("/")
	public String test(){
		return userService.test();
	}
}
