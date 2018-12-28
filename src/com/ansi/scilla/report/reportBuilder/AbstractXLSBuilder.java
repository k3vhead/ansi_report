package com.ansi.scilla.report.reportBuilder;

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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.AnsiTime;

public abstract class AbstractXLSBuilder extends ReportBuilder {

	private static final long serialVersionUID = 1L;
	
	public static final Double marginTopDefault = 0.25D;
	public static final Double marginBottomDefault = 0.5D; // need room for the page number in the footer
	public static final Double marginLeftDefault = 0.25D;
	public static final Double marginRightDefault = 0.25D;

	
	private CreationHelper createHelper;
	protected XLSReportFormatter rf;
	protected HashMap<DataFormats, CellStyle> cellStyles;
	protected Double marginTop = marginTopDefault;
	protected Double marginBottom = marginBottomDefault;
	protected Double marginLeft = marginLeftDefault;
	protected Double marginRight = marginRightDefault;
	protected Date runDate;
	protected Date startDate;
	protected Date endDate;
	protected ReportStartLoc reportStartLoc = new ReportStartLoc(0,0);



	protected AbstractXLSBuilder(StandardReport report) {
		super(report);
		makeHeaderDates();
	}

	protected AbstractXLSBuilder(StandardSummaryReport report) {
		super(report);
		makeHeaderDates();
	}
	
	private void makeHeaderDates() {
		Calendar calendar = Calendar.getInstance(new AnsiTime());
		runDate = calendar.getTime();
		calendar.clear();
		//calendar.set(year, month, 1);
		startDate = calendar.getTime();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getMaximum(Calendar.DAY_OF_MONTH));
		endDate = calendar.getTime();
	}

	protected void makeFormatters(XSSFWorkbook workbook) {
		this.createHelper = workbook.getCreationHelper();
		this.rf = new XLSReportFormatter(workbook);
		this.cellStyles = new HashMap<DataFormats, CellStyle>();
		
		this.cellStyles.put(DataFormats.DATE_FORMAT, rf.cellStyleDateLeft);
		this.cellStyles.put(DataFormats.DATE_TIME_FORMAT, rf.cellStyleDateLeft);
		this.cellStyles.put(DataFormats.DETAIL_TIME_FORMAT, rf.cellStyleDateLeft);
		
		this.cellStyles.put(DataFormats.INTEGER_FORMAT, rf.cellStyleStandardInteger);
		this.cellStyles.put(DataFormats.NUMBER_FORMAT, rf.cellStyleStandardNumber);
		this.cellStyles.put(DataFormats.NUMBER_CENTERED, rf.cellStyleNumberCenter);
		this.cellStyles.put(DataFormats.DECIMAL_FORMAT, rf.cellStyleStandardDecimal);

		this.cellStyles.put(DataFormats.CURRENCY_FORMAT, rf.cellStyleStandardCurrency);

		this.cellStyles.put(DataFormats.STRING_FORMAT, rf.cellStyleStandardLeft);
		this.cellStyles.put(DataFormats.STRING_CENTERED, rf.cellStyleStandardCenter);
		this.cellStyles.put(DataFormats.STRING_TRUNCATE, rf.cellStyleStandardLeft);
		this.cellStyles.put(DataFormats.STRING_ABBREVIATE, rf.cellStyleStandardLeft);
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
	
	

	/**
	 * Make banner data display
	 * @param headerData What type of data to display
	 * @param index where to display it
	 * @return the formatted display
	 * @throws Exception Something bad happened
	 * @deprecated Use makeBannerData() instead
	 */
	@Deprecated
	protected String[] makeHeaderData(List<ReportHeaderCol> headerData, Integer index) throws Exception {
		String[] data = new String[2];
		if ( ! headerData.isEmpty() ) {
			for ( ReportHeaderCol col : headerData ) {
				if ( index < col.getRowList().size() ) {
					ReportHeaderRow row = col.getRowList().get(index);
					
					Object value = row.getValue().invoke(this.report, (Object[])null); 
					String display = formatValue(row.getFormatter(), value);
					
					data[0] = row.getLabel();
					data[1] = display;
					
				}
			}
			
		}
		return data;
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
	
	public Double getMarginTop() {
		return marginTop;
	}

	public void setMarginTop(Double marginTop) {
		this.marginTop = marginTop;
	}

	public Double getMarginBottom() {
		return marginBottom;
	}

	public void setMarginBottom(Double marginBottom) {
		this.marginBottom = marginBottom;
	}

	public Double getMarginLeft() {
		return marginLeft;
	}

	public void setMarginLeft(Double marginLeft) {
		this.marginLeft = marginLeft;
	}

	public Double getMarginRight() {
		return marginRight;
	}

	public void setMarginRight(Double marginRight) {
		this.marginRight = marginRight;
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
				Double cellValue = new Double((Integer)value);
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
			} else {
				// if you're looking here because you're reading a stack trace and found a "method not found error"
				// you need to add another "else if" in the lines right above
				Method valueSetter = XSSFCell.class.getMethod("setCellValue", new Class<?>[] {value.getClass()});
				valueSetter.invoke(cell, new Object[] {value});
			}
		}
	}

	public void populateCell(ColumnHeader columnHeader, Object value, int columnIndex, Object dataRow, XSSFRow row) throws Exception {
		
		XSSFCell cell = row.createCell(columnIndex);

		setCellValue(cell, value);
		if ( this.cellStyles.containsKey(columnHeader.getFormatter())) {					
			cell.setCellStyle(this.cellStyles.get(columnHeader.getFormatter()));
		} else {
			throw new Exception("Missing cell style for " + columnHeader.getFormatter());
		}

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
				
				if ( i == 1 ) {
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
					columnIndex++;
				}
				if ( i == report.getHeaderRow().length - 1 ) {
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, report.getHeaderRow().length, report.getHeaderRow().length+1));
				}
				
				String fieldName = columnHeader.getFieldName();
				if ( fieldsToDisplay.contains(fieldName)) {
					String subtotal = super.makeSubtotalData(columnHeader);
					XSSFCell reportCell = reportRow.createCell(columnIndex);
					reportCell.setCellValue(subtotal);
					reportCell.setCellStyle(rf.cellStyleSubtotalDecimal);
				}
				columnIndex++;
			}
			rowNum++;  // we added a row, so the location of the next detail row needs to be incremented

		}
		
		return rowNum;
	}

	protected void makeFinalSubtotal(XSSFSheet sheet) throws Exception {
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

		if ( addASub ) {
			//subtract 1 because getReportHeight includes this row that we're getting ready to add
			//subtract another 1 because the rownumbers are zero-based
			int rowNum = sheet.getLastRowNum() + 1; //this.reportStartLoc.rowIndex + report.getReportHeight() - 2;  
			XSSFRow reportRow = XLSReportBuilderUtils.makeRow(sheet, rowNum); 

			int columnIndex = 0;
			for ( int i = 0; i < subtotalValues.length; i++ ) {
				if ( i == 1 ) {
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
					columnIndex++;
				}
				if ( i == report.getHeaderRow().length - 1 ) {
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, report.getHeaderRow().length, report.getHeaderRow().length+1));
				}
				if ( ! StringUtils.isBlank(subtotalValues[i]) ) {
					// we're doing a subtotal for this field				
					XSSFCell reportCell = reportRow.createCell(columnIndex);
					reportCell.setCellValue(subtotalValues[i]);
					reportCell.setCellStyle(rf.cellStyleSubtotalDecimal);
				}
				columnIndex++;
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
			if ( i == 1 ) {
				Integer startMerge = this.reportStartLoc.columnIndex;
				Integer endMerge = this.reportStartLoc.columnIndex + 1;
				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startMerge, endMerge));
				columnIndex++;
			}
			if ( i == report.getHeaderRow().length - 1 ) {
				Integer startMerge = this.reportStartLoc.columnIndex + report.getHeaderRow().length;
				Integer endMerge = this.reportStartLoc.columnIndex + report.getHeaderRow().length + 1;
				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startMerge, endMerge));
			}
			cell = row.createCell(columnIndex);
			if ( !columnHeader.getSummaryType().equals(SummaryType.NONE)) {
				addASummary = true;
				//makeSummaryData(columnHeader);
				cell.setCellValue(makeSummaryData(columnHeader));
				cell.setCellStyle(cellStyles.get(columnHeader.getFormatter()));
			}
			columnIndex++;
		}
		rowNum++;
	}
}
