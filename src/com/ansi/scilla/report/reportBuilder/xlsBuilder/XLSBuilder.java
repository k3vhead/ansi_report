package com.ansi.scilla.report.reportBuilder.xlsBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.CustomCell;
import com.ansi.scilla.report.reportBuilder.formatter.StringWrapFormatter;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;



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
		if ( this.report instanceof StandardReport ) {
			StandardReport myReport = (StandardReport)report;
			if ( myReport.getAddendum() != null ) {
				logger.log(Level.DEBUG, "Recap");
				makeRecap(sheet);
			}
			
		}
		sheet.setFitToPage(true);
		sheet.getPrintSetup().setFitWidth((short)1);		
		sheet.getPrintSetup().setFitHeight((short)0);
	}


	
	


	private void makeColumnHeader(XSSFSheet sheet) {
		XLSReportBuilderUtils.makeColumnHeader((StandardReport)this.report, this.reportStartLoc, sheet, this.rf);
	}


	

	private void makeDetails(XSSFSheet sheet) throws Exception {
		XSSFRow row = null;
		StandardReport report = (StandardReport)this.report;
		
		int startingRow = this.reportStartLoc.rowIndex + XLSReportBuilderUtils.makeHeaderRowCount(this.report) + 2;
		int rowNum = startingRow;
		for ( Object dataRow : report.getDataRows() ) {
			rowNum = makeSubtotal(report, sheet, dataRow, rowNum);
			row = XLSReportBuilderUtils.makeRow(sheet, rowNum);   //sheet.createRow(rowNum);	
			int columnIndex = this.reportStartLoc.columnIndex;
			if ( this.report instanceof StandardReport ) {
				columnIndex = columnIndex + ((StandardReport)this.report).getFirstDetailColumn();
			}
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];				
				Object value = makeDisplayData(columnHeader, dataRow);
				if(value != null) {
//					String display = makeFormattedDisplayData(columnHeader.getFormatter(), value);
//					if(display != null) {
						if(columnHeader.getMaxCharacters() != null) {
							StringUtils.abbreviate(value.toString(), columnHeader.getMaxCharacters());
						}
//					}
				}
				super.doSummaries(columnHeader, value);
				if ( columnHeader.getColspan() > 0 ) {
					Integer firstColumn = columnIndex;
					Integer lastColumn = firstColumn + columnHeader.getColspan() - 1;
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, firstColumn, lastColumn));
//					columnIndex = columnIndex + columnHeader.getColspan() - 1;
				}
//				if ( i == 1 ) {
//					columnIndex++;
//					Integer startMerge = this.reportStartLoc.columnIndex;
//					Integer endMerge = this.reportStartLoc.columnIndex + 1;
//					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startMerge, endMerge)); 
//				}
//				if ( i == report.getHeaderRow().length - 1 ) {
//					Integer startMerge = this.reportStartLoc.columnIndex + report.getHeaderRow().length;
//					Integer endMerge = this.reportStartLoc.columnIndex + report.getHeaderRow().length + 1;
//					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startMerge, endMerge));
//				}
				super.populateCell(columnHeader, value, columnIndex, dataRow, row);
				columnIndex = columnIndex + columnHeader.getColspan();
			}
			rowNum++;
		}
		
		
		// After we populate all the rows & columns, we go back and see if we need to increase the row height
		// for any rows. This only happens if text-wrap is set to yes (else, we have one line and the default
		// height should be sufficient. If we set the height per row as we add the row, all following rows will have
		// the same height, which may not be appropriate for the given report.
		List<Integer> columnsToCheck = new ArrayList<Integer>();
		for ( Integer idx = 0; idx < report.getHeaderRow().length; idx++ ) {
			if ( report.getHeaderRow()[idx].getFormatter().formatter() instanceof StringWrapFormatter ) {
				columnsToCheck.add(idx);
			}
		}
		logger.log(Level.DEBUG, "Columns to check: " + StringUtils.join(columnsToCheck, ","));
		if ( columnsToCheck.size() > 0 ) {
			for ( int rowIdx=startingRow; rowIdx < sheet.getLastRowNum()+1; rowIdx++ ) {
				XSSFRow reportRow = sheet.getRow(rowIdx);
				short currentHeight = reportRow.getHeight();
				short requiredHeight = -1;
				for ( Integer columnIndex : columnsToCheck ) {					
					XSSFCell cell = reportRow.getCell(columnIndex);
					String text = StringUtils.strip(StringUtils.trim(StringUtils.strip(cell.getStringCellValue())));
					Integer columnWidth = sheet.getColumnWidth(columnIndex);
					XSSFFont font = cell.getCellStyle().getFont();
					requiredHeight = XLSReportFormatter.calculateRequiredRowHeight(columnWidth, font, text);
//					logger.log(Level.DEBUG, "Checking: " + rowIdx+","+columnIndex + "\t" + currentHeight + "\t" + requiredHeight + "\t" + cell.getStringCellValue());
					if ( requiredHeight > currentHeight ) {
						reportRow.setHeight(requiredHeight);
						currentHeight = requiredHeight;
					}
				}
//				logger.log(Level.DEBUG, "Row: " + rowIdx + "\t" + firstHeight + "\t" + currentHeight + "\t" + requiredHeight);
			}
		}
		
		
		
//		for ( int i = 0; i < report.getHeaderRow().length+4; i++ ) { // removed for performance issues 13 mins/column in CRR Detail
//			sheet.autoSizeColumn(i);
//		}
	}




	

	private void makeRecap(XSSFSheet sheet) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		XSSFWorkbook workbook = sheet.getWorkbook();
		StandardReport report = (StandardReport)this.report;
		int rowNum = sheet.getLastRowNum();
		for ( List<CustomCell> dataRow : report.getAddendum() ) {
			XSSFRow row = XLSReportBuilderUtils.makeRow(sheet, rowNum);
			int columnIndex = 0;
			for ( CustomCell dataCell : dataRow ) {
				XSSFCell cell = row.createCell(columnIndex);
				setCellValue(cell, dataCell.getValue());
				cell.setCellStyle(dataCell.makeXlsStyle(workbook));
				columnIndex++;
			}
			rowNum++;
		}
		
	}

	/**
	 * Add a report as a new tab to an existing spreadsheet
	 * 
	 * @param report
	 * @param workbook
	 * @return
	 * @throws Exception
	 */
	public static XSSFSheet build(StandardReport report, XSSFWorkbook workbook) throws Exception {	
		XLSBuilder builder = new XLSBuilder(report);
		builder.makeFormatters(workbook);
		XSSFSheet sheet = workbook.createSheet();		
		
		builder.buildReport(sheet);
		return sheet;
	}
	
	/**
	 * Create a new spreadsheet with one tab filled with the input report
	 * 
	 * @param report
	 * @return
	 * @throws Exception
	 */
	public static XSSFWorkbook build(StandardReport report) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		build(report, workbook);
		return workbook;
	}

	/**
	 * Add a report as a block of cells to an existing spreadsheet tab
	 * 
	 * @param report
	 * @param sheet
	 * @param reportStartLoc
	 * @throws Exception
	 */
	public static void build(StandardReport report, XSSFSheet sheet, ReportStartLoc reportStartLoc) throws Exception {
		XLSBuilder builder = new XLSBuilder(report);
		builder.setReportStartLoc(reportStartLoc);
		builder.makeFormatters(sheet.getWorkbook());
		builder.buildReport(sheet);
	}

}
