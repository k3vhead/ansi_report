package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.report.reportBuilder.common.ReportBuilderUtils;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderCol;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportBuilderUtils.HeaderPosition;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.TabSettings;
import com.itextpdf.text.TabStop;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Page header for pdf reports. Freely stolen from https://memorynotfound.com/adding-header-footer-pdf-using-itext-java
 * @author dclewis
 *
 */
public class PDFReportHeader extends PdfPageEventHelper {
	private static final Float standardHeaderCellWidth = 252F;

	
	protected PdfPTable headerTable;
	protected PdfTemplate t;

	public PDFReportHeader(AbstractReport report) throws Exception {
		super();
		makeHeaderTable(report);
	}
	
	public PdfPTable getHeaderTable() {
		return headerTable;
	}
	public void setHeaderTable(PdfPTable headerTable) {
		this.headerTable = headerTable;
	}
	
	
	
	
	protected void makeHeaderTable(AbstractReport report) throws Exception {
		this.headerTable = new PdfPTable(3);
		
		headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerTable.setTotalWidth(new float[] {standardHeaderCellWidth, standardHeaderCellWidth, standardHeaderCellWidth});
		headerTable.setLockedWidth(true);

		
		PdfPCell headerLeftCell = new PdfPCell();
		headerLeftCell.setVerticalAlignment(Element.ALIGN_TOP);
		headerLeftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerLeftCell.setBorder(Rectangle.NO_BORDER);
//		headerLeft.setBorderColor(BaseColor.BLACK);
//		headerLeft.setBorderWidth(1F);
		headerLeftCell.setIndent(0F);
		headerLeftCell.setPaddingTop(0F);
		headerLeftCell.setPaddingBottom(0F);
		
		PdfPCell headerCenterCell = new PdfPCell();
		headerCenterCell.setVerticalAlignment(Element.ALIGN_TOP);
		headerCenterCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerCenterCell.setBorder(Rectangle.NO_BORDER);
//		headerCenter.setBorderColor(BaseColor.BLACK);
//		headerCenter.setBorderWidth(1F);
		headerCenterCell.setIndent(0F);
		headerCenterCell.setPaddingTop(0F);
		headerCenterCell.setPaddingBottom(0F);
		
		PdfPCell headerRightCell = new PdfPCell();
		headerRightCell.setVerticalAlignment(Element.ALIGN_TOP);
		headerRightCell.setHorizontalAlignment(Element.ALIGN_LEFT);  //Seems like it should be align:right, but this makes the tabbing work
		headerRightCell.setBorder(Rectangle.NO_BORDER);
//		headerRight.setBorderColor(BaseColor.BLACK);
//		headerRight.setBorderWidth(1F);
		headerRightCell.setIndent(0F);
		headerRightCell.setPaddingTop(0F);
		headerRightCell.setPaddingBottom(0F);

		Phrase headerLeftContent = makeHeaderLeftContent(report, report.getHeaderLeft());		
		headerLeftCell.setPhrase(headerLeftContent);
		Phrase headerCenterContent = makeHeaderCenter(report);
		headerCenterCell.setPhrase(headerCenterContent);
		Phrase headerRightContent = makeHeaderRightContent(report, report.getHeaderRight());
		headerRightCell.setPhrase(headerRightContent);
		
		headerTable.addCell(headerLeftCell);
		headerTable.addCell(headerCenterCell);
		headerTable.addCell(headerRightCell);
		
		if ( ! StringUtils.isBlank(report.getHeaderNotes()) ) {
			Phrase headerNotes = new Phrase(new Chunk(report.getHeaderNotes(), PDFReportFormatter.fontReportNote));
			PdfPCell notesCell = new PdfPCell(headerNotes);
			notesCell.setColspan(3);
			notesCell.setVerticalAlignment(Element.ALIGN_TOP);
			notesCell.setHorizontalAlignment(Element.ALIGN_LEFT);
			notesCell.setBorder(Rectangle.NO_BORDER);
			headerTable.addCell(notesCell);
		}	
		
		headerTable.setSpacingAfter(4.0F);		
		
	}

	
	private static Phrase makeHeaderLeftContent(AbstractReport report, List<ReportHeaderCol> headerLeft) throws Exception {
		HeaderContent headerContent = makeHeaderContent(report, headerLeft);
		TabStop tabStop = new TabStop(headerContent.maxLabelSize + 4F, TabStop.Alignment.LEFT);
		List<TabStop> tabStops = new ArrayList<TabStop>();
		tabStops.add(tabStop);
		Phrase headerLeftContent = makeHeaderDisplay(headerContent.headerDisplayList, tabStops, HeaderPosition.LEFT);
		return headerLeftContent;
	}

	private static Phrase makeHeaderCenter(AbstractReport report) {
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


	private static Phrase makeHeaderRightContent(AbstractReport report, List<ReportHeaderCol> headerRight) throws Exception {
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
	
	private static HeaderContent makeHeaderContent(AbstractReport report, List<ReportHeaderCol> headerColumns) throws Exception {
		Logger logger = LogManager.getLogger(PDFReportBuilderUtils.class);
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
				headerRows[rowIdx][colIdx] = new HeaderDisplay(row.getLabel(), ReportBuilderUtils.formatValue(row.getFormatter(), value));	
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
	
	
	protected static Phrase makeHeaderDisplay(HeaderDisplay[][] headerRows, List<TabStop> tabStops, HeaderPosition position) {
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
	
	
	
	@Override
	public void onOpenDocument(PdfWriter writer, Document document) {
		t = writer.getDirectContent().createTemplate(30,16);
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		// add header
		headerTable.writeSelectedRows(0, -1, PDFReportFormatter.headerDefaultPositionX, PDFReportFormatter.headerDefaultPositionY, writer.getDirectContent());
		
		// add footer
		Float center = (document.getPageSize().getRight() - document.getPageSize().getLeft())/2.0F;
//		Float top = document.getPageSize().getHeight() - 50F;
		Float bottom = document.getPageSize().getBottom() + 20F;
		Float noRotation = 0F;
		Phrase footer = new Phrase(new Chunk("Page " + document.getPageNumber() + " of ", PDFReportFormatter.fontStandardBlack));		
		ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, footer, center, bottom, noRotation);
	}

	

	@Override
	public void onCloseDocument(PdfWriter writer, Document document) {
		//TODO : Add "of total page count" to footer
		Logger logger = LogManager.getLogger(this.getClass());
		String lastPageNumber = String.valueOf(writer.getPageNumber());
		logger.log(Level.DEBUG, "Last Page: " + lastPageNumber);
	}
}
