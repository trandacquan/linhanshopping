package com.linhanshopping.backend.role;

import org.springframework.data.repository.CrudRepository;

import com.linhanshopping.common.entity.Role;

public interface RoleRepository extends CrudRepository<Role, Integer>{
	// Lưu ý tham số thứ nhất (Role) là Entity
	// Tham số thứ 2 (Integer)  là kiểu dữ liệu của khóa chính
	
	// RoleRepository kế thừa (extends) từ CrudRepository nên sẽ có
	//  các hàm từ CrudRepository như sau:
	//  save, saveAll, findById, findAll, existById
	//  findAllById, count, delete, deleteById, deleteAll
	
	// CRUD: Create, Read, Update, Delete
}
