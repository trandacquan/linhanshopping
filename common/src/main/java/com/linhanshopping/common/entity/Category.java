package com.linhanshopping.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "categories")
public class Category extends IdBasedEntity {

	@Column(length = 128, nullable = false, unique = true)
	private String name;

	@Column(length = 64, nullable = false, unique = true)
	private String alias;

	@Column(length = 128, nullable = false)
	private String image;

	private boolean enabled;

	public Category() {
		super();
	}

	public Category(String name) {
		this.name = name;
		this.alias = name;
		this.image = "default.png";
	}

	public Category(Integer id, String name, String alias) {
		super();
		this.id = id;
		this.name = name;
		this.alias = alias;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return this.name;
	}

	// Trả về URL ảnh để hiển thị trên giao diện (không lưu xuống DB -->@Transient)
	// Lưu ý: URL này phải khớp với nơi CategoryController lưu file thật ("../category-images/" + id, xem
	// CategoryController.saveCategory()) và với cấu hình expose thư mục trong MvcConfig.addResourceHandlers().
	// (Trước đây thiếu dấu "/" giữa "category-images" và id, gây lỗi ảnh không hiển thị -->đã fix.)
	@Transient
	public String getImagePath() {
		if (this.image == null) {
			return "/images/image-thumbnail.png"; // ảnh mặc định khi category chưa có ảnh
		} else {
			return "/category-images/" + this.id + "/" + this.image;
		}
	}

}
