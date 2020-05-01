package com.ansi.scilla.report.test.itext5;

import java.io.FileOutputStream;

import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportFormatter;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class TestPdfHeader {

	private void go() throws Exception {
		Document document = new Document(PageSize.LETTER.rotate(), PDFReportFormatter.marginLeft, PDFReportFormatter.marginRight, PDFReportFormatter.marginTop, PDFReportFormatter.marginBottom);
		PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/header_test.pdf"));
		pdfWriter.setPageEvent(new HeaderFooterPageEvent());
		document.open();
		document.add(new Paragraph("Adding stuff to a PDF just to fill some space"));
		document.newPage();
		document.add(new Paragraph("lorem ipsum or some such stuff"));
		document.close();	
		
	}
	
	public static void main(String[] args) {
		try {
			new TestPdfHeader().go();
			System.out.println("DOne");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
