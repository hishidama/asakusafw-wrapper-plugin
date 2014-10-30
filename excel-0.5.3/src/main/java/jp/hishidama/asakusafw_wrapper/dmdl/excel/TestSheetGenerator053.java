package jp.hishidama.asakusafw_wrapper.dmdl.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.testdata.generator.excel.SheetBuilder;
import com.asakusafw.testdata.generator.excel.WorkbookGenerator;

public class TestSheetGenerator053 extends AbstractTestSheetGenerator {

	@Override
	protected void generate(Map<String, List<SheetInfo>> bookMap) throws IOException {
		for (Entry<String, List<SheetInfo>> entry : bookMap.entrySet()) {
			String fileName = entry.getKey();
			List<SheetInfo> list = entry.getValue();

			SpreadsheetVersion spreadsheetVersion = getSpreadsheetVersion(fileName);
			Workbook workbook = WorkbookGenerator.createEmptyWorkbook(spreadsheetVersion);

			for (SheetInfo sheet : list) {
				ModelDeclaration model = findModelDeclaration(sheet.getSrcModelName());
				SheetBuilder builder = new SheetBuilder(workbook, spreadsheetVersion, model);
				if ("rule".equals(sheet.getSrcSheetName())) {
					builder.addRule(sheet.getDstSheetName());
				} else {
					builder.addData(sheet.getDstSheetName());
				}
			}

			generateIndexSheet(workbook, list);

			FileOutputStream fos = new FileOutputStream(fileName);
			try {
				workbook.write(fos);
			} finally {
				fos.close();
			}
		}
	}

	private SpreadsheetVersion getSpreadsheetVersion(String fileName) {
		if (fileName.endsWith(".xlsx")) {
			return SpreadsheetVersion.EXCEL2007;
		} else {
			return SpreadsheetVersion.EXCEL97;
		}
	}
}
