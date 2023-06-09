package com.linhanshopping.backend.user.export;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.linhanshopping.backend.AbstractExporter;
import com.linhanshopping.common.entity.User;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class UserPdfExporter extends AbstractExporter {

	public void writeTableHeader(PdfPTable table) {// Tạo ra một header
		PdfPCell cell = new PdfPCell();// Khởi tạo một cell
		cell.setBackgroundColor(Color.GRAY);// Khởi tạo màu nền cho thanh header
		cell.setPadding(5);// Thiết lập padding cho chữ trong header

		Font font = FontFactory.getFont(FontFactory.HELVETICA);// Thiết lập font Helvetica cho chữ header
		font.setColor(Color.WHITE);// Thiết lập màu White cho chữ trong header

		cell.setPhrase(new Phrase("ID", font));// Thiết lập chữ và thêm vào các ô trong header
		table.addCell(cell);

		cell.setPhrase(new Phrase("E-mail", font));
		table.addCell(cell);

		cell.setPhrase(new Phrase("First Name", font));
		table.addCell(cell);

		cell.setPhrase(new Phrase("Last Name", font));
		table.addCell(cell);

		cell.setPhrase(new Phrase("Roles", font));
		table.addCell(cell);

		cell.setPhrase(new Phrase("Enabled", font));
		table.addCell(cell);
	}

	private void writeTableData(PdfPTable table, List<User> listUsers) {// Tạo ra một table trong pdf file
		for (User user : listUsers) {// Chạy vòng lặp trong listUser để lấy dữ liệu của từng trường trong users
			table.addCell(String.valueOf(user.getId()));// Lấy trường id và chuyển về kiểu String và add vào cell
			table.addCell(user.getEmail());// Lấy trường email và add vào cell
			table.addCell(user.getFirstName());// Lấy trường first name và add vào cell
			table.addCell(user.getLastName());// Lấy trường last name và add vào cell
			table.addCell(user.getRoles().toString());// Lấy trường Roles và add vào cell
			table.addCell(String.valueOf(user.isEnabled()));// Lấy trường enable là trg boolean, chuyển về string và add
															// vào cell
		}
	}

	public void export(List<User> listUsers, HttpServletResponse response) throws IOException {//Hàm export dữ liệu ra pdf
		super.setResponseHeader(response, "application/pdf", ".pdf", "users_");//Tham chiếu đến AbstractExporter để định dạng kiểu xuất, tên file, ...

		Document document = new Document(PageSize.A4);//Tạo ra một tài liệu A4 mới
		PdfWriter.getInstance(document, response.getOutputStream());

		document.open();

		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(16);
		font.setColor(Color.BLUE);

		Paragraph paragraph = new Paragraph("List of Users", font);
		paragraph.setAlignment(Paragraph.ALIGN_CENTER);

		document.add(paragraph);

		PdfPTable table = new PdfPTable(6);//Tạo ra một table có 6 cột
		table.setWidthPercentage(100f);
		table.setSpacingBefore(10);
		table.setWidths(new float[] { 1.2f, 3.5f, 3.0f, 3.0f, 3.0f, 1.7f });

		writeTableHeader(table);
		writeTableData(table, listUsers);

		document.add(table);
		document.close();
	}
}
