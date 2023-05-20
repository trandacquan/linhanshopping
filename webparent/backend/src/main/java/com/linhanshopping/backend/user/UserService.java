package com.linhanshopping.backend.user;

import java.util.List;
import java.util.ArrayList;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.linhanshopping.backend.role.RoleRepository;
import com.linhanshopping.common.entity.User;

@Service
@Transactional
public class UserService {

	public static final int USERS_PER_PAGE = 5;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	public List<User> listAll() {
		return (List<User>) userRepo.findAll(Sort.by("firstName").ascending());
	}

	public Page<User> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		// Đối tượng sort sẽ sắp xếp các giá trị trả về theo biến sortField
		// tăng dần asc hoặc giảm dần desc
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

		Pageable pageable = PageRequest.of(pageNum - 1, USERS_PER_PAGE, sort);

		if (keyword != null) {
			return userRepo.findAll(keyword, pageable);
		}
		return userRepo.findAll(pageable);
	}
	
	public int count() {
		return (int) userRepo.count();
	}

	public List<Integer> calculateSegment(int userCount, int baseSegment) {
		List<Integer> segments = new ArrayList<>();

		if (userCount <= baseSegment) {
			segments.add(userCount);
		} else {
			int segmentCount = userCount / baseSegment;

			for (int i = 1; i <= segmentCount; i++) {
				segments.add(i * baseSegment);
			}
			
			int e = segments.get(segments.size()-1);
			
			if(userCount!=e) {
				segments.add(userCount); // Phân đoạn "All"
			}
		}
		
		return segments;
	}

}
