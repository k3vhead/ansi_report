package com.ansi.scilla.report.reportBuilder;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class XLSReportBuilderUtils extends ReportBuilderUtils {

	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Builds standard header for standard reports. (Can't get much more generic than that)
	 * @param report
	 * @param reportStartLoc
	 * @param sheet
	 * @throws Exception
	 */
	public static void makeStandardHeader(StandardReport report, ReportStartLoc reportStartLoc, XSSFSheet sheet) throws Exception {
		int headerRowCount = makeHeaderRowCount(report);
		XLSReportFormatter rf = new XLSReportFormatter(sheet.getWorkbook());
		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
		sheet.getPrintSetup().setLandscape(report.getReportOrientation().equals(ReportOrientation.LANDSCAPE));
		sheet.getPrintSetup().setFitWidth((short)1);

//		SimpleDateFormat dateFormatTime = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss");
//		SimpleDateFormat dateFormatStandard = new SimpleDateFormat("yyyy/mm/dd");
//		dateFormatTime.format(runDate);
//		dateFormatStandard.format(startDate);
//		dateFormatStandard.format(endDate);
		
		XSSFRow row = null;
		XSSFCell cell = null;
		
		int startingRow = reportStartLoc.rowIndex;
		
		makeHeaderRow(report, reportStartLoc, rf, startingRow, report.getHeaderLeft(), report.getBanner(), rf.cellStyleReportBanner, report.getHeaderRight(), sheet);
		if ( headerRowCount > 1 ) {
			makeHeaderRow(report, reportStartLoc, rf, startingRow + 1, report.getHeaderLeft(), report.getTitle(), rf.cellStyleReportTitle, report.getHeaderRight(), sheet);	
		}
		if ( headerRowCount > 2 ) {
			makeHeaderRow(report, reportStartLoc, rf, startingRow + 2, report.getHeaderLeft(), report.getSubtitle(), rf.cellStyleReportSubTitle, report.getHeaderRight(), sheet);
		}
		if ( headerRowCount > 3 ) {
			for ( int i=startingRow + 3;i<headerRowCount;i++) {
				makeHeaderRow(report, reportStartLoc, rf, i, report.getHeaderLeft(), "", rf.cellStyleStandardCenter, report.getHeaderRight(), sheet);
			}
		}
		
		if ( ! StringUtils.isBlank(report.getHeaderNotes())) {		
			Integer headerRowNum = startingRow + headerRowCount;
			row = makeRow(sheet, headerRowNum);
			String reportNote = report.getHeaderNotes();
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
	

	/**
	 * Builds standard header for summary reports
	 * @param report
	 * @param reportStartLoc
	 * @param sheet
	 * @return
	 * @throws Exception
	 */
	public static Integer makeSummaryHeader(StandardSummaryReport report, ReportStartLoc reportStartLoc, XSSFSheet sheet) throws Exception {
//		StandardSummaryReport report = (StandardSummaryReport)this.report; 
		int headerRowCount = makeHeaderRowCount(report);
		XLSReportFormatter rf = new XLSReportFormatter(sheet.getWorkbook());
		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
//		boolean reportIsLandscape = report.getReportOrientation().equals(ReportOrientation.LANDSCAPE);
//		sheet.getPrintSetup().setLandscape(reportIsLandscape);
		sheet.getPrintSetup().setFitWidth((short)1);

//		SimpleDateFormat dateFormatTime = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss");
//		SimpleDateFormat dateFormatStandard = new SimpleDateFormat("yyyy/mm/dd");
//		dateFormatTime.format(runDate);
//		dateFormatStandard.format(startDate);
//		dateFormatStandard.format(endDate);
		
		XSSFRow row = null;
		XSSFCell cell = null;
		
		makeHeaderRow(report, reportStartLoc, rf, 0, report.getHeaderLeft(), report.getBanner(), rf.cellStyleReportBanner, report.getHeaderRight(), sheet);
		if ( headerRowCount > 1 ) {
			makeHeaderRow(report, reportStartLoc, rf, 1, report.getHeaderLeft(), report.getTitle(), rf.cellStyleReportTitle, report.getHeaderRight(), sheet);	
		}
		if ( headerRowCount > 2 ) {
			makeHeaderRow(report, reportStartLoc, rf, 2, report.getHeaderLeft(), report.getSubtitle(), rf.cellStyleReportSubTitle, report.getHeaderRight(), sheet);
		}
		if ( headerRowCount > 3 ) {
			for ( int i=3;i<headerRowCount;i++) {
				makeHeaderRow(report, reportStartLoc, rf, i, report.getHeaderLeft(), "", rf.cellStyleStandardCenter, report.getHeaderRight(), sheet);
			}
		}
		
		if ( ! StringUtils.isBlank(report.getHeaderNotes())) {			
			row = makeRow(sheet, headerRowCount);
//			sheet.addMergedRegion(new CellRangeAddress(headerRowCount, headerRowCount, 0, report.getHeaderRow().length + 1));
			sheet.addMergedRegion(new CellRangeAddress(headerRowCount, headerRowCount, 0, 10));
			cell = row.createCell(0);
			cell.setCellStyle(rf.cellStyleReportNote);
			cell.setCellValue(report.getHeaderNotes());
		}
		
		int numberOfHeaderRows = Math.max(3, headerRowCount); // banner + title + subtitle is the minimum
		numberOfHeaderRows++;  // need to include headers + column labels
		sheet.setRepeatingRows(new CellRangeAddress(0,numberOfHeaderRows, 0, makeColumnCount(report)));
	    
		Footer footer = sheet.getFooter();
		footer.setCenter("Page &P of &N");
		
		return numberOfHeaderRows;
	}

	
	
	
	
	
	
	public static void makeHeaderRow(AbstractReport report, ReportStartLoc reportStartLoc, XLSReportFormatter rf, Integer rowIndex, List<ReportHeaderCol> headerLeft, String text, CellStyle bannerStyle, List<ReportHeaderCol> headerRight, XSSFSheet sheet) throws Exception {
		Integer headerLeftSize = headerLeft == null ? 0 : headerLeft.size();
		Integer headerRightSize = headerRight == null ? 0 : headerRight.size();
		Integer bannerMergeSize = report.getReportWidth() - (2 * headerLeftSize + 2 * headerRightSize ) - 1;  // nbr of columns - (label&data for left and right)
		if ( bannerMergeSize < 0 ) {
			bannerMergeSize = 2;
		}
		
		XSSFRow row = null;
		XSSFCell cell = null;
		
		row = makeRow(sheet, rowIndex);
		int colIndex = reportStartLoc.columnIndex;
		
		if ( headerLeft != null ) { 
			for ( ReportHeaderCol headerCol : headerLeft ) {
				if ( headerCol != null && ! headerCol.getRowList().isEmpty() && headerCol.getRowList().size() > rowIndex ) {
					makeBannerData(report, headerCol, row, colIndex, rf.cellStyleReportHeaderLabelLeft, rf.cellStyleStandardLeft);
				}
				colIndex = colIndex + 2;
			}
		}
		
		cell = row.createCell(colIndex);
		cell.setCellValue(text);
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex+bannerMergeSize));
	    cell.setCellStyle(bannerStyle);
		colIndex = colIndex+bannerMergeSize+1;
	    
	    if ( headerRight != null ) {
			for ( ReportHeaderCol headerCol : headerRight ) {
				if ( headerCol != null && ! headerCol.getRowList().isEmpty() && headerCol.getRowList().size() > rowIndex ) {
					makeBannerData(report, headerCol, row, colIndex, rf.cellStyleReportHeaderLabelRight, rf.cellStyleStandardRight);
				}
				colIndex = colIndex + 2;
			}
	    }
	}
	
	
	/**
	 * Creates left/right side data for banner rows in an Excel-formatted report
	 * @param report Any ANSI report
	 * @param headerCol
	 * @param row
	 * @param colIndex
	 * @param labelStyle
	 * @param dataStyle
	 * @throws Exception
	 */
	public static void makeBannerData(AbstractReport report, ReportHeaderCol headerCol, XSSFRow row, Integer colIndex, CellStyle labelStyle, CellStyle dataStyle) throws Exception {
		XSSFCell cell;
		
		cell = row.createCell(colIndex);
		cell.setCellStyle(labelStyle);
		ReportHeaderRow headerRow = headerCol.getRowList().get(row.getRowNum());
		cell.setCellValue(headerRow.getLabel());
		colIndex++;
		cell = row.createCell(colIndex);
		Object value = headerRow.getValue().invoke(report, (Object[])null); 
		String display = formatValue(headerRow.getFormatter(), value);	
		cell.setCellValue(display);
		cell.setCellStyle(dataStyle);
		colIndex++;		
	}
	
	
	
	
	/**
	 * Returns an existing row, if it exist, or creates a new one, if it does not already exist
	 * @param sheet
	 * @param rowNum
	 * @return
	 */
	public static XSSFRow makeRow(XSSFSheet sheet, Integer rowNum) {		
		return sheet.getRow(rowNum) == null ? sheet.createRow(rowNum) : sheet.getRow(rowNum);
	}
}
