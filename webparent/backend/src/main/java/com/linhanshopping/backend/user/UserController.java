package com.linhanshopping.backend.user;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.linhanshopping.backend.FileUploadUtil;
import com.linhanshopping.backend.user.export.UserCsvExporter;
import com.linhanshopping.backend.user.export.UserExcelExporter;
import com.linhanshopping.backend.user.export.UserPdfExporter;
import com.linhanshopping.common.entity.Role;
import com.linhanshopping.common.entity.User;

@Controller
public class UserController {

	private String defaultRedirectURL = "redirect:/users/page/1?sortField=firstName&sortDir=asc&size=5";

	@Autowired
	private UserService userService;

	/**
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
			@Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword) {

		Page<User> page = userService.listByPage(pageNum, sortField, sortDir, keyword);
		List<User> listUsers = page.getContent();

		long startCount = (pageNum - 1) * UserService.USERS_PER_PAGE + 1;
		long endCount = startCount + UserService.USERS_PER_PAGE - 1;

		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}

		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

		model.addAttribute("title", "User Controller");// truyen title
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

		return "users/users";// trả về users/users.html
	}

	@GetMapping("/users/new")
	public String newUser(Model model) {
		List<Role> listRoles = userService.listRoles();

		User user = new User();
		user.setEnabled(true);

		model.addAttribute("user", user);// khi trả về form user_form.html thì bắt buộc phải khởi tạo 1 đối tượng user,
		// vì trong form có khai báo th:object="${user}"
		model.addAttribute("listRoles", listRoles);
		model.addAttribute("title", "User");

		return "users/user_form";
	}

	@PostMapping("/users/save")
	public String saveUser(User user, RedirectAttributes redirectAttributes,
			@RequestParam("image") MultipartFile multipartFile) throws IOException {
		// -RedirectAttributes chỉ có tác dụng khi dùng "redirect:/.. -->gọi @GetMapping
		// -Trường hợp submit form có method là POST -->tất cả các giá trị trong form sẽ
		// được gán vào đối tượng user. Nhưng nếu vẫn muốn lấy riêng các giá trị của thẻ
		// input, select,...(những thẻ có thẻ nhập, chọn được) thì có thể dùng
		// @RequestParam("image") -->lưu ý image là name của thẻ cần lấy --> <input
		// type="file" id="fileImage" name="image"/> -->thẻ này có type="file" nên sẽ
		// dùng đối tượng multipartFile để gán giá trị
		if (!multipartFile.isEmpty()) {// nếu multipartFile empty -> khi ko nhấn chọn hình
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());// Chỉ lấy tên file hình để lưu
			user.setPhotos(fileName);// Gán tên file hình vào object photos
			User savedUser = userService.save(user);// save user

			String uploadDir = "user-photos/" + savedUser.getId();// Tạo đường dẫn để chuẩn bị cho gđ save hình.

			FileUploadUtil.cleanDir(uploadDir);// xóa tất cả các file hình nằm bên trong folder hiện tại, vì 1 user chỉ
												// có 1 hình
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);// save hình của user vào folder user-photos
		} else {// Nếu multipartFile có cả file hình
			if (user.getPhotos().isEmpty())
				user.setPhotos(null);
			userService.save(user);
		}

		redirectAttributes.addFlashAttribute("message", "The user has been saved successfully");

		return getRedirectURLtoAffectedUser(user);
	}

	private String getRedirectURLtoAffectedUser(User user) {
		String firstPartOfEmail = user.getEmail().split("@")[0];
		// Trả về trang list users có keyword là email của user này.
		return "redirect:/users/page/1?sortField=id&sortDir=asc&keyword=" + firstPartOfEmail;
	}

	@GetMapping("/users/edit/{id}")
	public String editUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			User user = userService.get(id);
			List<Role> listRoles = userService.listRoles();

			model.addAttribute("user", user);
			model.addAttribute("pageTitle", "Edit User (ID: " + id + ")");
			model.addAttribute("listRoles", listRoles);

			return "users/user_form";
		} catch (UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return defaultRedirectURL;
		}
	}

	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable(name = "id") Integer id, Model model,
			RedirectAttributes redirectAttributes) {
		try {
			userService.delete(id);

			String userPhotoDirectory = "user-photos/" + id;
			FileUploadUtil.removeDir(userPhotoDirectory);// Sau khi delete user thì xóa folder chứa hình của user đó

			redirectAttributes.addFlashAttribute("message", "The user id " + id + "has been deleted successfully!");

		} catch (UserNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}

		return defaultRedirectURL;
	}

	@GetMapping("/users/{id}/enabled/{status}")
	public String updateUserEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean enabled,
			RedirectAttributes redirectAttributes, Model model) {

		userService.updateUserEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disable";
		String message = "The user id " + id + "has been " + status;
		redirectAttributes.addFlashAttribute("message", message);

		return defaultRedirectURL;
	}

	@GetMapping("/users/export/csv")
	public void exportToCSV(HttpServletResponse response) throws IOException {
		List<User> listUsers = userService.listAll();
		UserCsvExporter exporter = new UserCsvExporter();
		exporter.export(listUsers, response);
	}

	@GetMapping("/users/export/excel")
	public void exportToExcel(HttpServletResponse response) throws IOException {
		List<User> listUsers = userService.listAll();
		UserExcelExporter exporter = new UserExcelExporter();
		exporter.export(listUsers, response);
	}

	@GetMapping("/users/export/pdf")
	public void exportToPDF(HttpServletResponse response) throws IOException {
		List<User> listUsers = userService.listAll();
		UserPdfExporter exporter = new UserPdfExporter();
		exporter.export(listUsers, response);
	}

}
