package com.linhanshopping.backend.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Được ghi đè từ WebMvcConfigurer để đăng ký các resource handler cho các đường
		// dẫn tài nguyên
		// Hàm exposeDirectory để đăng ký các thư mục chứa tài nguyên.
		// Với exposeDirectory thì một đường dẫn và 1 đối tượng ResourceHandlerRegistry
		// sẽ đc truyền vào
		exposeDirectory("user-photos", registry);// Khai báo đường dẫn tương đối
		exposeDirectory("../category-images", registry);// Relative path -->
		exposeDirectory("../brand-logos", registry);
		exposeDirectory("../product-images", registry);
	}

	private void exposeDirectory(String pathPattern, ResourceHandlerRegistry registry) {
		// phải khai báo folder chứa hình thì mới truy cập được đến các folder hình này

		Path path = Paths.get(pathPattern);// Chuyển đg dẫn mẫu pathPattern thành một đối tượng path

		String absolutePath = path.toFile().getAbsolutePath();// Đường dẫn đến folder chứa hình
		// Lấy đường dẫn tuyệt đối của thư mục. Đồng thời xây dựng một đường dẫn logic
		// bằng cách loại bỏ các ký tự '../'
		// trong pathPattern và thêm '/**' vào cuối.

		String logicalPath = pathPattern.replace("../", "") + "/**";

		registry.addResourceHandler(logicalPath).addResourceLocations("file:/" + absolutePath + "/");
		// Cuối cùng đăng kí resource handler với đường dẫn logic và đường dẫn tuyệt
		// đối của thư mục xử dụng
		// ("file:/" + absolutePath + "/")
	}
}
