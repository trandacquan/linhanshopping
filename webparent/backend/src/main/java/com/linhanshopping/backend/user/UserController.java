package com.linhanshopping.backend.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.linhanshopping.common.entity.User;

@Controller
public class UserController {

	private String defaultRedirectURL = "redirect:/users/page/1?sortField=firstName&sortDir=asc&segment=5";

	@Autowired
	private UserService userService;

	/*
	 * Hàm Show tất cả Users Khi truy cập /users thì mặc định sẽ load URL:
	 * defaultRedirectURL = "redirect:/users/page/1"
	 */
	@GetMapping("/users")
	public String listFistPage(Model model) {
		return defaultRedirectURL;
	}

	/* Hàm list All Users dùng để list tất cả Users không phân trang */
	@GetMapping("/users/pages")
	public String listAllUsers(Model model) {
		List<User> listUsers = userService.listAll();
		model.addAttribute("listUsers", listUsers);
		return "users/users";
	}

	@GetMapping("/users/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model,
			@Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword,
			@Param("segment") int segment) {

		int segment_base = segment;
		int segment_constant = 5;

		Page<User> page = userService.listByPage(pageNum, sortField, sortDir, keyword, segment_base);
		List<User> listUsers = page.getContent();

		List<Integer> segments = userService.calculateSegment(userService.count(), segment_constant);

		long startCount = (pageNum - 1) * segment_base + 1;
		long endCount = startCount + segment_base - 1;

		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}

		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

		model.addAttribute("currentPage", pageNum);// Trang hiện tại
		model.addAttribute("totalPages", page.getTotalPages());// Tổng số trang
		model.addAttribute("startCount", startCount);// Index của user bắt đầu
		model.addAttribute("endCount", endCount);// Index của user bắt đầu
		model.addAttribute("totalItems", page.getTotalElements());// tổng số users(số users tối đa)
		model.addAttribute("listUsers", listUsers);// tất cả users trong trang hiện tại
		model.addAttribute("sortField", sortField);// thuộc tính cần sắp xếp theo
		model.addAttribute("sortDir", sortDir);// sắp xếp tăng dần hoặc giảm dần
		model.addAttribute("reverseSortDir", reverseSortDir);// đảo ngược sắp xếp
		model.addAttribute("keyword", keyword);// từ khóa tìm kiếm
		model.addAttribute("listSegments", segments);// Trả về các list segments

		return "users/users";// trả về users/users.html
	}

}
