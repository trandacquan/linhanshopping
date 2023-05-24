package com.linhanshopping.backend.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRestController {

	@Autowired
	private UserService userService;

	@GetMapping("/users/segments")
	public List<Integer> getSegments(int userCount, int baseSegment) {
		List<Integer> segments = userService.calculateSegment(userCount, baseSegment);
		return segments;
	}

	@PostMapping("/users/check_email")
	public String checkDuplicateEmail(Integer id, String email) {
		// params = { id: userId, email: userEmail}
		// -->khai báo id,email đúng tên và thứ tự như object params
		return userService.isEmailUnique(id, email) ? "OK" : "Duplicated";
	}

}
