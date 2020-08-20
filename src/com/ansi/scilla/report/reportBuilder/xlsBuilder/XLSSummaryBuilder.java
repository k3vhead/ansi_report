package com.ansi.scilla.report.reportBuilder.xlsBuilder;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;

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
	@Deprecated
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
