package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.io.ByteArrayOutputStream;

import org.dom4j.DocumentException;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.reportBuilder.AnsiReportBuilder;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.CompoundReport;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

public class PDFCompoundReportBuilder extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	private CompoundReport report;
	
	private PDFCompoundReportBuilder(CompoundReport report) {
		super();
		this.report = report;
	}
	
	public CompoundReport getReport() {
		return report;
	}

	public void setReport(CompoundReport report) {
		this.report = report;
	}

	private ByteArrayOutputStream buildReport() throws DocumentException, Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();		
		Document compoundReport = new Document();
		PdfCopy pdfCopy = new PdfCopy(compoundReport, baos);
		
		compoundReport.open();
		for ( AbstractReport subReport : report.getReports() ) {
			ByteArrayOutputStream subBaos = AnsiReportBuilder.buildPDF(subReport);
			PdfReader reader = new PdfReader(subBaos.toByteArray());
			pdfCopy.addDocument(reader);
		}
		compoundReport.close();
		
		return baos;
	}


	public static ByteArrayOutputStream build(CompoundReport report) throws DocumentException, Exception {
		return new PDFCompoundReportBuilder(report).buildReport();
	}
}
