package com.ansi.scilla.report.test.itext5;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportFormatter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class HeaderFooterPageEvent extends PdfPageEventHelper {

	private PdfTemplate t;
//	private Image total;
	
	
	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		addHeader(writer);
		addFooter(document, writer);
	}

	@Override
	public void onOpenDocument(PdfWriter writer, Document document) {
		t = writer.getDirectContent().createTemplate(30,16);
//		try {
//			total = Image.getInstance(t);
//			total.setRole(PdfName.ARTIFACT);
//		} catch ( DocumentException e ) {
//			throw new ExceptionConverter(e);
//		}
	}

	@Override
	public void onCloseDocument(PdfWriter writer, Document document) {
		Logger logger = LogManager.getLogger(HeaderFooterPageEvent.class);
		String lastPageNumber = String.valueOf(writer.getPageNumber());
		logger.log(Level.DEBUG, "Last Page: " + lastPageNumber);
	}
	

	private void addHeader(PdfWriter writer) {
		PdfPTable headerTable = new PdfPTable(3);
		try {
			headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			headerTable.setTotalWidth(new float[] {252F, 252F, 252F});
			headerTable.setLockedWidth(true);
			
			headerTable.addCell(new PdfPCell(new Phrase("This is hdrcol 1")));
			headerTable.addCell(new PdfPCell(new Phrase("This is hdrcol 2")));
			headerTable.addCell(new PdfPCell(new Phrase("This is hdrcol 3")));
			
			headerTable.writeSelectedRows(0, -1, 34, 612, writer.getDirectContent());
		} catch ( DocumentException e ) {
			throw new RuntimeException(e);
		}
	}

	private void addFooter(Document document, PdfWriter writer) {
		Float center = (document.getPageSize().getRight() - document.getPageSize().getLeft())/2.0F;
//		Float top = document.getPageSize().getHeight() - 50F;
		Float bottom = document.getPageSize().getBottom() + 20F;
		Float noRotation = 0F;
		Phrase footer = new Phrase(new Chunk("Page " + document.getPageNumber() + " of ", PDFReportFormatter.fontStandardBlack));		
		ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, footer, center, bottom, noRotation);		
	}

}
