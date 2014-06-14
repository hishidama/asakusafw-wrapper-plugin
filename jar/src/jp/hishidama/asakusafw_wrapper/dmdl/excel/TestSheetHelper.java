package jp.hishidama.asakusafw_wrapper.dmdl.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestSheetHelper {

	public void copySheets(Map<String, List<Object[]>> map) throws IOException {
		ExcelSheetCopy copy = new ExcelSheetCopy();

		for (Entry<String, List<Object[]>> entry : map.entrySet()) {
			String dstBookName = entry.getKey();
			Workbook dstBook = createNewBook(dstBookName);

			for (Object[] object : entry.getValue()) {
				String dstSheetName = (String) object[0];
				String srcBookName = (String) object[1];
				String srcSheetName = (String) object[2];

				Workbook srcBook = getInputWorkbook(srcBookName);
				Sheet srcSheet = srcBook.getSheet(srcSheetName);
				Sheet dstSheet = dstBook.createSheet(dstSheetName);
				copy.execute(srcSheet, dstSheet);
			}

			save(dstBook, dstBookName);
		}
	}

	protected Workbook createNewBook(String dstBookName) {
		if (dstBookName.endsWith(".xls")) {
			return new HSSFWorkbook();
		} else {
			return new XSSFWorkbook();
		}
	}

	private Map<String, Workbook> inputMap = new HashMap<String, Workbook>();

	private Workbook getInputWorkbook(String name) throws IOException {
		Workbook book = inputMap.get(name);
		if (book == null) {
			File file = new File(name);
			try {
				book = WorkbookFactory.create(file);
				inputMap.put(name, book);
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		return book;
	}

	private void save(Workbook book, String path) throws IOException {
		File file = new File(path);
		FileOutputStream os = new FileOutputStream(file);
		try {
			book.write(os);
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
