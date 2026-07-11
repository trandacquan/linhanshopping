package com.linhanshopping.backend.category.export;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.linhanshopping.backend.AbstractExporter;
import com.linhanshopping.common.entity.Category;

// Xuất danh sách Category ra file .csv, dùng thư viện Super CSV.
// Kế thừa AbstractExporter để dùng chung hàm setResponseHeader (đặt tên file + content-type tải về).
public class CategoryCsvExporter extends AbstractExporter {

	public void export(List<Category> listCategories, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "text/csv", ".csv", "categories_");

		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

		// csvHeader: tên cột hiển thị trong file csv
		// fieldMapping: tên property tương ứng trong entity Category (dùng getter id/name/alias/enabled)
		String[] csvHeader = { "Category Id", "Category Name", "Alias", "Enabled" };
		String[] fieldMapping = { "id", "name", "alias", "enabled" };

		csvWriter.writeHeader(csvHeader);

		for (Category category : listCategories) {
			csvWriter.write(category, fieldMapping); // csvWriter tự gọi getId()/getName()/getAlias()/isEnabled() theo fieldMapping
		}

		csvWriter.close();
	}

}
