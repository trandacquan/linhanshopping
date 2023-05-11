package com.linhanshopping.backend.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.linhanshopping.common.entity.User;

@Controller
public class UserController {

	private String defaultRedirectURL = "redirect:/users/page/1";

	@Autowired
	private UserService userService;

	@GetMapping("/users")
	public String listFistPage(Model model) {
		return defaultRedirectURL;
	}

	@GetMapping("/users/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model) {
		List<User> listUsers = userService.listAll();

		model.addAttribute("listUsers", listUsers);

		return "users/users";
	}
}
