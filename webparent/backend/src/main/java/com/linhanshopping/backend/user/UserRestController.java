package com.linhanshopping.backend.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

}
