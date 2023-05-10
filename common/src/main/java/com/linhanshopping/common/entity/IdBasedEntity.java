package com.linhanshopping.common.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass // Để kế thừa các entity sẽ nhận biết đc các annotation trong entity này
public abstract class IdBasedEntity {
	@Id // Khai báo khóa chính cho Entity
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Khóa chính tự động tăng
	protected Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
