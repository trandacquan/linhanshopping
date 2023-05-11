package com.linhanshopping.backend.user;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linhanshopping.backend.role.RoleRepository;
import com.linhanshopping.common.entity.User;

@Service
@Transactional
public class UserService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private RoleRepository roleRepo;
	
	public List<User> listAll() {
		return (List<User>) userRepo.findAll();
	}
}
