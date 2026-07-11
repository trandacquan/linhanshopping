package com.linhanshopping.backend.category;

import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.linhanshopping.common.entity.Category;

// @Service: đánh dấu đây là 1 Spring Bean thuộc tầng Service (business logic), để Controller có thể @Autowired.
// @Transactional: mỗi hàm public trong class này chạy trong 1 transaction DB (tự rollback nếu có lỗi).
@Service
@Transactional
public class CategoryService {

	// Số category hiển thị trên mỗi trang -->dùng cố định (khác với User có thêm tùy chọn "Show N entries")
	public static final int CATEGORIES_PER_PAGE = 10;

	@Autowired
	private CategoryRepository categoryRepo;

	// Lấy tất cả category, sắp xếp theo tên -->dùng cho export CSV/Excel/PDF (không cần phân trang)
	public List<Category> listAll() {
		return (List<Category>) categoryRepo.findAll(Sort.by("name").ascending());
	}

	// Lấy category theo trang, có sắp xếp (sortField/sortDir) và tìm kiếm (keyword)
	public Page<Category> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

		// pageNum trên URL bắt đầu từ 1 (thân thiện với người dùng), nhưng Spring Data PageRequest bắt đầu từ 0
		Pageable pageable = PageRequest.of(pageNum - 1, CATEGORIES_PER_PAGE, sort);

		// Nếu có từ khóa tìm kiếm thì dùng query findAll(keyword, pageable) đã viết ở Repository,
		// ngược lại dùng findAll(pageable) có sẵn để lấy tất cả theo trang
		if (keyword != null) {
			return categoryRepo.findAll(keyword, pageable);
		}
		return categoryRepo.findAll(pageable);
	}

	// Dùng chung cho cả tạo mới và cập nhật: nếu category.id == null -->JPA tự INSERT,
	// nếu category.id != null -->JPA tự UPDATE (theo cơ chế merge của Hibernate)
	public Category save(Category category) {
		return categoryRepo.save(category);
	}

	// Lấy 1 category theo id để hiển thị lên form edit.
	// findById() trả về Optional<Category>, .get() sẽ ném NoSuchElementException nếu rỗng
	// -->bắt lại và ném ra CategoryNotFoundException (custom exception) để Controller xử lý dễ hơn
	public Category get(Integer id) throws CategoryNotFoundException {
		try {
			return categoryRepo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new CategoryNotFoundException("Could not find any category with id " + id);
		}
	}

	// Xóa category theo id, có kiểm tra tồn tại trước (dùng countById ở Repository)
	// để tránh xóa 1 id không có thật (deleteById của Spring Data sẽ ném lỗi khó hiểu nếu id không tồn tại)
	public void delete(Integer id) throws CategoryNotFoundException {
		Long countById = categoryRepo.countById(id);
		if (countById == null || countById == 0) {
			throw new CategoryNotFoundException("Could not find any category with id " + id);
		}

		categoryRepo.deleteById(id);
	}

	// Bật/tắt trạng thái enabled của 1 category (click vào icon ở cột "Enabled" trên trang danh sách)
	public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
		categoryRepo.updateEnabledStatus(id, enabled);
	}

}
