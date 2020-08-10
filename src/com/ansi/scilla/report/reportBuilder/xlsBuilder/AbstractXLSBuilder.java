package com.ansi.scilla.report.reportBuilder.xlsBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.NoPreviousValue;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderCol;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.reportType.PrintableReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;

public abstract class AbstractXLSBuilder extends PrintableReport {

	private static final long serialVersionUID = 1L;
	
	private CreationHelper createHelper;
	protected XLSReportFormatter rf;
	protected HashMap<DataFormats, CellStyle> cellStyles;	
	protected ReportStartLoc reportStartLoc = new ReportStartLoc(0,0);



	protected AbstractXLSBuilder(StandardReport report) {
		super(report);
		makeHeaderDates();
	}

	protected AbstractXLSBuilder(StandardSummaryReport report) {
		super(report);
		makeHeaderDates();
	}
	
	

	protected void makeFormatters(XSSFWorkbook workbook) {
		this.createHelper = workbook.getCreationHelper();
		this.rf = new XLSReportFormatter(workbook);
		this.cellStyles = new HashMap<DataFormats, CellStyle>();
		
		this.cellStyles.put(DataFormats.DATE_FORMAT, rf.cellStyleDateLeft);
		this.cellStyles.put(DataFormats.DATE_TIME_FORMAT, rf.cellStyleDateTimeLeft);
		this.cellStyles.put(DataFormats.DETAIL_TIME_FORMAT, rf.cellStyleDateTimeLeft);
		
		this.cellStyles.put(DataFormats.INTEGER_FORMAT, rf.cellStyleStandardInteger);
		this.cellStyles.put(DataFormats.NUMBER_FORMAT, rf.cellStyleStandardNumber);
		this.cellStyles.put(DataFormats.NUMBER_CENTERED, rf.cellStyleNumberCenter);
		this.cellStyles.put(DataFormats.DECIMAL_FORMAT, rf.cellStyleStandardDecimal);

		this.cellStyles.put(DataFormats.CURRENCY_FORMAT, rf.cellStyleStandardCurrency);

		this.cellStyles.put(DataFormats.STRING_FORMAT, rf.cellStyleStandardLeft);
		this.cellStyles.put(DataFormats.STRING_CENTERED, rf.cellStyleStandardCenter);
		this.cellStyles.put(DataFormats.STRING_TRUNCATE, rf.cellStyleStandardLeft);
		this.cellStyles.put(DataFormats.STRING_ABBREVIATE, rf.cellStyleStandardLeft);
		
		this.cellStyles.put(DataFormats.STRING_WRAP_LEFT, rf.cellStyleTextWrapLeft);
		this.cellStyles.put(DataFormats.STRING_WRAP_CENTERED, rf.cellStyleTextWrapCenter);
		this.cellStyles.put(DataFormats.STRING_WRAP_RIGHT, rf.cellStyleTextWrapRight);
	}
	

	protected void makeBannerData(ReportHeaderCol headerCol, XSSFRow row, Integer colIndex, CellStyle labelStyle, CellStyle dataStyle) throws Exception {
		XSSFCell cell;
		
		cell = row.createCell(colIndex);
		cell.setCellStyle(labelStyle);
		ReportHeaderRow headerRow = headerCol.getRowList().get(row.getRowNum());
		cell.setCellValue(headerRow.getLabel());
		colIndex++;
		cell = row.createCell(colIndex);
		Object value = headerRow.getValue().invoke(this.report, (Object[])null); 
		String display = formatValue(headerRow.getFormatter(), value);	
		cell.setCellValue(display);
		cell.setCellStyle(dataStyle);
		colIndex++;
		
	}
	
	

	
	
	
	protected void makeHeaderRow(Integer rowIndex, List<ReportHeaderCol> headerLeft, String text, CellStyle bannerStyle, List<ReportHeaderCol> headerRight, XSSFSheet sheet) throws Exception {
		StandardReport report = (StandardReport)this.report;
		Integer reportColCount = report.getHeaderRow().length;
		Integer bannerMergeSize = reportColCount - (2 * headerLeft.size() + 2 * headerRight.size() ) + 1;  // nbr of columns - (label&data for left and right)
		if ( bannerMergeSize < 0 ) {
			bannerMergeSize = 2;
		}
		
		XSSFRow row = null;
		XSSFCell cell = null;
		
		row = sheet.createRow(rowIndex);
		int colIndex = this.reportStartLoc.columnIndex;
		
		for ( ReportHeaderCol headerCol : headerLeft ) {
			if ( headerCol != null && ! headerCol.getRowList().isEmpty() && headerCol.getRowList().size() > rowIndex ) {
				makeBannerData(headerCol, row, colIndex, rf.cellStyleReportHeaderLabelLeft, rf.cellStyleStandardLeft);
			}
			colIndex = colIndex + 2;
		}
		
		cell = row.createCell(colIndex);
		cell.setCellValue(text);
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex+bannerMergeSize));
	    cell.setCellStyle(bannerStyle);
		colIndex = colIndex+bannerMergeSize+1;
	    
	    
		for ( ReportHeaderCol headerCol : headerRight ) {
			if ( headerCol != null && ! headerCol.getRowList().isEmpty() && headerCol.getRowList().size() > rowIndex ) {
				makeBannerData(headerCol, row, colIndex, rf.cellStyleReportHeaderLabelRight, rf.cellStyleStandardRight);
			}
			colIndex = colIndex + 2;
		}

	}
	
	public ReportStartLoc getReportStartLoc() {
		return reportStartLoc;
	}

	public void setReportStartLoc(ReportStartLoc reportStartLoc) {
		this.reportStartLoc = reportStartLoc;
	}

	/**
	 * The POI setCellValue methods only accept a limited number of value types. So we translate what
	 * we have to what we need.
	 * @param cell The Excel cell we're going to populate
	 * @param value The data to be put in the cell
	 * @throws NoSuchMethodException Indicates we have encountered a value type for which we do not have a translator
	 * @throws SecurityException Java reflection error - this shouldn't happen
	 * @throws IllegalAccessException Java reflection error - this shouldn't happen
	 * @throws IllegalArgumentException XSSFCell.set value didn't match expectations 
	 * @throws InvocationTargetException Java reflection error - this shouldn't happen
	 */
	public static void setCellValue(XSSFCell cell, Object value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if ( value != null ) {
			String dataClass = value.getClass().getSimpleName();
			if ( dataClass.equals("Integer")) {
				Double cellValue = Double.valueOf((Integer)value); 
				cell.setCellValue(cellValue);
			} else if ( dataClass.equals("Timestamp")) {
				Calendar cellValue = Calendar.getInstance(new AnsiTime());
				cellValue.setTime(new Date(((Timestamp)value).getTime()));
				cell.setCellValue(cellValue);		
			} else if ( dataClass.equals("BigDecimal")) {
				BigDecimal cellValue = (BigDecimal)value;						
				cell.setCellValue(cellValue.doubleValue());		
			} else if ( dataClass.equals("Double")) {
				Double cellValue = (Double)value;						
				cell.setCellValue(cellValue.doubleValue());	
			} else if ( dataClass.equals("Date")) {
				Date cellValue = (Date)value;
				if ( value.getClass().getName().equals("java.sql.Date")) {
					// there's no POI method for java.sql.Date, so convert the date to java.util.Date
					cellValue = new Date( ((java.sql.Date)cellValue).getTime());
				}
				cell.setCellValue(cellValue);		
			} else {
				// if you're looking here because you're reading a stack trace and found a "method not found error"
				// you need to add another "else if" in the lines right above
				Method valueSetter = XSSFCell.class.getMethod("setCellValue", new Class<?>[] {value.getClass()});
				valueSetter.invoke(cell, new Object[] {value});
			}
		}
	}

	public void populateCell(ColumnHeader columnHeader, Object value, int columnIndex, Object dataRow, XSSFRow row) throws Exception {
		logger.log(Level.DEBUG, "populating cell. columnIndex: " + columnIndex);
		XSSFCell cell = row.createCell(columnIndex);
		if ( this.cellStyles.containsKey(columnHeader.getFormatter())) {		
			logger.log(Level.DEBUG, "\t" + columnHeader.getFormatter().name());
			CellStyle cellStyle = this.cellStyles.get(columnHeader.getFormatter());
			logger.log(Level.DEBUG, "\t" + cellStyle.getWrapText() + "\t" + row.getHeight());
			cell.setCellStyle(this.cellStyles.get(columnHeader.getFormatter()));			
		} else {
			throw new Exception("Missing cell style for " + columnHeader.getFormatter());
		}
		setCellValue(cell, value);

		columnIndex++;		
	}
	
	
	
	protected int makeSubtotal(StandardReport report, XSSFSheet sheet, Object dataRow, int rowNum) throws Exception {
		List<String> fieldsToDisplay = new ArrayList<String>();
		List<String> fieldsThatChanged = new ArrayList<String>();
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];	
			String fieldName = columnHeader.getFieldName();
			if (this.previousValues.containsKey(columnHeader.getFieldName())) {
				// we need to check for changed values because this field is a trigger for a subtotal
				Object previousValue = this.previousValues.get(fieldName);
				Object newValue = makeDisplayData(columnHeader,dataRow);
				if ( ! previousValue.equals(new NoPreviousValue()) && ! previousValue.equals(newValue)) {
					// we have a value change, so add a subtotal row
					fieldsThatChanged.add(fieldName);
				}
				this.previousValues.put(fieldName,  newValue);
			}			
		}
		
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			String fieldName = columnHeader.getFieldName();
			if ( fieldsThatChanged.contains(columnHeader.getSubTotalTrigger())) {
				fieldsToDisplay.add(fieldName);
			}
		}
		
		if ( ! fieldsToDisplay.isEmpty() ) {
			XSSFRow reportRow = sheet.createRow(rowNum);

			int columnIndex = 0;
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];
				
				if ( columnHeader.getColspan() > 0 ) {
					Integer firstColumn = columnIndex;
					Integer lastColumn = columnIndex + columnHeader.getColspan() - 1;
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, firstColumn, lastColumn));
//					columnIndex = columnIndex + columnHeader.getColspan();
				}
//				if ( i == 1 ) {
//					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
//					columnIndex++;
//				}
//				if ( i == report.getHeaderRow().length - 1 ) {
//					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, report.getHeaderRow().length, report.getHeaderRow().length+1));
//				}
				
				columnIndex = columnIndex + report.getFirstDetailColumn();
				String fieldName = columnHeader.getFieldName();
				if ( fieldsToDisplay.contains(fieldName)) {
					String subtotal = super.makeSubtotalData(columnHeader);
					XSSFCell reportCell = reportRow.createCell(columnIndex);
					reportCell.setCellValue(subtotal);
					reportCell.setCellStyle(rf.cellStyleSubtotalDecimal);
				}
				columnIndex = columnIndex + columnHeader.getColspan();
			}
			rowNum++;  // we added a row, so the location of the next detail row needs to be incremented

		}
		
		return rowNum;
	}

	protected void makeFinalSubtotal(XSSFSheet sheet) throws Exception {
		logger.log(Level.DEBUG, "makeFinalSubTotal");
		boolean addASub = false;
		StandardReport report = (StandardReport)this.report;
		String[] subtotalValues = new String[report.getHeaderRow().length];
		
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];	
			if ( ! StringUtils.isBlank(columnHeader.getSubTotalTrigger())) {
				// we're doing a subtotal for this field				
				addASub = true;
				subtotalValues[i] = super.makeSubtotalData(columnHeader);
			}
		}

		logger.log(Level.DEBUG, addASub);
		if ( addASub ) {
			//subtract 1 because getReportHeight includes this row that we're getting ready to add
			//subtract another 1 because the rownumbers are zero-based
			int rowNum = sheet.getLastRowNum() + 1; //this.reportStartLoc.rowIndex + report.getReportHeight() - 2;  
			XSSFRow reportRow = XLSReportBuilderUtils.makeRow(sheet, rowNum); 

			int columnIndex = 0;
			for ( int i = 0; i < subtotalValues.length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];
				if ( columnHeader.getColspan() > 1 ) {
					Integer firstColumn = columnIndex;
					Integer lastColumn = firstColumn + columnHeader.getColspan() - 1;
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, firstColumn, lastColumn));
				}
//				if ( i == 1 ) {
//					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
//					columnIndex++;
//				}
//				if ( i == report.getHeaderRow().length - 1 ) {
//					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, report.getHeaderRow().length, report.getHeaderRow().length+1));
//				}
				if ( ! StringUtils.isBlank(subtotalValues[i]) ) {
					// we're doing a subtotal for this field	
					logger.log(Level.DEBUG, columnIndex + "\t" + report.getFirstDetailColumn());
					Integer reportIndex = columnIndex + report.getFirstDetailColumn();
					XSSFCell reportCell = reportRow.createCell(reportIndex);
					reportCell.setCellValue(subtotalValues[i]);
					reportCell.setCellStyle(rf.cellStyleSubtotalDecimal);
				}
//				columnIndex++;
				columnIndex = columnIndex + columnHeader.getColspan();
			}
		}
	}
	
	protected void makeSummary(XSSFSheet sheet) throws Exception {
		StandardReport report = (StandardReport)this.report;
		boolean addASummary = false;
		XSSFRow row = null;
		XSSFCell cell = null;

		int rowNum = sheet.getLastRowNum() + 1; //this.getReportStartLoc().rowIndex + report.getReportHeight() - 1;
		row = sheet.createRow(rowNum);

		int columnIndex = this.reportStartLoc.columnIndex;
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			if ( columnHeader.getColspan() > 1 ) {
				Integer firstColumn = columnIndex;
				Integer lastColumn = firstColumn + columnHeader.getColspan() - 1;
				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, firstColumn, lastColumn));
			}
//			if ( i == 1 ) {
//				Integer startMerge = this.reportStartLoc.columnIndex;
//				Integer endMerge = this.reportStartLoc.columnIndex + 1;
//				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startMerge, endMerge));
//				columnIndex++;
//			}
//			if ( i == report.getHeaderRow().length - 1 ) {
//				Integer startMerge = this.reportStartLoc.columnIndex + report.getHeaderRow().length;
//				Integer endMerge = this.reportStartLoc.columnIndex + report.getHeaderRow().length + 1;
//				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startMerge, endMerge));
//			}
			Integer reportIndex = columnIndex + report.getFirstDetailColumn();
			cell = row.createCell(reportIndex);
			if ( !columnHeader.getSummaryType().equals(SummaryType.NONE)) {
				addASummary = true;
				//makeSummaryData(columnHeader);
				cell.setCellValue(makeSummaryData(columnHeader));
				cell.setCellStyle(cellStyles.get(columnHeader.getFormatter()));
			}
			columnIndex = columnIndex + columnHeader.getColspan();
		}
		rowNum++;
	}
}
