package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.io.ByteArrayOutputStream;

import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFSummaryBuilder extends AbstractPDFBuilder {

	private static final long serialVersionUID = 1L;

	protected PDFSummaryBuilder(StandardSummaryReport report) {
		super(report);
	}

	private ByteArrayOutputStream buildReport() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		PDFReportHeader header = new PDFReportHeader(report);
		float topMargin = header.getHeaderTable().getTotalHeight() + (PDFReportFormatter.shortSideSize - PDFReportFormatter.headerDefaultPositionY) + 4.0F;
		Document document = new Document(PageSize.LETTER.rotate(), PDFReportFormatter.marginLeft, PDFReportFormatter.marginRight, topMargin, PDFReportFormatter.marginBottom);
		PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
		document.open();
		pdfWriter.setPageEvent(header);

		//TODO : Footer / page count
		document.add(new Paragraph(new Phrase(new Chunk("stuff goes here"))));
		document.close();

		return baos;	
	}
	
	public static ByteArrayOutputStream build(StandardSummaryReport report) throws Exception {
		return new PDFSummaryBuilder(report).buildReport();
	}

}
