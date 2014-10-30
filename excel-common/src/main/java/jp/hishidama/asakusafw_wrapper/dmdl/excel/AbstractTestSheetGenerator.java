package jp.hishidama.asakusafw_wrapper.dmdl.excel;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

public abstract class AbstractTestSheetGenerator extends TestSheetGenerator {

	protected void generateIndexSheet(Workbook workbook, List<SheetInfo> list) {
		IndexSheetGenerator gen = new IndexSheetGenerator(workbook, list);
		gen.generate();
	}
}
