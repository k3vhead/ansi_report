package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;

import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ReportBuilderUtils;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderCol;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.TabSettings;
import com.itextpdf.text.TabStop;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFReportBuilderUtils extends ReportBuilderUtils {

	private static final long serialVersionUID = 1L;
	
	private static final Float standardHeaderCellWidth = 252F;
	private static final Logger logger = LogManager.getLogger(PDFReportBuilderUtils.class);
	
	/**
	 * Builds standard header for standard reports. (Can't get much more generic than that)
	 * @param report Any ANSI Standard report object
	 * @param reportStartLoc Where in the sheet to put the report
	 * @param sheet The sheet we're working with
	 * @throws Exception Something bad happened
	 */
	public static Paragraph makeStandardHeader(StandardReport report, ReportStartLoc reportStartLoc, Document document, PdfWriter pdfWriter) throws Exception {
		Paragraph header = new Paragraph();
		
		PdfPTable headerTable = new PdfPTable(3);
		headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerTable.setTotalWidth(new float[] {standardHeaderCellWidth, standardHeaderCellWidth, standardHeaderCellWidth});
		headerTable.setLockedWidth(true);

		
		PdfPCell headerLeftCell = new AnsiPCell();
		PdfPCell headerCenterCell = new AnsiPCell();		
		PdfPCell headerRightCell = new AnsiPCell();

		Phrase headerLeftContent = makeHeaderLeftContent(report, report.getHeaderLeft());		
		headerLeftCell.setPhrase(headerLeftContent);
		Phrase headerCenterContent = makeHeaderCenter(report);
		headerCenterCell.setPhrase(headerCenterContent);
		Phrase headerRightContent = makeHeaderRightContent(report, report.getHeaderRight());
		headerRightCell.setPhrase(headerRightContent);
		
		headerTable.addCell(headerLeftCell);
		headerTable.addCell(headerCenterCell);
		headerTable.addCell(headerRightCell);
		headerTable.setSpacingAfter(4.0F);
//		document.add(headerTable);
		header.add( headerTable );

		if ( ! StringUtils.isBlank(report.getHeaderNotes()) ) {
			Phrase headerNotes = new Paragraph(new Chunk(report.getHeaderNotes(), PDFReportFormatter.fontReportNote));
			Paragraph headerNoteParagraph = new Paragraph(headerNotes);
			headerNoteParagraph.setSpacingAfter(4.0F);
//			document.add( headerNoteParagraph );	
			header.add( headerNoteParagraph );
		}

		return header;
		
//		
//		int numberOfHeaderRows = Math.max(3, startingRow + headerRowCount); // banner + title + subtitle is the minimum
//		numberOfHeaderRows++;  // need to include headers + column labels
//		sheet.setRepeatingRows(new CellRangeAddress(0,numberOfHeaderRows, 0, report.getHeaderRow().length));
//	    
//		Footer footer = sheet.getFooter();
//		footer.setCenter("Page &P of &N");
	}
	

	private static Phrase makeHeaderLeftContent(StandardReport report, List<ReportHeaderCol> headerLeft) throws Exception {
		HeaderContent headerContent = makeHeaderContent(report, headerLeft);
		TabStop tabStop = new TabStop(headerContent.maxLabelSize + 4F, TabStop.Alignment.LEFT);
		List<TabStop> tabStops = new ArrayList<TabStop>();
		tabStops.add(tabStop);
		Phrase headerLeftContent = makeHeaderDisplay(headerContent.headerDisplayList, tabStops, HeaderPosition.LEFT);
		return headerLeftContent;
	}

	private static Phrase makeHeaderCenter(StandardReport report) {
		TabStop centerTab = new TabStop(standardHeaderCellWidth/2.0F, TabStop.Alignment.CENTER);
		TabSettings tabSettings = new TabSettings(Arrays.asList(new TabStop[] { centerTab } ));
		Phrase centerContent = new Phrase();
		centerContent.setTabSettings(tabSettings);
		
		centerContent.setTabSettings(tabSettings);
		centerContent.add(Chunk.TABBING);
		centerContent.add(new Chunk(report.getBanner(), PDFReportFormatter.fontReportBanner));
		centerContent.add(Chunk.NEWLINE);
		centerContent.add(new Chunk(report.getTitle(), PDFReportFormatter.fontReportTitle));
		centerContent.add(Chunk.NEWLINE);
		if ( ! StringUtils.isBlank(report.getSubtitle() )) {
			centerContent.add(new Chunk(report.getSubtitle(), PDFReportFormatter.fontReportSubTitle));
			centerContent.add(Chunk.NEWLINE);
		}
		return centerContent;
	}


	private static Phrase makeHeaderRightContent(StandardReport report, List<ReportHeaderCol> headerRight) throws Exception {
		HeaderContent headerContent = makeHeaderContent(report, headerRight);
		Float tabPosition = standardHeaderCellWidth - headerContent.maxDataSize - 30.0F;
		TabStop tabStop = new TabStop(tabPosition, TabStop.Alignment.RIGHT);
		TabStop tabStop2 = new TabStop(tabPosition+8.0F, TabStop.Alignment.LEFT);
		List<TabStop> tabStops = new ArrayList<TabStop>();
		tabStops.add(tabStop);
		tabStops.add(tabStop2);
		
		HeaderDisplay[][] headerRows = headerContent.headerDisplayList;
		HeaderPosition position = HeaderPosition.RIGHT;
		
		TabSettings tabSettings = new TabSettings(tabStops);
		Phrase cellContent = new Phrase();
		cellContent.setTabSettings(tabSettings);
		for ( int rowIdx=0; rowIdx < 50; rowIdx++ ) {
			for ( int colIdx = 0; colIdx < 50; colIdx++ ) {
				if ( headerRows[rowIdx][colIdx] != null ) {					
					if ( position.equals(HeaderPosition.RIGHT)) {
						cellContent.add( Chunk.TABBING);
					}
					cellContent.add( headerRows[rowIdx][colIdx].label);
					cellContent.add( Chunk.TABBING);
					cellContent.add( headerRows[rowIdx][colIdx].display);
					cellContent.add(Chunk.NEWLINE );
				}
			}
		}
		
		return cellContent;
	}
	


	private static HeaderContent makeHeaderContent(StandardReport report, List<ReportHeaderCol> headerColumns) throws Exception {
		HeaderDisplay[][] headerRows = new HeaderDisplay[50][50];  // if we have more than 50, we're in big trouble
		for ( int rowIdx=0; rowIdx<50; rowIdx++ ) {
			for ( int colIdx=0; colIdx<50; colIdx++ ) {
				headerRows[rowIdx][colIdx]=null;
			}
		}
		
		float maxLabelSize = -1F;
		float maxDisplaySize = -1F;
		
		List<ReportHeaderCol> header = headerColumns == null ? new ArrayList<ReportHeaderCol>() : headerColumns;		
		
		for ( int colIdx = 0; colIdx < header.size(); colIdx++ ) {
			ReportHeaderCol col = header.get(colIdx);
			List<ReportHeaderRow> rowList = col.getRowList() == null ? new ArrayList<ReportHeaderRow>() : col.getRowList();
			for ( int rowIdx = 0; rowIdx < rowList.size(); rowIdx++ ) {
				ReportHeaderRow row = rowList.get(rowIdx);				
				Object value = row.getValue().invoke(report, (Object[])null); 
				headerRows[rowIdx][colIdx] = new HeaderDisplay(row.getLabel(), formatValue(row.getFormatter(), value));	
				if ( headerRows[rowIdx][colIdx].labelSize > maxLabelSize ) {
					maxLabelSize = headerRows[rowIdx][colIdx].labelSize;
				}
				if ( headerRows[rowIdx][colIdx].displaySize > maxDisplaySize ) {
					maxDisplaySize = headerRows[rowIdx][colIdx].displaySize;
				}
			}
		}

		return new HeaderContent(maxLabelSize, maxDisplaySize, headerRows);
	}
	
	
	private static Phrase makeHeaderDisplay(HeaderDisplay[][] headerRows, List<TabStop> tabStops, HeaderPosition position) {
		TabSettings tabSettings = new TabSettings(tabStops);
		Phrase cellContent = new Phrase();
		//cellContent.setTabSettings(tabSettings);
		for ( int rowIdx=0; rowIdx < 50; rowIdx++ ) {
			for ( int colIdx = 0; colIdx < 50; colIdx++ ) {
				if ( headerRows[rowIdx][colIdx] != null ) {					
					Phrase display = new Phrase();
					display.setTabSettings(tabSettings);
					if ( position.equals(HeaderPosition.RIGHT)) {
						display.add( Chunk.TABBING);
					}
					display.add( headerRows[rowIdx][colIdx].label);
					display.add( Chunk.TABBING);
					display.add( headerRows[rowIdx][colIdx].display);
					display.add(Chunk.NEWLINE );
					cellContent.add(display);
				}
			}
		}
		return cellContent;		
	}


	


	/**
	 * 
	 * @param report
	 * @param reportStartLoc
	 * @param rowIndex
	 * @param headerLeft
	 * @param text
	 * @param headerRight
	 * @param document
	 * @throws Exception
	 */
	public static void makeHeaderRow(AbstractReport report, ReportStartLoc reportStartLoc, Float rowIndex, List<ReportHeaderCol> headerLeft, String text, List<ReportHeaderCol> headerRight, Document document) throws Exception {
		Integer headerLeftSize = headerLeft == null ? 0 : headerLeft.size();
		Integer headerRightSize = headerRight == null ? 0 : headerRight.size();
		Integer bannerMergeSize = report.getReportWidth() - (2 * headerLeftSize + 2 * headerRightSize ) - 1;  // nbr of columns - (label&data for left and right)
		if ( bannerMergeSize < 0 ) {
			bannerMergeSize = 2;
		}
		
		List<TabStop> tabStopList = new ArrayList<TabStop>();
		tabStopList.add(new TabStop(450F, TabStop.Alignment.CENTER));
		
		Paragraph reportHeaderCenter = new Paragraph();
		reportHeaderCenter.add(new Chunk(text));
		document.add(reportHeaderCenter);
		/*
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
	    */
	}
	
	
	/**
	 * Creates left/right side data for banner rows in an Excel-formatted report
	 * @param report Any ANSI report
	 * @param headerCol The label/data definition
	 * @param row	Which row in the banner are we working with
	 * @param colIndex Which column in the banner are we working with
	 * @param labelStyle How to format the label
	 * @param dataStyle How to format the data
	 * @throws Exception Something bad happened
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
	 * Add column headers to a standard report
	 * @param report
	 * @param dataTable
	 */
	public static void makeColumnHeader(StandardReport report, PdfPTable dataTable) {
		
		
		
		for ( ColumnHeader columnHeader : report.getHeaderRow() ) {
			Phrase columnHeaderContent = new Phrase( new Chunk(columnHeader.getLabel(), PDFReportFormatter.fontStandardWhiteBold));
			PdfPCell headerCell = new AnsiPCell();			
			headerCell.setPhrase(columnHeaderContent);
			headerCell.setBackgroundColor(BaseColor.BLACK);
			dataTable.addCell(headerCell);
		}
	}
	
	
	
	
	
	public static float[] makeColumnWidths(StandardReport report) {
		float[] totalWidth = null;
	
		if ( report.getColumnWidths() != null ) {
			float[] columnWidths = new float[report.getColumnWidths().length]; // working area for column widths
	
			float definedWidth = 0.0F;
			float definedColumnCount = 0.0F;
			for ( int i = 0; i < report.getColumnWidths().length; i++ ) {
				if ( report.getColumnWidths()[i] != null && report.getColumnWidths()[i].pdfWidth() != null ) {
					columnWidths[i] = report.getColumnWidths()[i].pdfWidth();
					definedWidth = definedWidth + report.getColumnWidths()[i].pdfWidth();
					definedColumnCount = definedColumnCount + 1;
				}
			}
			float defaultWidth = (PDFReportFormatter.tableTotalWidth - definedWidth)/(columnWidths.length - definedColumnCount);
			logger.log(Level.DEBUG, "Default Column Width: " + defaultWidth + " ("+report.getTitle() + ")");
			for ( int i = 0; i < columnWidths.length; i++ ) {
				if ( columnWidths[i] == 0.0F ) {
					columnWidths[i] = defaultWidth;
				}
			}
	
			ColumnHeader[] headers = report.getHeaderRow();
			totalWidth = new float[headers.length];
			int idxC = 0;  // index into columnWidth
			for ( int idxH = 0; idxH < headers.length; idxH ++ ) {
				float width = 0.0F;
				for ( int cCount=0; cCount < headers[idxH].getColspan(); cCount++ ) {
					width = width + columnWidths[idxC];
					idxC++;
				}
				totalWidth[idxH] = width;
			}
		}
		
		if ( totalWidth == null ) {
			logger.log(Level.DEBUG, "No COlumn widths defined");
		}
		
		
		return totalWidth;
	}





	public enum HeaderPosition {
		LEFT,
		CENTER,
		RIGHT,
		;
		
		
	}
}
