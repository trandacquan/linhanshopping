package com.linhanshopping.backend.user;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.ArrayList;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.linhanshopping.backend.role.RoleRepository;
import com.linhanshopping.common.entity.Role;
import com.linhanshopping.common.entity.User;

@Service
@Transactional
public class UserService {

	//public static final int USERS_PER_PAGE = 5;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public List<User> listAll() {
		return (List<User>) userRepo.findAll(Sort.by("firstName").ascending());
	}

	public Page<User> listByPage(int pageNum, String sortField, String sortDir, String keyword, Integer size) {
		// Đối tượng sort sẽ sắp xếp các giá trị trả về theo biến sortField
		// tăng dần asc hoặc giảm dần desc
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

		Pageable pageable = PageRequest.of(pageNum - 1, size, sort);

		if (keyword != null) {
			return userRepo.findAll(keyword, pageable);
		}
		return userRepo.findAll(pageable);
	}

	public int count() {
		return (int) userRepo.count();
	}

	public List<Integer> calculateSegment(int userCount, int baseSegment) {
		List<Integer> segments = new ArrayList<>();// Tạo một list segments chuẩn bị chứa 1 loạt các phân đoạn để lựa
													// chọn

		if (userCount <= baseSegment) {// Nếu số lượng user <= baseSegment
			segments.add(userCount);
		} else {
			int segmentCount = userCount / baseSegment;

			for (int i = 1; i <= segmentCount; i++) {
				segments.add(i * baseSegment);
			}

			int e = segments.get(segments.size() - 1);

			if (userCount != e) {
				segments.add(userCount); // Phân đoạn "All"
			}
		}

		return segments;
	}

	public List<Role> listRoles() {
		return (List<Role>) roleRepo.findAll();// .findAll() trả về Iterable<Role> -->ép kiểu thành List<Role>
	}

	public boolean isEmailUnique(Integer id, String email) {
		User userByEmail = userRepo.getUserByEmail(email);

		// Trường hợp không có email dưới repo thì cho cập nhật email ngay
		// Nếu tìm không có userByEmail dưới repo thì trả về true -> nghĩa là email ko
		// trùng
		if (userByEmail == null)
			return true;

		// Trường hợp có email dưới repo:
		// Xét việc tạo mới hay update user -> nếu tạo mới thì không được trùng
		// nếu update thì được trùng
		boolean isCreatingNew = (id == null) ? true : false;

		if (isCreatingNew) {// Nếu tạo mới thì ko đc trùng
			if (userByEmail != null)
				return false;
		} else {// Nếu update thì có thể trùng
			if (userByEmail.getId() != id)
				return false;
		}

		return true;
	}

	/* Hàm save User xuống db */
	public User save(User user) {
		boolean isUpdatingUser = (user.getId() != null) ? true : false;// Nếu id=null thì Create, !=null(có id của user
																		// truyền vào) thì Update

		if (isUpdatingUser) {// Trường hợp Update (isUpdatingUser = true)
			User existingUser = userRepo.findById(user.getId()).get();// Lấy user theo id bằng cách truy dưới repo
			if (user.getPassword().isEmpty()) {// Nếu không nhập password thì lấy pass cũ để save lại
				user.setPassword(existingUser.getPassword());// Save pass cũ đã tồn tại
			} else {// Nếu có nhập password thì mã hóa
				encodePassword(user);// Mã hóa password bằng Bcriptpassword
			}
		} else {// Trường hợp Create -> luôn mã hóa password
			encodePassword(user);
		}

		return userRepo.save(user);// Save user xuống db
	}

	/*
	 * Hàm mã hoas password phải import PasswordEncoder (Inteface) từ đầu để sử dụng
	 */
	private void encodePassword(User user) {
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
	}

	public User get(Integer id) throws UserNotFoundException {
		try {
			return userRepo.findById(id).get();// lấy user ra theo id
		} catch (NoSuchElementException ex) {
			throw new UserNotFoundException("Could not find any user with id " + id);
		}
	}

	public void delete(Integer id) throws UserNotFoundException {
		Long countById = userRepo.countById(id);
		if (countById == null || countById == 0) {
			throw new UserNotFoundException("Could not find any user with id " + id);
		}

		userRepo.deleteById(id);
	}

	public void updateUserEnabledStatus(Integer id, boolean enabled) {
		userRepo.updateEnabledStatus(id, enabled);
	}

}
