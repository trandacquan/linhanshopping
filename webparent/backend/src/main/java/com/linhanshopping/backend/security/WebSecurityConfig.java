package com.linhanshopping.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.linhanshopping.backend.security.ShoppingUserDetails;

@Configuration // @Configuration + @Bean -->khởi tạo Spring Bean -->để các Spring Bean khác có
				// thể @Autowired
@EnableWebSecurity // Kích hoạt Spring Security, lưu ý phải import trong file pom
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	//kế thừa WebSecurityConfigurerAdapter để @Override phương thức configure
	
	@Bean
	public UserDetailsService userDetailsService() {
		return new ShoppingUserDetailsService();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
