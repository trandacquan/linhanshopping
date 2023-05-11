package com.linhanshopping.backend.user;

import org.springframework.data.repository.CrudRepository;

import com.linhanshopping.common.entity.User;

public interface UserRepository extends CrudRepository<User, Integer> {

	public Long countById(Integer id);
	
}
