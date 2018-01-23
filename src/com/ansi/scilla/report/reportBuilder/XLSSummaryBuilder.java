package com.ansi.scilla.report.reportBuilder;

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSSummaryBuilder extends AbstractXLSBuilder {

	private static final long serialVersionUID = 1L;
	

	public XLSSummaryBuilder(StandardSummaryReport report) {
		super(report);
	}

	private void buildReport(XSSFSheet sheet) throws Exception {

		StandardSummaryReport report = (StandardSummaryReport)this.report;
		sheet.setMargin(XSSFSheet.BottomMargin, this.marginBottom);
		sheet.setMargin(XSSFSheet.TopMargin, this.marginTop);
		sheet.setMargin(XSSFSheet.RightMargin, this.marginRight);
		sheet.setMargin(XSSFSheet.LeftMargin, this.marginLeft);
		
		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
		sheet.getPrintSetup().setLandscape(report.getReportOrientation().equals(ReportOrientation.LANDSCAPE));
		
		boolean hasCompanySummary = report.hasCompanySummary();
		boolean hasRegionSummary = report.hasRegionSummary();
		boolean hasDivisionSummary = report.hasDivisionSummary();
		
		ReportStartLoc startLoc = new ReportStartLoc(0,0);		
//		Integer numberOfHeaderRows = makeHeader(sheet);
		Integer numberOfHeaderRows = XLSReportBuilderUtils.makeSummaryHeader((StandardSummaryReport)this.getReport(), new ReportStartLoc(0,0), sheet);
		// number of data rows + column headers + summary header
		Integer numberOfCompanyRows = hasCompanySummary ? report.getCompanyData().size() + 2 : 0;
		
		Integer numberOfCompanyColumns = hasCompanySummary ? report.getCompanySummary().getHeaderRow().length : 0;
		Integer numberOfRegionColumns = hasRegionSummary ? report.getRegionSummary().getHeaderRow().length : 0;
		Integer numberOfDivisionColumns = hasDivisionSummary? report.getDivisionSummary().getHeaderRow().length : 0;

		
		startLoc.rowIndex = numberOfHeaderRows;   // top left of either company or region		
		if ( hasCompanySummary ) {
			makeReport(sheet, report.getCompanySummary(), startLoc);
			startLoc.rowIndex = startLoc.rowIndex + numberOfCompanyRows + 2;  // data + summary
		}
		
		if ( hasRegionSummary ) {
			boolean printColumnHeaders = ! hasCompanySummary;
			makeReport(sheet, report.getRegionSummary(), startLoc, printColumnHeaders);
		}
		
		if ( hasDivisionSummary ) {
			startLoc.rowIndex = numberOfHeaderRows;
			if ( hasCompanySummary || hasRegionSummary ) {
				startLoc.columnIndex = Math.max(report.getCompanySummary().getHeaderRow().length, report.getRegionSummary().getHeaderRow().length) + 1;
			}
			makeReport(sheet, report.getDivisionSummary(), startLoc);
		}
	
		sheet.setFitToPage(true);
		sheet.getPrintSetup().setFitWidth((short)1);		
		sheet.getPrintSetup().setFitHeight((short)0);	
		
		Integer maxColumns = Math.max(numberOfCompanyColumns, numberOfRegionColumns) + numberOfDivisionColumns + 1;
		for ( int i = 0; i < maxColumns; i++ ) {
			sheet.autoSizeColumn(i);
		}
	}


	/**
	 * Use XLSReportBuilderUtils.makeSummaryHeader
	 * @deprecated
	 * @param sheet Excel sheet to be populated
	 * @return Number of header Rows
	 * @throws Exception Something bad happened
	 */
	protected Integer makeHeader(XSSFSheet sheet) throws Exception {
		StandardSummaryReport report = (StandardSummaryReport)this.report; 
		int headerRowCount = makeHeaderRowCount();
		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
//		boolean reportIsLandscape = report.getReportOrientation().equals(ReportOrientation.LANDSCAPE);
//		sheet.getPrintSetup().setLandscape(reportIsLandscape);
		sheet.getPrintSetup().setFitWidth((short)1);

		SimpleDateFormat dateFormatTime = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss");
		SimpleDateFormat dateFormatStandard = new SimpleDateFormat("yyyy/mm/dd");
		dateFormatTime.format(runDate);
		dateFormatStandard.format(startDate);
		dateFormatStandard.format(endDate);
		
		XSSFRow row = null;
		XSSFCell cell = null;
		
		makeHeaderRow(0, report.getHeaderLeft(), report.getBanner(), rf.cellStyleReportBanner, report.getHeaderRight(), sheet);
		if ( headerRowCount > 1 ) {
			makeHeaderRow(1, report.getHeaderLeft(), report.getTitle(), rf.cellStyleReportTitle, report.getHeaderRight(), sheet);	
		}
		if ( headerRowCount > 2 ) {
			makeHeaderRow(2, report.getHeaderLeft(), report.getSubtitle(), rf.cellStyleReportSubTitle, report.getHeaderRight(), sheet);
		}
		if ( headerRowCount > 3 ) {
			for ( int i=3;i<headerRowCount;i++) {
				makeHeaderRow(i, report.getHeaderLeft(), "", rf.cellStyleStandardCenter, report.getHeaderRight(), sheet);
			}
		}
		
		if ( ! StringUtils.isBlank(report.getHeaderNotes())) {			
			row = sheet.createRow(headerRowCount);
//			sheet.addMergedRegion(new CellRangeAddress(headerRowCount, headerRowCount, 0, report.getHeaderRow().length + 1));
			sheet.addMergedRegion(new CellRangeAddress(headerRowCount, headerRowCount, 0, 10));
			cell = row.createCell(0);
			cell.setCellStyle(rf.cellStyleReportNote);
			cell.setCellValue(this.report.getHeaderNotes());
		}
		
		int numberOfHeaderRows = Math.max(3, headerRowCount); // banner + title + subtitle is the minimum
		numberOfHeaderRows++;  // need to include headers + column labels
		sheet.setRepeatingRows(new CellRangeAddress(0,numberOfHeaderRows, 0, makeColumnCount(report)));
	    
		Footer footer = sheet.getFooter();
		footer.setCenter("Page &P of &N");
		
		return numberOfHeaderRows;
	}
	

	

	private void makeReport(XSSFSheet sheet, StandardReport report, ReportStartLoc startLoc) throws Exception {
		makeReport(sheet, report, startLoc, true);
	}
	
	private void makeReport(XSSFSheet sheet, StandardReport report, ReportStartLoc startLoc, boolean printColumnHeaders) throws Exception {
		Integer rowIndex = startLoc.rowIndex;
		Integer columnIndex = startLoc.columnIndex;
		super.initializeSummaries(report.getHeaderRow());
		makeReportTitle(report, sheet, rowIndex, columnIndex);
		rowIndex++;
		if ( printColumnHeaders ) {
			makeColumnHeader(report, sheet, rowIndex, startLoc.columnIndex);
			rowIndex++;
		}
		makeDetails(report, sheet, rowIndex, startLoc.columnIndex);
		makeSummary(sheet, report, startLoc, printColumnHeaders);
	}
	
	
	private void makeReportTitle(StandardReport report, XSSFSheet sheet, Integer rowIndex, Integer columnIndex) {
		Integer titleWidth = report.getHeaderRow().length - 1;   //region is zero-based
		XSSFRow row = makeRow(sheet, rowIndex);
		XSSFCell cell = row.createCell(columnIndex);
		
		cell.setCellValue(report.getTitle());
		cell.setCellStyle(rf.cellStyleReportTitle);
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, columnIndex, columnIndex + titleWidth));		
	}

	private void makeColumnHeader(StandardReport report, XSSFSheet sheet, Integer rowIndex, Integer columnIndex) {			
		CellStyle cellStyleHdrLeft = rf.cellStyleColHdrLeft;
		CellStyle cellStyleHdrCenter = rf.cellStyleColHdrCenter;

		XSSFCell cell = null;
		XSSFRow row = makeRow(sheet, rowIndex);
		for ( ColumnHeader columnHeader : report.getHeaderRow() ) {
			cell = row.createCell(columnIndex);
			cell.setCellValue(columnHeader.getLabel());
			if(columnIndex == 0 || columnIndex == report.getHeaderRow().length - 1){
				cell.setCellStyle(cellStyleHdrLeft);
			} else {
				cell.setCellStyle(cellStyleHdrCenter);
			}
			columnIndex++;
		}
	}
	



	
	
	private void makeDetails(StandardReport report, XSSFSheet sheet, Integer rowIndex, Integer columnIndex) throws Exception {
		XSSFRow row = null;
		Integer rowNum = rowIndex;
		for ( Object dataRow : report.getDataRows() ) {
			rowNum = makeSubtotal(report, sheet, dataRow, rowNum);
			row = makeRow(sheet, rowNum);
			Integer columnNumber = columnIndex;
			for ( int headerIndex = 0; headerIndex < report.getHeaderRow().length; headerIndex++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[headerIndex];				
				Object value = makeDisplayData(columnHeader, dataRow);
				super.doSummaries(columnHeader, value);
				super.populateCell(columnHeader, value, columnNumber, dataRow, row);
				columnNumber++;
			}
			rowNum++;
		}
	}

	protected void makeSummary(XSSFSheet sheet, StandardReport report, ReportStartLoc startLoc, boolean printColumnHeaders) throws Exception {
		XSSFRow row = null;
		XSSFCell cell = null;
		int additionalRows = printColumnHeaders ? 2 : 1;   // starting row + summary title + col hdr + data
		int rowNum = startLoc.rowIndex + report.getDataRows().size() + additionalRows;  
		row = makeRow(sheet, rowNum);
		
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			int columnIndex = startLoc.columnIndex + i;			
			cell = row.createCell(columnIndex);
			if ( !columnHeader.getSummaryType().equals(SummaryType.NONE)) {
				cell.setCellValue(makeSummaryData(columnHeader));
				CellStyle summaryStyle = sheet.getWorkbook().createCellStyle();
				summaryStyle.cloneStyleFrom(cellStyles.get(columnHeader.getFormatter()));
				summaryStyle.setFont(rf.fontStandardBlackBold);
				cell.setCellStyle(summaryStyle);
			}
		}
		rowNum++;
	}
	

	/**
	 * Figure out how wide a summary report is, based on existence and width of constituent reports
	 * @deprecated use ReportBuilderUtils.makeColumnCount()
	 * @param report
	 * @return
	 */
	private Integer makeColumnCount(StandardSummaryReport report) {
		Integer companyColumnCount = report.hasCompanySummary() ? report.getCompanySummary().getHeaderRow().length : 0;
		Integer regionColumnCount = report.hasRegionSummary() ? report.getRegionSummary().getHeaderRow().length : 0;
		Integer divisionColumnCount = report.hasDivisionSummary() ? report.getDivisionSummary().getHeaderRow().length : 0;
		Integer colCount = Math.max(companyColumnCount, regionColumnCount) + divisionColumnCount;
		return colCount;
	}

	private XSSFRow makeRow(XSSFSheet sheet, Integer rowIndex) {
		XSSFRow row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
		return row;
	}

	public static XSSFSheet build(StandardSummaryReport report, XSSFWorkbook workbook) throws Exception {	
		XLSSummaryBuilder builder = new XLSSummaryBuilder(report);
		builder.makeFormatters(workbook);
		XSSFSheet sheet = workbook.createSheet();		
		
		builder.buildReport(sheet);
		return sheet;
	}
	
	public static XSSFWorkbook build(StandardSummaryReport report) throws Exception {
		XLSSummaryBuilder builder = new XLSSummaryBuilder(report);
		XSSFWorkbook workbook = new XSSFWorkbook();
		builder.makeFormatters(workbook);
		XSSFSheet sheet = workbook.createSheet();
		

		builder.buildReport(sheet);
		return workbook;
	}
	
	
	
}
