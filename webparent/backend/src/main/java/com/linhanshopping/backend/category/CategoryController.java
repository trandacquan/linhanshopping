package com.linhanshopping.backend.category;

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
import com.linhanshopping.backend.category.export.CategoryCsvExporter;
import com.linhanshopping.backend.category.export.CategoryExcelExporter;
import com.linhanshopping.backend.category.export.CategoryPdfExporter;
import com.linhanshopping.common.entity.Category;

@Controller
public class CategoryController {

	// URL mặc định khi /categories hoặc khi có lỗi (category không tồn tại) -->quay lại trang 1
	private String defaultRedirectURL = "redirect:/categories/page/1?sortField=name&sortDir=asc";

	@Autowired
	private CategoryService categoryService;

	// Vào /categories -->luôn redirect sang trang 1 có sẵn sort mặc định (không tự render trực tiếp ở đây)
	@GetMapping("/categories")
	public String listFirstPage(Model model) {
		return defaultRedirectURL;
	}

	// Trang danh sách category có phân trang + sort + tìm kiếm
	// @Param dùng để bắt các cặp key-value trên URL dạng ?sortField=..&sortDir=..&keyword=..
	@GetMapping("/categories/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model,
			@Param("sortField") String sortField,
			@Param("sortDir") String sortDir,
			@Param("keyword") String keyword) {

		Page<Category> page = categoryService.listByPage(pageNum, sortField, sortDir, keyword);
		List<Category> listCategories = page.getContent();

		// Tính số thứ tự category đầu/cuối hiển thị trên trang hiện tại (VD: "Showing 1 to 10 of 23")
		long startCount = (pageNum - 1) * CategoryService.CATEGORIES_PER_PAGE + 1;
		long endCount = startCount + CategoryService.CATEGORIES_PER_PAGE - 1;

		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}

		// reverseSortDir: dùng để đổi chiều sort khi bấm lại vào cùng 1 cột (đang asc -->chuyển desc)
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

		model.addAttribute("title", "Category Controller");
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);

		return "categories/categories"; // trỏ tới templates/categories/categories.html
	}

	// Mở form tạo mới -->khởi tạo sẵn 1 Category rỗng với enabled = true (mặc định bật)
	@GetMapping("/categories/new")
	public String newCategory(Model model) {
		Category category = new Category();
		category.setEnabled(true);

		model.addAttribute("category", category); // form dùng th:object="${category}" nên bắt buộc phải có biến này
		model.addAttribute("pageTitle", "Create New Category");

		return "categories/category_form";
	}

	// Xử lý submit form (dùng chung cho cả tạo mới lẫn cập nhật, phân biệt bằng category.getId() == null hay không)
	// multipartFile: file ảnh người dùng chọn ở input type="file" name="fileImage"
	@PostMapping("/categories/save")
	public String saveCategory(Category category, RedirectAttributes redirectAttributes,
			@RequestParam("fileImage") MultipartFile multipartFile) throws IOException {

		if (!multipartFile.isEmpty()) {
			// Có chọn ảnh mới -->lấy tên file, gán vào category rồi save xuống DB trước để chắc chắn có category.getId()
			// (trường hợp tạo mới thì phải save trước mới có id để đặt tên thư mục lưu ảnh)
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			category.setImage(fileName);
			Category savedCategory = categoryService.save(category);

			// LƯU Ý: thư mục lưu ảnh phải là "../category-images" (ngang hàng với webparent/),
			// khớp với cấu hình exposeDirectory("../category-images", ...) trong MvcConfig.java.
			// Nếu để "category-images/" (không có ../) thì ảnh sẽ lưu vào bên trong module backend,
			// còn URL /category-images/** lại được MvcConfig trỏ ra ngoài -->ảnh lưu 1 nơi, phục vụ 1 nơi khác -->404.
			String uploadDir = "../category-images/" + savedCategory.getId();

			FileUploadUtil.cleanDir(uploadDir); // xóa ảnh cũ (nếu có) vì 1 category chỉ có 1 ảnh
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			// Không chọn ảnh mới -->giữ nguyên tên ảnh cũ (đã có sẵn trong hidden input th:field="*{image}")
			if (category.getImage().isEmpty()) {
				category.setImage(null);
			}
			categoryService.save(category);
		}

		redirectAttributes.addFlashAttribute("message", "The category has been saved successfully");

		return getRedirectURLtoAffectedCategory(category);
	}

	// Sau khi save xong, quay về trang 1 kèm keyword = tên category vừa lưu -->để user thấy ngay kết quả
	private String getRedirectURLtoAffectedCategory(Category category) {
		return "redirect:/categories/page/1?sortField=id&sortDir=asc&keyword=" + category.getName();
	}

	// Mở form sửa: lấy category theo id đổ dữ liệu lên form (dùng chung template với form tạo mới)
	@GetMapping("/categories/edit/{id}")
	public String editCategory(@PathVariable(name = "id") Integer id, Model model,
			RedirectAttributes redirectAttributes) {
		try {
			Category category = categoryService.get(id);

			model.addAttribute("category", category);
			model.addAttribute("pageTitle", "Edit Category (ID: " + id + ")");

			return "categories/category_form";
		} catch (CategoryNotFoundException ex) {
			// id không tồn tại (VD: gõ tay URL sai) -->không cho lỗi 500, quay về trang danh sách kèm thông báo
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return defaultRedirectURL;
		}
	}

	// Xóa category theo id, đồng thời xóa luôn thư mục ảnh tương ứng để không rác dữ liệu
	@GetMapping("/categories/delete/{id}")
	public String deleteCategory(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		try {
			categoryService.delete(id);

			// Xóa thư mục ảnh của category này -->phải dùng "../category-images/" giống lúc save (xem giải thích ở trên)
			String categoryImagesDir = "../category-images/" + id;
			FileUploadUtil.removeDir(categoryImagesDir);

			redirectAttributes.addFlashAttribute("message", "The category id " + id + " has been deleted successfully!");

		} catch (CategoryNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}

		return defaultRedirectURL;
	}

	// Bật/tắt category -->được gọi khi bấm vào icon ở cột "Enabled" trên trang danh sách (fragments :: status)
	@GetMapping("/categories/{id}/enabled/{status}")
	public String updateCategoryEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean enabled,
			RedirectAttributes redirectAttributes) {

		categoryService.updateCategoryEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The category id " + id + " has been " + status;
		redirectAttributes.addFlashAttribute("message", message);

		return defaultRedirectURL;
	}

	// 3 hàm export bên dưới đều lấy toàn bộ danh sách (không phân trang) rồi giao cho lớp Exporter tương ứng xử lý.
	// Trả về void vì ghi dữ liệu trực tiếp vào HttpServletResponse (không qua Thymeleaf template).
	@GetMapping("/categories/export/csv")
	public void exportToCSV(HttpServletResponse response) throws IOException {
		List<Category> listCategories = categoryService.listAll();
		CategoryCsvExporter exporter = new CategoryCsvExporter();
		exporter.export(listCategories, response);
	}

	@GetMapping("/categories/export/excel")
	public void exportToExcel(HttpServletResponse response) throws IOException {
		List<Category> listCategories = categoryService.listAll();
		CategoryExcelExporter exporter = new CategoryExcelExporter();
		exporter.export(listCategories, response);
	}

	@GetMapping("/categories/export/pdf")
	public void exportToPDF(HttpServletResponse response) throws IOException {
		List<Category> listCategories = categoryService.listAll();
		CategoryPdfExporter exporter = new CategoryPdfExporter();
		exporter.export(listCategories, response);
	}

}
