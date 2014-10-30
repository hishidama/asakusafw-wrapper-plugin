package jp.hishidama.asakusafw_wrapper.dmdl.excel;

import java.util.List;

import jp.hishidama.asakusafw_wrapper.dmdl.excel.TestSheetGenerator.SheetInfo;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellUtil;

public class IndexSheetGenerator {
	private Workbook workbook;
	private List<SheetInfo> sheetList;

	private Sheet indexSheet;

	public IndexSheetGenerator(Workbook workbook, List<SheetInfo> list) {
		assert list.size() > 0;

		this.workbook = workbook;
		this.sheetList = list;
	}

	public boolean generate() {
		String indexSheetName = sheetList.get(0).getIndexSheetName();
		if (indexSheetName == null) {
			return false;
		}
		this.indexSheet = workbook.createSheet(indexSheetName);
		workbook.setSheetOrder(indexSheetName, 0);
		for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
			workbook.getSheetAt(i).setSelected(false);
		}
		indexSheet.setSelected(true);
		workbook.setActiveSheet(0);

		generateFormatHeader();
		generateClassNameHeader();
		generateSheetTable();
		return true;
	}

	private void generateFormatHeader() {
		CellReference cr = new CellReference("A1");

		Row row = CellUtil.getRow(cr.getRow(), indexSheet);
		{
			Cell cell = CellUtil.getCell(row, cr.getCol());
			cell.setCellValue("Format");
			cell.setCellStyle(getHeaderCellStyle());
		}
		{
			Cell cell = CellUtil.getCell(row, cr.getCol() + 1);
			cell.setCellValue("DMDL EditorX INDEX-0.0.1");
			cell.setCellStyle(getLockCellStyle());
		}
	}

	private void generateClassNameHeader() {
		String className = sheetList.get(0).getFlowClassName();

		CellReference cr = new CellReference("A2");

		Row row = CellUtil.getRow(cr.getRow(), indexSheet);
		{
			Cell cell = CellUtil.getCell(row, cr.getCol());
			cell.setCellValue("Flow Class");
			cell.setCellStyle(getHeaderCellStyle());
		}
		{
			Cell cell = CellUtil.getCell(row, cr.getCol() + 1);
			cell.setCellValue(className);
			cell.setCellStyle(getBorderCellStyle());
		}
	}

	private void generateSheetTable() {
		CellReference cr = new CellReference("A4");

		int sheetNameIndex;
		int propertyNameIndex, propertyIndex, rowIndex, columnIndex, jumpIndex;
		int columnCount;

		{
			Row row = CellUtil.getRow(cr.getRow(), indexSheet);
			int c = cr.getCol();
			{
				sheetNameIndex = c;
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue("sheetName");
				cell.setCellStyle(getHeaderCellStyle());
			}
			{
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue("description");
				cell.setCellStyle(getHeaderCellStyle());
			}
			{
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue("type");
				cell.setCellStyle(getHeaderCellStyle());
			}
			{
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue("modelName");
				cell.setCellStyle(getHeaderCellStyle());
			}
			{
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue("modelDescription");
				cell.setCellStyle(getHeaderCellStyle());
			}
			{ // ハイパーリンク
				int startColumn = c;

				{
					propertyNameIndex = c;
					Cell cell = CellUtil.getCell(row, c++);
					cell.setCellValue("propertyName");
					cell.setCellStyle(getHeaderCellStyle());
				}
				{
					propertyIndex = c;
					Cell cell = CellUtil.getCell(row, c++);
					cell.setCellValue("propertyIndex");
					cell.setCellStyle(getHeaderCellStyle());
				}
				{
					rowIndex = c;
					Cell cell = CellUtil.getCell(row, c++);
					cell.setCellValue("row");
					cell.setCellStyle(getHeaderCellStyle());
				}
				{
					columnIndex = c;
					Cell cell = CellUtil.getCell(row, c++);
					cell.setCellValue("column");
					cell.setCellStyle(getHeaderCellStyle());
				}
				{
					jumpIndex = c;
					Cell cell = CellUtil.getCell(row, c++);
					cell.setCellValue("jump");
					cell.setCellStyle(getHeaderCellStyle());
				}

				{
					int r1 = cr.getRow() - 1;
					Row row1 = CellUtil.getRow(r1, indexSheet);
					Cell cell = CellUtil.getCell(row1, startColumn);
					cell.setCellValue("hyperlink");
					for (int i = startColumn; i < c; i++) {
						CellUtil.getCell(row1, i).setCellStyle(getHeaderCellStyle());
					}

					// セル結合
					CellRangeAddress region = new CellRangeAddress(r1, r1, startColumn, c - 1);
					indexSheet.addMergedRegion(region);
				}
			}

			columnCount = c;
		}

		int r = cr.getRow() + 1;
		for (SheetInfo sheet : sheetList) {
			Row row = CellUtil.getRow(r++, indexSheet);
			int c = cr.getCol();

			boolean isRule = "rule".equals(sheet.getSrcSheetName());
			String sheetNamePos;

			{ // シート名
				sheetNamePos = convertNumToString(row.getRowNum(), c);
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue(sheet.getDstSheetName());
				cell.setCellStyle(getBorderCellStyle());
			}
			{ // 説明
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue(sheet.getDstSheetDescription());
				cell.setCellStyle(getBorderCellStyle());
			}
			{ // rule/data
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue(isRule ? "rule" : "data");
				cell.setCellStyle(getBorderCellStyle());
			}
			{ // モデル名
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue(sheet.getSrcModelName());
				cell.setCellStyle(getBorderCellStyle());
			}
			{ // モデル説明
				Cell cell = CellUtil.getCell(row, c++);
				cell.setCellValue(sheet.getSrcModelDescription());
				cell.setCellStyle(getBorderCellStyle());
			}

			{ // ハイパーリンク
				String propertyRange;
				if (isRule) {
					propertyRange = String.format("'%s'!$A:$A", sheet.getDstSheetName());
				} else {
					propertyRange = String.format("'%s'!$1:$1", sheet.getDstSheetName());
				}

				{ // プロパティー名
					Cell cell = CellUtil.getCell(row, c++);
					cell.setCellStyle(getLockCellStyle());

					// プルダウン
					DataValidationHelper helper = indexSheet.getDataValidationHelper();
					DataValidationConstraint constraint = helper.createFormulaListConstraint(propertyRange);
					CellRangeAddressList region = new CellRangeAddressList(cell.getRowIndex(), cell.getRowIndex(),
							cell.getColumnIndex(), cell.getColumnIndex());
					DataValidation validation = helper.createValidation(constraint, region);
					indexSheet.addValidationData(validation);
				}
				{ // プロパティーのインデックス
					Cell cell = CellUtil.getCell(row, c++);
					cell.setCellStyle(getBorderCellStyle());
					String propertyPos = convertNumToString(row.getRowNum(), propertyNameIndex);
					String formula = String.format("MATCH(%s, %s, 0)", propertyPos, propertyRange);
					cell.setCellFormula(formula);
				}
				{ // 行インデックス
					Cell cell = CellUtil.getCell(row, c++);
					if (isRule) {
						String propertyPos = "$" + convertNumToString(row.getRowNum(), propertyIndex);
						String formula = String.format("IF(ISERROR(%s), 4, %s)", propertyPos, propertyPos);
						cell.setCellFormula(formula);
					} else {
						cell.setCellValue(2);
					}
					cell.setCellStyle(getBorderCellStyle());
				}
				{ // 列インデックス
					Cell cell = CellUtil.getCell(row, c++);
					if (isRule) {
						cell.setCellValue(1);
					} else {
						String propertyPos = "$" + convertNumToString(row.getRowNum(), propertyIndex);
						String formula = String.format("IF(ISERROR(%s), 1, %s)", propertyPos, propertyPos);
						cell.setCellFormula(formula);
					}
					cell.setCellStyle(getBorderCellStyle());
				}
				{ // リンク
					String rowPos = convertNumToString(row.getRowNum(), rowIndex);
					String colPos = convertNumToString(row.getRowNum(), columnIndex);
					String label = String.format("ADDRESS(%s, %s, 4, TRUE, %s)", rowPos, colPos, sheetNamePos);
					String link = String.format("\"#\" & %s", label);
					String formula = String.format("HYPERLINK(%s, %s)", link, label);
					Cell cell = CellUtil.getCell(row, c++);
					cell.setCellFormula(formula);
					cell.setCellStyle(getLinkCellStyle());
				}
			}
		}

		for (int i = 0; i < columnCount; i++) {
			if (i != jumpIndex) {
				indexSheet.autoSizeColumn(i);
			} else {
				indexSheet.setColumnWidth(i,
						indexSheet.getColumnWidth(sheetNameIndex) + indexSheet.getColumnWidth(rowIndex));
			}
		}
	}

	private static String convertNumToString(int row, int column) {
		return CellReference.convertNumToColString(column) + (row + 1);
	}

	private CellStyle headerCellStyle;

	protected CellStyle getHeaderCellStyle() {
		if (headerCellStyle == null) {
			// @see com.asakusafw.testdata.generator.excel.WorkbookInfo
			CellStyle style = indexSheet.getWorkbook().createCellStyle();
			headerCellStyle = style;

			style.setBorderTop(CellStyle.BORDER_THIN);
			style.setBorderBottom(CellStyle.BORDER_THIN);
			style.setBorderLeft(CellStyle.BORDER_THIN);
			style.setBorderRight(CellStyle.BORDER_THIN);

			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			style.setAlignment(CellStyle.ALIGN_CENTER);

			style.setLocked(true);
		}
		return headerCellStyle;
	}

	private CellStyle borderCellStyle;

	protected CellStyle getBorderCellStyle() {
		if (borderCellStyle == null) {
			CellStyle style = indexSheet.getWorkbook().createCellStyle();
			borderCellStyle = style;

			style.setBorderTop(CellStyle.BORDER_THIN);
			style.setBorderBottom(CellStyle.BORDER_THIN);
			style.setBorderLeft(CellStyle.BORDER_THIN);
			style.setBorderRight(CellStyle.BORDER_THIN);
		}
		return borderCellStyle;
	}

	private CellStyle pullCellStyle;

	protected CellStyle getLockCellStyle() {
		if (pullCellStyle == null) {
			CellStyle style = indexSheet.getWorkbook().createCellStyle();
			pullCellStyle = style;

			style.setBorderTop(CellStyle.BORDER_THIN);
			style.setBorderBottom(CellStyle.BORDER_THIN);
			style.setBorderLeft(CellStyle.BORDER_THIN);
			style.setBorderRight(CellStyle.BORDER_THIN);

			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
		}
		return pullCellStyle;
	}

	private CellStyle linkCellStyle;

	protected CellStyle getLinkCellStyle() {
		if (linkCellStyle == null) {
			CellStyle style = indexSheet.getWorkbook().createCellStyle();
			linkCellStyle = style;

			style.setBorderTop(CellStyle.BORDER_THIN);
			style.setBorderBottom(CellStyle.BORDER_THIN);
			style.setBorderLeft(CellStyle.BORDER_THIN);
			style.setBorderRight(CellStyle.BORDER_THIN);

			Font font = indexSheet.getWorkbook().createFont();
			font.setColor(IndexedColors.BLUE.getIndex());
			font.setUnderline(Font.U_SINGLE);
			style.setFont(font);

			style.setLocked(true);
		}
		return linkCellStyle;
	}
}
