package com.ansi.scilla.report.reportBuilder.reportType;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.htmlBuilder.HTMLReportFormatter;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.AnsiPCell;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportBuilderUtils;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportFormatter;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportHeader;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSReportFormatter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


public abstract class DataDumpReport extends CustomReport {

	private static final long serialVersionUID = 1L;

	protected String sql;
	protected List<String> columnHeaders;
	protected List<List<Object>> dataRows;
	protected Logger logger;
	
	protected Map<String, String> htmlCellStyles;
	protected Map<String, String> htmlCellContentStyles;
	protected Map<String, DataFormats> dataFormatters;
	protected Map<String, CellStyle> xlsStyles;
	protected Map<String, Integer> pdfCellStyles;
	
	protected Double marginTop = XLSBuilder.marginTopDefault;
	protected Double marginBottom = XLSBuilder.marginBottomDefault;
	protected Double marginLeft = XLSBuilder.marginLeftDefault;
	protected Double marginRight = XLSBuilder.marginRightDefault;
	
	
	protected DataDumpReport() throws Exception {
		super();		
		this.logger = LogManager.getLogger(this.getClass());
		makeHtmlCellStyles();
		makePdfCellStyles();
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
	private void makeHtmlCellStyles() {
		htmlCellStyles = new HashMap<String, String>();
		htmlCellStyles.put("String", HTMLReportFormatter.CSS_DATA_LEFT);
		
		htmlCellStyles.put("Integer", HTMLReportFormatter.CSS_DATA_RIGHT);
		
		htmlCellStyles.put("Double", HTMLReportFormatter.CSS_DATA_RIGHT);
		htmlCellStyles.put("Float", HTMLReportFormatter.CSS_DATA_RIGHT);
		htmlCellStyles.put("BigDecimal", HTMLReportFormatter.CSS_DATA_RIGHT);

		htmlCellStyles.put("Calendar", HTMLReportFormatter.CSS_DATA_LEFT);		
		htmlCellStyles.put("GregorianCalendar", HTMLReportFormatter.CSS_DATA_LEFT);		
		htmlCellStyles.put("Date", HTMLReportFormatter.CSS_DATA_LEFT);		
		htmlCellStyles.put("Midnight", HTMLReportFormatter.CSS_DATA_LEFT);
		htmlCellStyles.put("Timestamp", HTMLReportFormatter.CSS_DATA_LEFT);
	}

	
	private void makePdfCellStyles() {
		pdfCellStyles = new HashMap<String, Integer>();
		pdfCellStyles.put("String", Element.ALIGN_LEFT);
		
		pdfCellStyles.put("Integer", Element.ALIGN_RIGHT);
		
		pdfCellStyles.put("Double", Element.ALIGN_RIGHT);
		pdfCellStyles.put("Float", Element.ALIGN_RIGHT);
		pdfCellStyles.put("BigDecimal", Element.ALIGN_RIGHT);

		pdfCellStyles.put("Calendar", Element.ALIGN_LEFT);		
		pdfCellStyles.put("GregorianCalendar", Element.ALIGN_LEFT);		
		pdfCellStyles.put("Date", Element.ALIGN_LEFT);		
		pdfCellStyles.put("Midnight", Element.ALIGN_LEFT);
		pdfCellStyles.put("Timestamp", Element.ALIGN_LEFT);		
	}


	/**
	 * Default formatting for HTML Cells. This method can be overridden for custom formatting
	 */
	private void makeCellContentStyles() {
		htmlCellContentStyles = new HashMap<String, String>();
		
		htmlCellContentStyles.put("String", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
		
		htmlCellContentStyles.put("Integer", HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
		
		htmlCellContentStyles.put("Double", HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
		htmlCellContentStyles.put("Float", HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
		htmlCellContentStyles.put("BigDecimal", HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);

		htmlCellContentStyles.put("Calendar", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);		
		htmlCellContentStyles.put("GregorianCalendar", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);		
		htmlCellContentStyles.put("Date", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);		
		htmlCellContentStyles.put("Midnight", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
		htmlCellContentStyles.put("Timestamp", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
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

	/**
	 * Use makeReport(ResultSet) instead
	 * @param conn
	 * @throws SQLException
	 */
	protected void makeReport(Connection conn) throws SQLException {
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(sql);
		makeReport(rs);
	}
	
	protected void makeReport(ResultSet rs) throws SQLException {
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
					String dataClass = value.getClass().getSimpleName();
					XLSBuilder.setCellValue(cell, value);
					cell.setCellStyle(xlsStyles.get(dataClass));
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
					if ( this.htmlCellStyles.containsKey(dataClass)) {
						cell.setCellStyle(htmlCellStyles.get(dataClass));
					} else {
						throw new InvalidFormatException("Missing cell style for " + dataClass);
					}
					if ( this.htmlCellContentStyles.containsKey(dataClass)) {
						cell.setCellContentStyle(htmlCellContentStyles.get(dataClass));
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

	
	public ByteArrayOutputStream makePDF() throws DocumentException, Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document(PageSize.LETTER.rotate(), PDFReportFormatter.marginLeft, PDFReportFormatter.marginRight, PDFReportFormatter.marginTopDatadump, PDFReportFormatter.marginBottom);
		PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
		pdfWriter.setPageEvent(new PDFReportHeader(this));
		document.open();

		if ( dataRows == null || dataRows.size() == 0 ) {
			makeEmptyReport(document);
		} else {
			makeDetailRows(document);
		}
		document.close();

		return baos;
	}
	
	private void makeEmptyReport(Document document) throws DocumentException {
		document.add(new Chunk("This report contains no data", PDFReportFormatter.fontStandardBlack));
	}


	private void makeDetailRows(Document document) throws DocumentException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		PdfPTable dataTable = new PdfPTable(dataRows.get(0).size());
		dataTable.setWidthPercentage(100F);
//		dataTable.setLockedWidth(true);
		
		for ( int columnIndex = 0; columnIndex < this.columnHeaders.size(); columnIndex++ ) {
			PdfPCell cell = new AnsiPCell(new Phrase(new Chunk(this.columnHeaders.get(columnIndex), PDFReportFormatter.fontStandardWhiteBold)));
			cell.setPaddingTop(0F);
			cell.setPaddingBottom(4F);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			cell.setBackgroundColor(BaseColor.BLACK);
			dataTable.addCell(cell);
		}
		
		for ( List<Object> dataRow : this.dataRows ) {
			for ( int columnIndex = 0; columnIndex < dataRow.size(); columnIndex++ ) {
				Object value = dataRow.get(columnIndex);
				if ( value == null ) {
					PdfPCell cell = new AnsiPCell(new Phrase(""));
					dataTable.addCell(cell);
				} else {
					String dataClass = value.getClass().getSimpleName();
					DataFormats dataFormats = dataFormatters.get(dataClass);					
					String display = PDFReportBuilderUtils.formatValue(dataFormats, value);
					Phrase content = new Phrase(new Chunk(display, PDFReportFormatter.fontStandardBlack));
					PdfPCell cell = new AnsiPCell(content);
					dataTable.addCell(cell);
				}
			}
		}
		dataTable.setHeaderRows(1);
		document.add(dataTable);

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
