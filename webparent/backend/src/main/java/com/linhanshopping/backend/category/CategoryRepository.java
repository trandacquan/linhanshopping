package com.linhanshopping.backend.category;

import com.linhanshopping.common.entity.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

// CategoryRepository kế thừa PagingAndSortingRepository nên có sẵn các hàm:
// save, saveAll, findById, findAll, existById, findAllById, count, delete, deleteById, deleteAll
// đồng thời có thêm findAll(Sort) và findAll(Pageable) để sắp xếp/phân trang (mạnh hơn CrudRepository).
public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer>{

	// Derived query method: Spring Data tự sinh câu SQL COUNT dựa theo tên hàm (countBy + tên field "Id")
	// Dùng để kiểm tra 1 category có tồn tại hay không trước khi update/xóa (xem CategoryService.delete()).
	public Long countById(Integer id);

	// Query tùy chỉnh bằng JPQL để tìm kiếm category theo từ khóa (tìm theo tên).
	// ?1 là positional parameter, ánh xạ tới tham số đầu tiên (keyword) của hàm.
	// Cùng tên "findAll" nhưng khác tham số so với findAll() có sẵn ở PagingAndSortingRepository -->overload.
	@Query("SELECT c FROM Category c WHERE c.name LIKE %?1%")
	public Page<Category> findAll(String keyword, Pageable pageable);

	// Query để bật/tắt (enable/disable) 1 category mà không cần load cả Entity lên rồi save lại.
	// ?1 = id, ?2 = enabled (theo đúng thứ tự tham số của hàm bên dưới).
	// Bắt buộc phải có @Modifying khi query là UPDATE/INSERT/DELETE (không phải SELECT).
	@Query("UPDATE Category c SET c.enabled = ?2 WHERE c.id = ?1")
	@Modifying
	public void updateEnabledStatus(Integer id, boolean enabled);

}
