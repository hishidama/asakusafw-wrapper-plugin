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

public class TestSheetGenerator053 extends TestSheetGenerator {

	@Override
	protected void generate(Map<String, List<SheetInfo>> map) throws IOException {
		for (Entry<String, List<SheetInfo>> entry : map.entrySet()) {
			SpreadsheetVersion spreadsheetVersion = getSpreadsheetVersion(entry.getKey());
			Workbook workbook = WorkbookGenerator.createEmptyWorkbook(spreadsheetVersion);

			for (SheetInfo info : entry.getValue()) {
				ModelDeclaration model = findModelDeclaration(info.srcModelName);
				SheetBuilder builder = new SheetBuilder(workbook, spreadsheetVersion, model);
				if ("rule".equals(info.srcSheetName)) {
					builder.addRule(info.dstSheetName);
				} else {
					builder.addData(info.dstSheetName);
				}
			}

			FileOutputStream fos = new FileOutputStream(entry.getKey());
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
