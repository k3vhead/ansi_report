package com.ansi.scilla.report.reportBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.report.htmlTable.HTMLCell;
import com.ansi.scilla.report.htmlTable.HTMLRow;
import com.ansi.scilla.report.htmlTable.HTMLTable;


public abstract class DataDumpReport extends CustomReport {

	private static final long serialVersionUID = 1L;

	protected String sql;
	protected List<String> columnHeaders;
	protected List<List<Object>> dataRows;
	protected Logger logger;
	
	protected Map<String, String> cellStyles;
	protected Map<String, String> cellContentStyles;
	protected Map<String, DataFormats> dataFormatters;
	protected Map<String, CellStyle> xlsStyles;
	
	protected Double marginTop = XLSBuilder.marginTopDefault;
	protected Double marginBottom = XLSBuilder.marginBottomDefault;
	protected Double marginLeft = XLSBuilder.marginLeftDefault;
	protected Double marginRight = XLSBuilder.marginRightDefault;
	
	
	protected DataDumpReport() throws Exception {
		super();		
		this.logger = LogManager.getLogger(this.getClass());
		makeCellStyles();
		makeCellContentStyles();
		makeDataFormatters();
	}
	
	
	@Override
	public Integer getReportWidth() {
		return columnHeaders.size();
	}


	/**
	 * Default formatting for HTML Cells. This method can be overridden for custom formatting
	 */
	private void makeCellStyles() {
		cellStyles = new HashMap<String, String>();
		cellStyles.put("String", HTMLReportFormatter.CSS_DATA_LEFT);
		
		cellStyles.put("Integer", HTMLReportFormatter.CSS_DATA_RIGHT);
		
		cellStyles.put("Double", HTMLReportFormatter.CSS_DATA_RIGHT);
		cellStyles.put("Float", HTMLReportFormatter.CSS_DATA_RIGHT);
		cellStyles.put("BigDecimal", HTMLReportFormatter.CSS_DATA_RIGHT);

		cellStyles.put("Calendar", HTMLReportFormatter.CSS_DATA_LEFT);		
		cellStyles.put("GregorianCalendar", HTMLReportFormatter.CSS_DATA_LEFT);		
		cellStyles.put("Date", HTMLReportFormatter.CSS_DATA_LEFT);		
		cellStyles.put("Midnight", HTMLReportFormatter.CSS_DATA_LEFT);
		cellStyles.put("Timestamp", HTMLReportFormatter.CSS_DATA_LEFT);
	}

	/**
	 * Default formatting for HTML Cells. This method can be overridden for custom formatting
	 */
	private void makeCellContentStyles() {
		cellContentStyles = new HashMap<String, String>();
		
		cellContentStyles.put("String", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
		
		cellContentStyles.put("Integer", HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
		
		cellContentStyles.put("Double", HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
		cellContentStyles.put("Float", HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
		cellContentStyles.put("BigDecimal", HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);

		cellContentStyles.put("Calendar", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);		
		cellContentStyles.put("GregorianCalendar", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);		
		cellContentStyles.put("Date", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);		
		cellContentStyles.put("Midnight", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
		cellContentStyles.put("Timestamp", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
	}

	private void makeXlsStyles(XLSReportFormatter reportFormatter) {
		xlsStyles = new HashMap<String, CellStyle>();
		
		xlsStyles.put("String", reportFormatter.cellStyleStandardLeft);
		
		xlsStyles.put("Integer", reportFormatter.cellStyleStandardNumber);
		
		xlsStyles.put("Double", reportFormatter.cellStyleStandardDecimal);
		xlsStyles.put("Float", reportFormatter.cellStyleStandardDecimal);
		xlsStyles.put("BigDecimal", reportFormatter.cellStyleStandardDecimal);

		xlsStyles.put("Calendar", reportFormatter.cellStyleDateLeft);		
		xlsStyles.put("GregorianCalendar", reportFormatter.cellStyleDateLeft);		
		xlsStyles.put("Date", reportFormatter.cellStyleDateLeft);		
		xlsStyles.put("Midnight", reportFormatter.cellStyleDateLeft);
		xlsStyles.put("Timestamp", reportFormatter.cellStyleDateLeft);
	}

	/**
	 * Default formatting for data content. This method can be overridden for custom formatting
	 */
	private void makeDataFormatters() {
		dataFormatters = new HashMap<String, DataFormats>();
		dataFormatters.put("String", DataFormats.STRING_FORMAT);
		
		dataFormatters.put("Integer", DataFormats.NUMBER_FORMAT);
		
		dataFormatters.put("Double", DataFormats.DECIMAL_FORMAT);
		dataFormatters.put("Float", DataFormats.DECIMAL_FORMAT);
		dataFormatters.put("BigDecimal", DataFormats.DECIMAL_FORMAT);		
		
		dataFormatters.put("Timestamp", DataFormats.DATE_TIME_FORMAT);
		dataFormatters.put("Calendar", DataFormats.DATE_FORMAT);		
		dataFormatters.put("GregorianCalendar", DataFormats.DATE_FORMAT);		
		dataFormatters.put("Date", DataFormats.DATE_FORMAT);		
		dataFormatters.put("Midnight", DataFormats.DATE_FORMAT);
		
	}

	protected void makeReport(Connection conn) throws SQLException {
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		makeHeaders(rsmd);
		makeData(rs, rsmd);
		rs.close();
	}
	
	private void makeHeaders(ResultSetMetaData rsmd) throws SQLException {
		this.columnHeaders = new ArrayList<String>();
		for ( int i = 0; i < rsmd.getColumnCount(); i++ ) {
			int index = i + 1;
			String columnName = rsmd.getColumnName(index);
			String headerText = makeHeaderText(columnName);
			columnHeaders.add(headerText);
		}
		
	}

	private void makeData(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
		this.dataRows = new ArrayList<List<Object>>();
		while ( rs.next() ) {
			List<Object> row = new ArrayList<Object>();
			for ( int i = 0; i < rsmd.getColumnCount(); i++ ) {
				int index = i + 1;
				row.add(rs.getObject(index));
			}
			dataRows.add(row);
		}
	}

	private String makeHeaderText(String columnName) {
		String[] pieces = columnName.split("_");
		List<String> text = new ArrayList<String>();
		for ( String piece : pieces ) {
			text.add(StringUtils.capitalize(piece.toLowerCase()));
		}
		String headerText = StringUtils.join(text.iterator(), " ");
		return headerText;
	}

	@Override
	public XSSFWorkbook makeXLS() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		add2XLS(workbook);
		return workbook;
	}

	@Override
	public void add2XLS(XSSFWorkbook workbook) throws Exception {
		String createdDate = makeCreatedDate();
		XLSReportFormatter reportFormatter = new XLSReportFormatter(workbook);
		makeXlsStyles(reportFormatter);
		
		int rowNum = 0;
		XSSFSheet sheet = workbook.createSheet();
		XSSFRow row = null;
		XSSFCell cell = null;
		
		sheet.setAutobreaks(true);
		XSSFPrintSetup ps = sheet.getPrintSetup();
		ps.setLandscape(super.getReportOrientation().equals(ReportOrientation.LANDSCAPE));
		ps.setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
		sheet.setFitToPage(true);
		ps.setFitWidth((short)1);
		ps.setFitHeight((short)0);
		sheet.setMargin(XSSFSheet.BottomMargin, this.marginBottom);
		sheet.setMargin(XSSFSheet.TopMargin, this.marginTop);
		sheet.setMargin(XSSFSheet.RightMargin, this.marginRight);
		sheet.setMargin(XSSFSheet.LeftMargin, this.marginLeft);
		
		
		sheet.setRepeatingRows(new CellRangeAddress(0, 3, 0, this.columnHeaders.size()));
	    
		Footer footer = sheet.getFooter();
		footer.setCenter("Page &P of &N");
		
		
		row = sheet.createRow(rowNum);
		cell = row.createCell(0);
		cell.setCellValue("Created");		
		cell.setCellStyle(reportFormatter.cellStyleReportHeaderLabelCenter);
		
		cell = row.createCell(1);
		cell.setCellValue(createdDate);
		cell.setCellStyle(reportFormatter.cellStyleStandardLeft);
		
		cell = row.createCell(2);
		sheet.addMergedRegion(new CellRangeAddress(
	            rowNum, //first row (0-based)
	            rowNum, //last row  (0-based)
	            2, //first column (0-based)
	            this.columnHeaders.size() - 2  //last column  (0-based)
	    ));
		cell.setCellValue(super.getBanner());
		cell.setCellStyle(reportFormatter.cellStyleReportBanner);
		
		rowNum++;
		row = sheet.createRow(rowNum);
		cell = row.createCell(2);
		sheet.addMergedRegion(new CellRangeAddress(
	            rowNum, //first row (0-based)
	            rowNum, //last row  (0-based)
	            2, //first column (0-based)
	            this.columnHeaders.size() - 2  //last column  (0-based)
	    ));
		cell.setCellValue(super.getTitle());
		cell.setCellStyle(reportFormatter.cellStyleReportTitle);
		
		rowNum++;
		row = sheet.createRow(rowNum);
		for ( int columnIndex = 0; columnIndex < this.columnHeaders.size(); columnIndex++ ) {
			cell = row.createCell(columnIndex);
			cell.setCellValue(this.columnHeaders.get(columnIndex));
			cell.setCellStyle(reportFormatter.cellStyleColHdrCenter);
		}
		
		rowNum++;
		for ( List<Object> dataRow : this.dataRows ) {

			/**
			 * The "merged region thing here needs to be re-examined. WIthout it, the page banners
			 * get really weird. With it, there can be weirdness with short/long column 0 values.
			 */
			row = sheet.createRow(rowNum);
//			sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
			for ( int columnIndex = 0; columnIndex < dataRow.size(); columnIndex++ ) {
//				int xlsIndex = columnIndex > 0 ? columnIndex+1 : columnIndex; //merge cols 0 & 1 so page banner is pretty
				int xlsIndex = columnIndex;
				cell = row.createCell(xlsIndex);
				Object value = dataRow.get(columnIndex);
				if ( value == null ) {
					cell.setCellValue("");
					cell.setCellStyle(reportFormatter.cellStyleStandardLeft);
				} else {
					XLSBuilder.setCellValue(cell, value);
				}
			}
			rowNum++;
		}
		
		for ( int i=0; i < this.columnHeaders.size(); i++ ) {
			sheet.autoSizeColumn(i);
		}
	}

	@Override
	public String makeHTML() throws Exception {
		List<String> reportLineList = new ArrayList<String>();
		reportLineList.add("<html>\n<head>");
		reportLineList.add("</head>\n<body>");
		int rowNum = 0;
		HTMLTable sheet = new HTMLTable();
		HTMLRow row = null;
		HTMLCell cell = null;
		
		row = sheet.createRow(rowNum);
		cell = row.createCell(0);
		cell.setCellValue("Created");
		cell.setCellStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_HEADER_LEFT_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		String createdDate = makeCreatedDate();
		cell = row.createCell(1);
		cell.setCellValue(createdDate);
		cell.setCellStyle(HTMLReportFormatter.CSS_DATA_LEFT);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		int bannerWidth = this.columnHeaders.size() - 4; // total width minus created date on both sides (so it's centered)
		if ( bannerWidth < 0 ) {
			bannerWidth = 1;
		}
		cell = row.createCell(2);
		cell.setColspan(bannerWidth);
		cell.setCellValue(super.getBanner());
		cell.setCellStyle(HTMLReportFormatter.CSS_BANNER);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_BANNER_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		rowNum++;
		row = sheet.createRow(rowNum);
		cell = row.createCell(0);
		cell.setCellValue("");
		cell.setCellStyle(HTMLReportFormatter.CSS_TITLE);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_TITLE_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		cell = row.createCell(1);
		cell.setCellValue("");
		cell.setCellStyle(HTMLReportFormatter.CSS_TITLE);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_TITLE_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		
		cell = row.createCell(2);
		cell.setColspan(bannerWidth);
		cell.setCellValue(super.getTitle());
		cell.setCellStyle(HTMLReportFormatter.CSS_TITLE);
		cell.setCellContentStyle(HTMLReportFormatter.CSS_TITLE_TEXT);
		cell.setDataFormats(DataFormats.STRING_FORMAT);
		
		rowNum++;
		row = sheet.createRow(rowNum);
		for ( int columnIndex = 0; columnIndex < this.columnHeaders.size(); columnIndex++ ) {
			cell = row.createCell(columnIndex);
			cell.setCellValue(this.columnHeaders.get(columnIndex));
			cell.setCellStyle(HTMLReportFormatter.CSS_COLHDR);
			cell.setCellContentStyle(HTMLReportFormatter.CSS_COLHDR_TEXT);
			cell.setDataFormats(DataFormats.STRING_FORMAT);
		}
		
		rowNum++;
		for ( List<Object> dataRow : this.dataRows ) {
			row = sheet.createRow(rowNum);
			for ( int columnIndex = 0; columnIndex < dataRow.size(); columnIndex++ ) {
				cell = row.createCell(columnIndex);
				Object value = dataRow.get(columnIndex);
				if ( value == null ) {
					cell.setCellValue("&nbsp;");
					cell.setDataFormats(DataFormats.STRING_FORMAT);
				} else {
					String dataClass = value.getClass().getSimpleName();
					cell.setCellValue(value);
					if ( this.cellStyles.containsKey(dataClass)) {
						cell.setCellStyle(cellStyles.get(dataClass));
					} else {
						throw new InvalidFormatException("Missing cell style for " + dataClass);
					}
					if ( this.cellContentStyles.containsKey(dataClass)) {
						cell.setCellContentStyle(cellContentStyles.get(dataClass));
					} else {
						throw new InvalidFormatException("Missing content style for " + dataClass);
					}
					if ( this.dataFormatters.containsKey(dataClass)) {
						cell.setDataFormats(dataFormatters.get(dataClass));
					} else {
						throw new InvalidFormatException("Missing formatter for " + dataClass);
					}
				}
			}
			rowNum++;
		}
		reportLineList.add(sheet.makeHTML());
		
		reportLineList.add("</body>\n</html>");
		return StringUtils.join(reportLineList.iterator(), "\n");
	}

	private String makeCreatedDate() {
		Calendar today = Calendar.getInstance(new AnsiTime());
		DateFormatter formatter = (DateFormatter)DataFormats.DETAIL_TIME_FORMAT.formatter();
		String createdDate = formatter.format(today);
		return createdDate;
	}
		

	public class InvalidFormatException extends Exception {

		private static final long serialVersionUID = 1L;

		public InvalidFormatException(String message) {
			super(message);
		}
		
	}
}
