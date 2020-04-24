package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.util.Calendar;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.report.reportBuilder.DataDumpReport;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class DataDumpPDFReportHeader extends AbstractPDFReportHeader {

	public DataDumpPDFReportHeader(DataDumpReport report) {
		super();
		try {
			makeHeaderTable(report);
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
	}

	private void makeHeaderTable(DataDumpReport report) throws DocumentException {		
		this.headerTable = new PdfPTable(3);
		this.headerTable.setTotalWidth(new float[] {189F, 378F, 189F});
		headerTable.setLockedWidth(true);
		Calendar today = Calendar.getInstance();
		
		PdfPCell headerLeftCell = new PdfPCell();
        headerLeftCell.setBorder(Rectangle.NO_BORDER);
//		headerLeft.setBorderColor(BaseColor.BLACK);
//		headerLeft.setBorderWidth(1F);
		headerLeftCell.setIndent(0F);
		headerLeftCell.setPaddingTop(0F);
		headerLeftCell.setPaddingBottom(0F);
		headerLeftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		headerLeftCell.setVerticalAlignment(Element.ALIGN_TOP);
		String createdDate = PDFReportFormatter.dataFormatDateTime.format(today.getTime());
		Phrase leftContent = new Phrase();
		leftContent.add(new Chunk("Created:", PDFReportFormatter.fontStandardBlackBold));
		leftContent.add(new Chunk(" ", PDFReportFormatter.fontStandardBlack));
		leftContent.add(new Chunk( createdDate, PDFReportFormatter.fontStandardBlack));
		headerLeftCell.setPhrase(leftContent);	
		headerTable.addCell(headerLeftCell);
		
		headerTable.addCell(new PdfPCell(new Phrase("Middle")));
		headerTable.addCell(new PdfPCell(new Phrase("Right")));
		/*
		cell = row.createCell(1);
		cell.setCellValue(createdDate);
		cell.setCellStyle(reportFormatter.cellStyleStandardLeft);
		
		cell = row.createCell(2);
		sheet.addMergedRegion(new CellRangeAddress(
	            rowNum, //first row (0-based)
	            rowNum, //last row  (0-based)
	            2, //first column (0-based)
	            this.columnHeaders.size() - 2  //last column  (0-based)
	    ));
		cell.setCellValue(super.getBanner());
		cell.setCellStyle(reportFormatter.cellStyleReportBanner);
		
		rowNum++;
		row = sheet.createRow(rowNum);
		cell = row.createCell(2);
		sheet.addMergedRegion(new CellRangeAddress(
	            rowNum, //first row (0-based)
	            rowNum, //last row  (0-based)
	            2, //first column (0-based)
	            this.columnHeaders.size() - 2  //last column  (0-based)
	    ));
		cell.setCellValue(super.getTitle());
		cell.setCellStyle(reportFormatter.cellStyleReportTitle);
		
		rowNum++;
		row = sheet.createRow(rowNum);
		for ( int columnIndex = 0; columnIndex < this.columnHeaders.size(); columnIndex++ ) {
			cell = row.createCell(columnIndex);
			cell.setCellValue(this.columnHeaders.get(columnIndex));
			cell.setCellStyle(reportFormatter.cellStyleColHdrCenter);
		}
		*/

	}

	@Override
	public void onOpenDocument(PdfWriter writer, Document document) {
		t = writer.getDirectContent().createTemplate(30,16);
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		// add header
		Logger logger = LogManager.getLogger(this.getClass());
		logger.log(Level.DEBUG, "Table height: " + headerTable.getTotalHeight() + "\tTable Width: " + headerTable.getTotalWidth());
		headerTable.writeSelectedRows(0, -1, 34, 612, writer.getDirectContent());
		
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
