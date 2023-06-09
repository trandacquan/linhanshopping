package com.linhanshopping.backend.user.export;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.linhanshopping.backend.AbstractExporter;
import com.linhanshopping.common.entity.User;

public class UserCsvExporter extends AbstractExporter {

	public void export(List<User> listUsers, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "text/csv", ".csv", "users_");// Các tham số là các định dạng mong muốn

		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

		String[] csvHeader = { "User Id", "E-mail", "First Name", "Last Name", "Roles", "Enabled" };// Phần header hiển
																									// thị
		String[] fieldMapping = { "id", "email", "firstName", "lastName", "roles", "enabled" };// Khai báo các thuộc
																								// tính trong entity

		csvWriter.writeHeader(csvHeader);// write header

		for (User user : listUsers) {
			csvWriter.write(user, fieldMapping);// write rows
		}
		
		csvWriter.close();
	}

}
