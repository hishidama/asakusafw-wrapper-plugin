package jp.hishidama.asakusafw_wrapper.dmdl.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.hishidama.asakusafw_wrapper.dmdl.excel.TestSheetGenerator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.testdata.generator.excel.SheetBuilder;

public class TestSheetGenerator02 extends TestSheetGenerator {

	@Override
	protected void generate(Map<String, List<SheetInfo>> map) throws IOException {
		for (Entry<String, List<SheetInfo>> entry : map.entrySet()) {
			HSSFWorkbook workbook = new HSSFWorkbook();

			for (SheetInfo info : entry.getValue()) {
				ModelDeclaration model = findModelDeclaration(info.srcModelName);
				SheetBuilder builder = new SheetBuilder(workbook, model);
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
}
