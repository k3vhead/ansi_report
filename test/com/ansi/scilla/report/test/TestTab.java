package com.ansi.scilla.report.test;

import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class TestTab {

	public void go() throws Exception {
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/tabTest.pdf"));
		document.open();
		
		PdfPTable headerTable = new PdfPTable(3);
		headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerTable.setTotalWidth(new float[] {252F, 252F, 252F});
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
		headerRightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		headerRightCell.setBorder(Rectangle.NO_BORDER);
//		headerRight.setBorderColor(BaseColor.BLACK);
//		headerRight.setBorderWidth(1F);
		headerRightCell.setIndent(0F);
		headerRightCell.setPaddingTop(0F);
		headerRightCell.setPaddingBottom(0F);
		
		
		
		document.close();
	}
	
	public static void main(String[] args) {
		try {
			new TestTab().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
