package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;

public abstract class AbstractPDFReportHeader extends PdfPageEventHelper {
	protected PdfPTable headerTable;
	protected PdfTemplate t;

	
	public PdfPTable getHeaderTable() {
		return headerTable;
	}
	public void setHeaderTable(PdfPTable headerTable) {
		this.headerTable = headerTable;
	}
}
