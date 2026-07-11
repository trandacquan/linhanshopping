package com.linhanshopping.backend.category;

// Exception tự tạo, ném ra khi tìm category theo id nhưng không có trong DB (xem CategoryService.get()/delete()).
// Controller sẽ bắt exception này để redirect kèm thông báo lỗi thay vì để lộ lỗi 500 ra ngoài.
public class CategoryNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public CategoryNotFoundException(String message) {
		super(message);
	}
}
