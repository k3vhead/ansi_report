package com.ansi.scilla.report.reportBuilder;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
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
		makeHeader(sheet);
		makeColumnHeader(sheet);		
		makeDetails(sheet);
		makeFinalSubtotal(sheet);
		super.makeSummary(sheet);	
		sheet.setFitToPage(true);
		sheet.getPrintSetup().setFitWidth((short)1);		
		sheet.getPrintSetup().setFitHeight((short)0);
	}



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
			sheet.addMergedRegion(new CellRangeAddress(headerRowCount, headerRowCount, 0, report.getHeaderRow().length + 1));
			cell = row.createCell(0);
			cell.setCellValue(this.report.getHeaderNotes());
		}
		
		int numberOfHeaderRows = Math.max(3, headerRowCount); // banner + title + subtitle is the minimum
		numberOfHeaderRows++;  // need to include headers + column labels
		sheet.setRepeatingRows(new CellRangeAddress(0,numberOfHeaderRows, 0, report.getHeaderRow().length));
	    
		Footer footer = sheet.getFooter();
		footer.setCenter("Page &P of &N");
	}
	
	private void makeHeaderRow(Integer index, List<ReportHeaderCol> headerLeft, String text, CellStyle bannerStyle, List<ReportHeaderCol> headerRight, XSSFSheet sheet) throws Exception {
		StandardReport report = (StandardReport)this.report;
		Integer colCount = report.getHeaderRow().length;
		String[] dataLeft = makeHeaderData(headerLeft, index);
		String[] dataRight = makeHeaderData(headerRight, index);
		
		XSSFRow row = null;
		XSSFCell cell = null;
		
		row = sheet.createRow(index);

//	    row.setHeight(rf.standardDetailHeight);
	    cell = row.createCell(0);
	    cell.setCellValue(dataLeft[0]);
	    cell.setCellStyle(rf.cellStyleReportHeaderLabelLeft);
	    
	    cell = row.createCell(1);
	    cell.setCellValue(dataLeft[1]);
	    cell.setCellStyle(rf.cellStyleStandardLeft);
	    
	    cell = row.createCell(2);
	    cell.setCellValue(text);
	    sheet.addMergedRegion(new CellRangeAddress(index, index, 2, colCount - 1));
	    cell.setCellStyle(bannerStyle);
	    
	    cell = row.createCell(colCount);
	    cell.setCellValue(dataRight[0]);
	    cell.setCellStyle(rf.cellStyleReportHeaderLabelRight);
	    
	    cell = row.createCell(colCount+1);
	    cell.setCellValue(dataRight[1]);
	    cell.setCellStyle(rf.cellStyleStandardRight);
	}




	private void makeColumnHeader(XSSFSheet sheet) {
		StandardReport report = (StandardReport)this.report;
		XSSFRow row = null;
		XSSFCell cell = null;
		
		int rowNum = makeHeaderRowCount() + 1;
		int columnIndex = 0;
		row = sheet.createRow(rowNum);

//		row.setHeight(rf.standardHeaderHeight);
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			if ( i == 1 ) {
				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
				columnIndex++;
			}
			if ( i == report.getHeaderRow().length - 1 ) {
				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, report.getHeaderRow().length, report.getHeaderRow().length+1));
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
		
		int rowNum = makeHeaderRowCount() + 2;
		for ( Object dataRow : report.getDataRows() ) {
			rowNum = makeSubtotal(report, sheet, dataRow, rowNum);
			row = sheet.createRow(rowNum);	
			int columnIndex = 0;
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];				
				Object value = makeDisplayData(columnHeader, dataRow);
				super.doSummaries(columnHeader, value);
				if ( i == 1 ) {
					columnIndex++;
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1)); 
				}
				if ( i == report.getHeaderRow().length - 1 ) {
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, report.getHeaderRow().length, report.getHeaderRow().length+1));
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
		XLSBuilder builder = new XLSBuilder(report);
		XSSFWorkbook workbook = new XSSFWorkbook();
		builder.makeFormatters(workbook);
		XSSFSheet sheet = workbook.createSheet();
		

		builder.buildReport(sheet);
		return workbook;
	}

}
