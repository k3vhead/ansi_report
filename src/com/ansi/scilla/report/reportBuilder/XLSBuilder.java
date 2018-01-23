package com.ansi.scilla.report.reportBuilder;

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class XLSBuilder extends AbstractXLSBuilder {


	
	private static final long serialVersionUID = 1L;

	
	private XLSBuilder(StandardReport report) {
		super(report);
	}
	
	private XLSBuilder(StandardReport report, XSSFSheet sheet){
		super(report);
	}
	
	private void buildReport(XSSFSheet sheet) throws Exception {
		sheet.setMargin(XSSFSheet.BottomMargin, this.marginBottom);
		sheet.setMargin(XSSFSheet.TopMargin, this.marginTop);
		sheet.setMargin(XSSFSheet.RightMargin, this.marginRight);
		sheet.setMargin(XSSFSheet.LeftMargin, this.marginLeft);

		if ( this.report instanceof StandardSummaryReport ) {
			XLSReportBuilderUtils.makeSummaryHeader((StandardSummaryReport)report, reportStartLoc, sheet);
		} else {
			XLSReportBuilderUtils.makeStandardHeader((StandardReport)report, reportStartLoc, sheet);
		}
//		makeHeader(sheet);

		makeColumnHeader(sheet);		
		makeDetails(sheet);
		makeFinalSubtotal(sheet);
		super.makeSummary(sheet);	
		sheet.setFitToPage(true);
		sheet.getPrintSetup().setFitWidth((short)1);		
		sheet.getPrintSetup().setFitHeight((short)0);
	}


	/**
	 * Use XLSReportBuilderUtils.makeStandardHeader()
	 * 
	 * @deprecated
	 * @param sheet The sheet upon which to put the header
	 * @throws Exception Something bad happened
	 */
	protected void makeHeader(XSSFSheet sheet) throws Exception {
		StandardReport report = (StandardReport)this.report; 
		int headerRowCount = makeHeaderRowCount();
		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
		sheet.getPrintSetup().setLandscape(report.getReportOrientation().equals(ReportOrientation.LANDSCAPE));
		sheet.getPrintSetup().setFitWidth((short)1);

		SimpleDateFormat dateFormatTime = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss");
		SimpleDateFormat dateFormatStandard = new SimpleDateFormat("yyyy/mm/dd");
		dateFormatTime.format(runDate);
		dateFormatStandard.format(startDate);
		dateFormatStandard.format(endDate);
		
		XSSFRow row = null;
		XSSFCell cell = null;
		
		int startingRow = this.reportStartLoc.rowIndex;
		
		makeHeaderRow(startingRow, report.getHeaderLeft(), report.getBanner(), rf.cellStyleReportBanner, report.getHeaderRight(), sheet);
		if ( headerRowCount > 1 ) {
			makeHeaderRow(startingRow + 1, report.getHeaderLeft(), report.getTitle(), rf.cellStyleReportTitle, report.getHeaderRight(), sheet);	
		}
		if ( headerRowCount > 2 ) {
			makeHeaderRow(startingRow + 2, report.getHeaderLeft(), report.getSubtitle(), rf.cellStyleReportSubTitle, report.getHeaderRight(), sheet);
		}
		if ( headerRowCount > 3 ) {
			for ( int i=startingRow + 3;i<headerRowCount;i++) {
				makeHeaderRow(i, report.getHeaderLeft(), "", rf.cellStyleStandardCenter, report.getHeaderRight(), sheet);
			}
		}
		
		if ( ! StringUtils.isBlank(report.getHeaderNotes())) {			
			row = XLSReportBuilderUtils.makeRow(sheet, startingRow + headerRowCount); //sheet.createRow(startingRow + headerRowCount);
			String reportNote = this.report.getHeaderNotes();
			Integer endCell = report.getHeaderRow().length + 1;
			sheet.addMergedRegion(new CellRangeAddress(headerRowCount, headerRowCount, 0, endCell));
			cell = row.createCell(0);
			cell.setCellStyle(rf.cellStyleReportNote);
			cell.setCellValue(reportNote);
			row.setHeight(XLSReportFormatter.calculateRowHeight(sheet, endCell, reportNote));
		}
		
		int numberOfHeaderRows = Math.max(3, startingRow + headerRowCount); // banner + title + subtitle is the minimum
		numberOfHeaderRows++;  // need to include headers + column labels
		sheet.setRepeatingRows(new CellRangeAddress(0,numberOfHeaderRows, 0, report.getHeaderRow().length));
	    
		Footer footer = sheet.getFooter();
		footer.setCenter("Page &P of &N");
	}
	





	private void makeColumnHeader(XSSFSheet sheet) {
		StandardReport report = (StandardReport)this.report;
		XSSFRow row = null;
		XSSFCell cell = null;
		
		int rowNum = this.reportStartLoc.rowIndex + XLSReportBuilderUtils.makeHeaderRowCount(this.report) + 1;
		int columnIndex = this.reportStartLoc.columnIndex;
		int startingColumn = this.reportStartLoc.columnIndex;
		row = XLSReportBuilderUtils.makeRow(sheet, rowNum);  //sheet.createRow(rowNum);

//		row.setHeight(rf.standardHeaderHeight);
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			if ( i == 1 ) {
				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startingColumn, startingColumn + 1));
				columnIndex++;
			}
			if ( i == report.getHeaderRow().length - 1 ) {
				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startingColumn + report.getHeaderRow().length, startingColumn + report.getHeaderRow().length+1));
			}
			cell = row.createCell(columnIndex);
			cell.setCellValue(columnHeader.getLabel());
			cell.setCellStyle(rf.cellStyleColHdrLeft);
			columnIndex++;
		}
	}

	private void makeDetails(XSSFSheet sheet) throws Exception {
		XSSFRow row = null;
		StandardReport report = (StandardReport)this.report;
		
		int rowNum = this.reportStartLoc.rowIndex + XLSReportBuilderUtils.makeHeaderRowCount(this.report) + 2;
		for ( Object dataRow : report.getDataRows() ) {
			rowNum = makeSubtotal(report, sheet, dataRow, rowNum);
			row = XLSReportBuilderUtils.makeRow(sheet, rowNum);   //sheet.createRow(rowNum);	
			int columnIndex = this.reportStartLoc.columnIndex;
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];				
				Object value = makeDisplayData(columnHeader, dataRow);
				super.doSummaries(columnHeader, value);
				if ( i == 1 ) {
					columnIndex++;
					Integer startMerge = this.reportStartLoc.columnIndex;
					Integer endMerge = this.reportStartLoc.columnIndex + 1;
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startMerge, endMerge)); 
				}
				if ( i == report.getHeaderRow().length - 1 ) {
					Integer startMerge = this.reportStartLoc.columnIndex + report.getHeaderRow().length;
					Integer endMerge = this.reportStartLoc.columnIndex + report.getHeaderRow().length + 1;
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startMerge, endMerge));
				}
				super.populateCell(columnHeader, value, columnIndex, dataRow, row);
				columnIndex++;

			}
			rowNum++;
		}
		for ( int i = 0; i < report.getHeaderRow().length+4; i++ ) {
			sheet.autoSizeColumn(i);
		}
	}




	

	
	public static XSSFSheet build(StandardReport report, XSSFWorkbook workbook) throws Exception {	
		XLSBuilder builder = new XLSBuilder(report);
		builder.makeFormatters(workbook);
		XSSFSheet sheet = workbook.createSheet();		
		
		builder.buildReport(sheet);
		return sheet;
	}
	
	public static XSSFWorkbook build(StandardReport report) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		build(report, workbook);
		return workbook;
	}

	public static void build(StandardReport report, XSSFSheet sheet, ReportStartLoc reportStartLoc) throws Exception {
		XLSBuilder builder = new XLSBuilder(report);
		builder.setReportStartLoc(reportStartLoc);
		builder.makeFormatters(sheet.getWorkbook());
		builder.buildReport(sheet);
	}

}
