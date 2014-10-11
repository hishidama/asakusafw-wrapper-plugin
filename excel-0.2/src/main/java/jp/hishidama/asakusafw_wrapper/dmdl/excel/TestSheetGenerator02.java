package jp.hishidama.asakusafw_wrapper.dmdl.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.testdata.generator.excel.SheetBuilder;

public class TestSheetGenerator02 extends AbstractTestSheetGenerator {

	@Override
	protected void generate(Map<String, List<SheetInfo>> bookMap) throws IOException {
		for (Entry<String, List<SheetInfo>> entry : bookMap.entrySet()) {
			String fileName = entry.getKey();
			List<SheetInfo> list = entry.getValue();

			HSSFWorkbook workbook = new HSSFWorkbook();

			generateIndexSheet(workbook, list);

			for (SheetInfo sheet : list) {
				ModelDeclaration model = findModelDeclaration(sheet.getSrcModelName());
				SheetBuilder builder = new SheetBuilder(workbook, model);
				if ("rule".equals(sheet.getSrcSheetName())) {
					builder.addRule(sheet.getDstSheetName());
				} else {
					builder.addData(sheet.getDstSheetName());
				}
			}

			FileOutputStream fos = new FileOutputStream(fileName);
			try {
				workbook.write(fos);
			} finally {
				fos.close();
			}
		}
	}
}
