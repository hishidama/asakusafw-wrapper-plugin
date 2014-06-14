package jp.hishidama.asakusafw_wrapper.dmdl.excel;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;

import com.asakusafw.testdriver.excel.NullityConditionKind;
import com.asakusafw.testdriver.excel.RuleSheetFormat;
import com.asakusafw.testdriver.excel.TotalConditionKind;
import com.asakusafw.testdriver.excel.ValueConditionKind;

/**
 * @see com.asakusafw.testdata.generator.excel.SheetBuilder
 */
public class FillRuleDataValidation {

	public void fillRuleDataValidation(Sheet sheet) {
		fillRuleTotalCondition(sheet);
		fillRulePropertyConditions(sheet);
	}

	private void fillRuleTotalCondition(Sheet sheet) {
		// 全体の比較
		Cell value = getCell(sheet, RuleSheetFormat.TOTAL_CONDITION, 0, 1);
		if (value == null) {
			return;
		}
		String[] options = TotalConditionKind.getOptions();
		setExplicitListConstraint(sheet, options, value.getRowIndex(), value.getColumnIndex(), value.getRowIndex(),
				value.getColumnIndex());
	}

	private void fillRulePropertyConditions(Sheet sheet) {
		int start = RuleSheetFormat.PROPERTY_NAME.getRowIndex() + 1;
		int end = getEndRowIndex(sheet, start, RuleSheetFormat.PROPERTY_NAME.getColumnIndex());
		// 値の比較
		setExplicitListConstraint(sheet, ValueConditionKind.getOptions(), start,
				RuleSheetFormat.VALUE_CONDITION.getColumnIndex(), end, RuleSheetFormat.VALUE_CONDITION.getColumnIndex());
		// NULLの比較
		setExplicitListConstraint(sheet, NullityConditionKind.getOptions(), start,
				RuleSheetFormat.NULLITY_CONDITION.getColumnIndex(), end,
				RuleSheetFormat.NULLITY_CONDITION.getColumnIndex());
	}

	private int getEndRowIndex(Sheet sheet, int start, int columnIndex) {
		for (int i = start; i < Integer.MAX_VALUE; i++) {
			Cell cell = getCell(sheet, i, columnIndex);
			if (cell == null) {
				return i - 1;
			}
		}
		return Integer.MAX_VALUE;
	}

	private Cell getCell(Sheet sheet, RuleSheetFormat item, int rowOffset, int columnOffset) {
		return getCell(sheet, item.getRowIndex() + rowOffset, item.getColumnIndex() + columnOffset);
	}

	private Cell getCell(Sheet sheet, int rowIndex, int columnIndex) {
		Row row = sheet.getRow(rowIndex);
		if (row == null) {
			return null;
		}
		Cell cell = row.getCell(columnIndex);
		return cell;
	}

	private void setExplicitListConstraint(Sheet sheet, String[] list, int firstRow, int firstCol, int lastRow,
			int lastCol) {
		assert sheet != null;
		assert list != null;
		CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
		DVConstraint constraint = DVConstraint.createExplicitListConstraint(list);
		DataValidationHelper helper = sheet.getDataValidationHelper();
		DataValidation validation = helper.createValidation(constraint, addressList);
		validation.setEmptyCellAllowed(true);
		validation.setSuppressDropDownArrow(false);
		sheet.addValidationData(validation);
	}
}
