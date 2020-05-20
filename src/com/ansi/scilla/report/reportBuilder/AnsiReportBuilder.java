package com.ansi.scilla.report.reportBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.pac.PacReport;
import com.ansi.scilla.report.reportBuilder.htmlBuilder.HTMLBuilder;
import com.ansi.scilla.report.reportBuilder.htmlBuilder.HTMLSummaryBuilder;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFBuilder;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFCompoundReportBuilder;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFSummaryBuilder;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.CompoundReport;
import com.ansi.scilla.report.reportBuilder.reportType.CustomReport;
import com.ansi.scilla.report.reportBuilder.reportType.DataDumpReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSSummaryBuilder;

/**
 * A Utility class to build and save any of the myriad of reports that can be generated. Provides
 * a one-line code to figure out what kind of report we have, the method to create XLS/HTML/PDF and
 * to save that formatted report in an arbitrary file location. Shear magic.
 * 
 * @author dclewis
 *
 */
public class AnsiReportBuilder extends ApplicationObject {

	private static final long serialVersionUID = 1L;
	
	public static ByteArrayOutputStream buildPDF(StandardReport report) throws Exception {
		return PDFBuilder.build(report);
	}
	
	public static ByteArrayOutputStream buildPDF(StandardSummaryReport report) throws Exception {
		return PDFSummaryBuilder.build(report);
	}
	
	public static ByteArrayOutputStream buildPDF(DataDumpReport report) throws Exception {
		return report.makePDF();
	}
	
	public static ByteArrayOutputStream buildPDF(CompoundReport report) throws Exception {
		return PDFCompoundReportBuilder.build(report);
	}
	
	public static ByteArrayOutputStream buildPDF(CustomReport report) throws Exception {
		return report.makePDF();
	}
	
	public static ByteArrayOutputStream buildPDF(AbstractReport report) throws Exception {
		ByteArrayOutputStream baos = null;
		if ( report instanceof StandardReport ) {
			baos = buildPDF((StandardReport)report);
		} else if ( report instanceof StandardSummaryReport ) {
			baos = buildPDF((StandardSummaryReport)report);
		} else if ( report instanceof CustomReport ) {
			baos = buildPDF((CustomReport)report);
		} else {
			throw new Exception("Unknown extension of AbstractReport: " + report.getClass().getName());
		}
		return baos;
	}
	
	public static void writePDF(StandardReport report, String filePath) throws Exception {
		buildPDF(report).writeTo(new FileOutputStream(filePath));
	}
	
	public static void writePDF(StandardSummaryReport report, String filePath) throws Exception {
		buildPDF(report).writeTo(new FileOutputStream(filePath));
	}
	
	public static void writePDF(DataDumpReport report, String filePath) throws Exception {
		buildPDF(report).writeTo(new FileOutputStream(filePath));
	}
	
	public static void writePDF(CompoundReport report, String filePath) throws Exception {
		buildPDF(report).writeTo(new FileOutputStream(filePath));
	}
	
	public static void writePDF(CustomReport report, String filePath) throws Exception {
		buildPDF(report).writeTo(new FileOutputStream(filePath));
	}
	
	public static void writePDF(AbstractReport report, String filePath) throws Exception {
		if ( report instanceof StandardReport ) {
			writePDF((StandardReport)report, filePath);
		} else if ( report instanceof StandardSummaryReport ) {
			writePDF((StandardSummaryReport)report, filePath);
		} else if ( report instanceof CustomReport ) {
			writePDF((CustomReport)report, filePath);
		} else {
			throw new Exception("Unknown extension of AbstractReport: " + report.getClass().getName());
		}
	}

	public static XSSFWorkbook buildXLS(StandardReport report) throws Exception {
		return XLSBuilder.build(report);
	}

	public static XSSFWorkbook buildXLS(StandardSummaryReport report) throws Exception {
		return XLSSummaryBuilder.build(report);
	}
	
	public static XSSFWorkbook buildXLS(DataDumpReport report) throws Exception {
		return report.makeXLS();
	}
	
	public static XSSFWorkbook buildXLS(CompoundReport report) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		for ( AbstractReport subReport : report.getReports() ) {
			if ( subReport instanceof StandardReport ) {
				XLSBuilder.build((StandardReport)subReport, workbook);
			} else if ( subReport instanceof StandardSummaryReport ) {
				XLSSummaryBuilder.build((StandardSummaryReport)subReport, workbook);
				workbook = buildXLS((StandardSummaryReport)subReport);
			} else if ( subReport instanceof CustomReport ) {
				((CustomReport)subReport).add2XLS(workbook);
			} else {
				throw new Exception("Unknown extension of AbstractReport: " + subReport.getClass().getName());
			}
		}
		return workbook;
	}
	
	public static XSSFWorkbook buildXLS(CustomReport report) throws Exception {
		return report.makeXLS();
	}
	
	public static XSSFWorkbook buildXLS(AbstractReport report) throws Exception {
		XSSFWorkbook workbook = null;
		if ( report instanceof StandardReport ) {
			workbook = buildXLS((StandardReport)report);
		} else if ( report instanceof StandardSummaryReport ) {
			workbook = buildXLS((StandardSummaryReport)report);
		} else if ( report instanceof CustomReport ) {
			workbook = buildXLS((CustomReport)report);
		} else {
			throw new Exception("Unknown extension of AbstractReport: " + report.getClass().getName());
		}
		return workbook;
	}
	
	public static void writeXLS(StandardReport report, String filePath) throws Exception {
		buildXLS(report).write(new FileOutputStream(filePath));
	}

	public static void writeXLS(StandardSummaryReport report, String filePath) throws Exception {
		buildXLS(report).write(new FileOutputStream(filePath));
	}
	
	public static void writeXLS(DataDumpReport report, String filePath) throws Exception {
		buildXLS(report).write(new FileOutputStream(filePath));
	}
	
	public static void writeXLS(CompoundReport report, String filePath) throws Exception {
		buildXLS(report).write(new FileOutputStream(filePath));
	}
	
	public static void writeXLS(CustomReport report, String filePath) throws Exception {
		buildXLS(report).write(new FileOutputStream(filePath));
	}
	
	public static void writeXLS(AbstractReport report, String filePath) throws Exception {
		buildXLS(report).write(new FileOutputStream(filePath));
	}
	
	
	
	
	public static String buildHtml(StandardReport report) throws Exception {
		return HTMLBuilder.build(report);
	}
	
	public static String buildHtml(StandardSummaryReport report) throws Exception {
		return HTMLSummaryBuilder.build(report);
	}

	public static String buildHtml(DataDumpReport report) throws Exception {
		return report.makeHTML();
	}

	public static String buildHtml(CustomReport report) throws Exception {
		return report.makeHTML();
	}

	public static String buildHTML(AbstractReport report) throws Exception {
		String html = null;
		if ( report instanceof StandardReport ) {
			html = buildHTML((StandardReport)report);
		} else if ( report instanceof StandardSummaryReport ) {
			html = buildHTML((StandardSummaryReport)report);
		} else if ( report instanceof CustomReport ) {
			html = buildHTML((CustomReport)report);
		} else {
			throw new Exception("Unknown extension of AbstractReport: " + report.getClass().getName());
		}
		return html;
	}
	
	public static void writeHTML(StandardReport report, String filePath) throws Exception {
		FileUtils.write(new File(filePath), buildHtml(report));
	}
	
	public static void writeHTML(DataDumpReport report, String filePath) throws Exception {
		FileUtils.write(new File(filePath), buildHtml(report));
	}
	
	public static void writeHTML(CustomReport report, String filePath) throws Exception {
		FileUtils.write(new File(filePath), buildHtml(report));
	}

	public static void writeHTML(AbstractReport report, String filePath) throws Exception {
		FileUtils.write(new File(filePath), buildHTML(report));
	}

	public static void writeHTML(CompoundReport report, String filePath) throws Exception {
		// TODO : AnsiReportBuilder Build HTML Compound Report
		throw new Exception("not written yet");		
	}


}
