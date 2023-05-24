package com.linhanshopping.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.linhanshopping.backend.security.ShoppingUserDetails;
import com.linhanshopping.backend.user.UserRepository;
import com.linhanshopping.common.entity.User;

public class ShoppingUserDetailService implements UserDetailsService{
	
	@Autowired
	private UserRepository userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
		User user = userRepo.getUserByEmail(email);
		if (user != null) {
			return new ShoppingUserDetails(user);
		}
		throw new UsernameNotFoundException("Could not find user with email: " + email);
	}
}
