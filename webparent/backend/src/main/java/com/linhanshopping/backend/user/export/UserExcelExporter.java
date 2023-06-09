package com.linhanshopping.backend.user.export;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.linhanshopping.backend.AbstractExporter;
import com.linhanshopping.common.entity.User;

public class UserExcelExporter extends AbstractExporter {

	private XSSFWorkbook workbook;

	private XSSFSheet sheet;

	public UserExcelExporter() {
		workbook = new XSSFWorkbook();// Khởi tạo 1 workbook đầu tiên: workbook -> sheet -> cell
	}

	private void createCell(XSSFRow row, int columnIndex, Object value, CellStyle style) {// Hàm tạo các ô: row, column
																							// index, value, style
		XSSFCell cell = row.createCell(columnIndex);// Đứng từ row tạo các cell
		sheet.autoSizeColumn(columnIndex);// Điều chỉnh chiều rộng các ô tự động fix với nội dung

		// Ghi các giá trị vào ô.
		if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else {
			cell.setCellValue((String) value);
		}

		cell.setCellStyle(style);// Gán style cho các ô
	}

	private void writeHeaderLine() {// Hàm tạo header line
		sheet = workbook.createSheet("Users");// Tạo 1 sheet Users trong workbook
		XSSFRow row = sheet.createRow(0);// Tạo hàng mới trong sheet Users --> Bắt đầu từ hàng số 0

		XSSFCellStyle cellStyle = workbook.createCellStyle();// Tạo cellStyle để lưu định dạng như bên dưới
		cellStyle.setAlignment(HorizontalAlignment.CENTER);// Căn giữa trong cell
		cellStyle.setBorderBottom(BorderStyle.THIN);// Tạo một nét ngang bên dưới cell
		XSSFFont font = workbook.createFont();// Tạo đối tượng font để gán
		font.setBold(true);// Tạo nét Bold
		font.setFontHeight(16);// Tạo kích cỡ font
		cellStyle.setFont(font);// Gán font vào cell

		createCell(row, 0, "User Id", cellStyle);// Bắt đầu tạo các ô của hàng đó
		createCell(row, 1, "E-mail", cellStyle);// row, column index, value - object, cellStyle
		createCell(row, 2, "First Name", cellStyle);
		createCell(row, 3, "Last Name", cellStyle);
		createCell(row, 4, "Roles", cellStyle);
		createCell(row, 5, "Enable", cellStyle);
	}

	private void writeDataLines(List<User> listUsers) {// Hàm ghi các ô trong 1 dãy row
		int rowIndex = 1;// Phải bắt đầu từ hàng 1 bởi vì hàng 0 là hàng header

		XSSFCellStyle cellStyle = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setFontHeight(14);
		cellStyle.setFont(font);

		for (User user : listUsers) {
			XSSFRow row = sheet.createRow(rowIndex++);
			int columnIndex = 0;// Các column index cũng sẽ bắt đầu bằng 0

			createCell(row, columnIndex++, user.getId(), cellStyle);
			createCell(row, columnIndex++, user.getEmail(), cellStyle);
			createCell(row, columnIndex++, user.getFirstName(), cellStyle);
			createCell(row, columnIndex++, user.getLastName(), cellStyle);
			createCell(row, columnIndex++, user.getRoles().toString(), cellStyle);
			createCell(row, columnIndex++, user.isEnabled(), cellStyle);
		}
	}

	public void export(List<User> listUsers, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "application/octet-stream", ".xlsx", "users_");

		writeHeaderLine();// Ghi header
		writeDataLines(listUsers);// Ghi các dòng trong bảng

		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();
	}

}
