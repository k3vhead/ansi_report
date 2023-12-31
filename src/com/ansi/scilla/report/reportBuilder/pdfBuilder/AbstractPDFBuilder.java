package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.util.ArrayList;
import java.util.List;

import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.NoPreviousValue;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.reportType.PrintableReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;


public abstract class AbstractPDFBuilder extends PrintableReport {

	private static final long serialVersionUID = 1L;	
	
	
	

	protected ReportStartLoc reportStartLoc = new ReportStartLoc(PDFReportFormatter.marginTop, PDFReportFormatter.marginLeft);

	public AbstractPDFBuilder(StandardReport report) {
		super(report);
		makeHeaderDates();
	}

	public AbstractPDFBuilder(StandardSummaryReport report) {
		super(report);
		makeHeaderDates();
	}

	protected void makeSubtotal(StandardReport report, Object row, PdfPTable dataTable) throws Exception {
		List<String> fieldsToDisplay = new ArrayList<String>();
		List<String> fieldsThatChanged = new ArrayList<String>();
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];	
			String fieldName = columnHeader.getFieldName();
			if (this.previousValues.containsKey(columnHeader.getFieldName())) {
				// we need to check for changed values because this field is a trigger for a subtotal
				Object previousValue = this.previousValues.get(fieldName);
				Object newValue = makeDisplayData(columnHeader,row);
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
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];
				String fieldName = columnHeader.getFieldName();
				if ( fieldsToDisplay.contains(fieldName)) {
					String subtotal = super.makeSubtotalData(columnHeader);
					PdfPCell cell = new AnsiPCell(new Phrase(new Chunk(subtotal, PDFReportFormatter.fontSubtotal)));
					cell.setHorizontalAlignment(PDFReportFormatter.cellStyles.get(columnHeader.getFormatter()));
					dataTable.addCell(cell);					
				} else {
					dataTable.addCell(new AnsiPCell(new Phrase("")));
				}
			}
		}
	}
	
	
	protected void makeSummary(StandardReport report, PdfPTable dataTable) throws Exception {
		boolean addASummary = false;		
		List<PdfPCell> summaryRow = new ArrayList<PdfPCell>();
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			PdfPCell cell = new AnsiPCell();			
			if ( columnHeader.getSummaryType().equals(SummaryType.NONE)) {
				cell.setPhrase(new Phrase(""));
			} else {
				addASummary = true;
				String subtotal = makeSummaryData(columnHeader);
				cell.setPhrase(new Phrase(new Chunk(subtotal, PDFReportFormatter.fontSubtotal)));
				cell.setHorizontalAlignment(PDFReportFormatter.cellStyles.get(columnHeader.getFormatter()));
			}
			summaryRow.add(cell);
		}
		
		if ( addASummary ) {
			for ( PdfPCell cell : summaryRow ) {
				dataTable.addCell(cell);
			}
		}
	}
	
	
	/**
	 * Writes element to an arbitrary location on a page
	 * @param canvas
	 * @param rect
	 * @param p
	 * @param simulate
	 * @return the position of the lower-right corner of the added element
	 * @throws DocumentException
	 */
	protected TableSize drawColumnText(PdfContentByte canvas, Rectangle rect, Element p, boolean simulate) throws DocumentException {
		ColumnText ct = new ColumnText(canvas);
		ct.setSimpleColumn(rect);
		ct.addElement(p);
		ct.go(simulate);
		return new TableSize(ct.getLastX(), ct.getYLine());
	}
	
	
	/*
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
	*/
	

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
	/*
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
			} else {
				// if you're looking here because you're reading a stack trace and found a "method not found error"
				// you need to add another "else if" in the lines right above
				Method valueSetter = XSSFCell.class.getMethod("setCellValue", new Class<?>[] {value.getClass()});
				valueSetter.invoke(cell, new Object[] {value});
			}
		}
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
				}
				
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
				ColumnHeader columnHeader = report.getHeaderRow()[i];
				if ( columnHeader.getColspan() > 1 ) {
					Integer firstColumn = columnIndex;
					Integer lastColumn = firstColumn + columnHeader.getColspan() - 1;
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, firstColumn, lastColumn));
				}

				if ( ! StringUtils.isBlank(subtotalValues[i]) ) {
					// we're doing a subtotal for this field				
					XSSFCell reportCell = reportRow.createCell(columnIndex);
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
			if ( columnHeader.getColspan() > 0 ) {
				Integer firstColumn = columnIndex;
				Integer lastColumn = firstColumn + columnHeader.getColspan() - 1;
				sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, firstColumn, lastColumn));
			}

			cell = row.createCell(columnIndex);
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
	*/
}
