package com.ansi.scilla.report.test.itext5;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFBuilder;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class TestWriter {

	private void go() throws Exception {
		ByteArrayOutputStream baos = makeDocument();
		baos.writeTo(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/write_test.pdf"));
	}
	
	private ByteArrayOutputStream makeDocument() throws DocumentException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document(PageSize.LETTER.rotate(), PDFBuilder.marginLeft, PDFBuilder.marginRight, PDFBuilder.marginTop, PDFBuilder.marginBottom);
		PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
		document.open();
		document.add(new Paragraph("Adding stuff to a PDF just to fill some space"));
		document.newPage();
		document.add(new Paragraph("lorem ipsum or some such stuff"));
		PdfPTable table = new PdfPTable(new float[] {.33F, .33F, .33F});
		table.setWidthPercentage(100F);
		table.addCell(new PdfPCell(new Phrase("Column 1")));
		table.addCell(new PdfPCell(new Phrase("Column 2")));
		table.addCell(new PdfPCell(new Phrase("Column 3")));
		document.add(table);
		document.close();	

		return baos;
	}

	public static void main(String[] args) {
		try {
			new TestWriter().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
