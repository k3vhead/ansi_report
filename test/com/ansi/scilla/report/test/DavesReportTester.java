package com.ansi.scilla.report.test;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterDetailReport;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterSummaryReport;
import com.ansi.scilla.report.datadumps.AccountsReceivableTotalsOver60Detail;
import com.ansi.scilla.report.invoiceRegisterReport.InvoiceRegisterReport;
import com.ansi.scilla.report.pac.PacReport;
import com.ansi.scilla.report.pastDue.PastDueReport2;
import com.ansi.scilla.report.reportBuilder.AnsiReportBuilder;
import com.ansi.scilla.report.sixMonthRollingVolume.SixMonthRollingVolumeReport;
import com.ansi.scilla.report.ticket.DispatchedOutstandingTicketReport;
import com.ansi.scilla.report.ticket.TicketStatusReport;;


public class DavesReportTester {

	private final String testResultDirectory = "/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/";
	
	protected ReportType reportType;
	protected Calendar startDate;
	protected Calendar endDate;
	protected Integer divisionId;
	protected Integer month;
	protected Integer year;
	protected HashMap<String, String> reportDisplay;
	protected Logger logger;

	
	public static void main(String[] args) {
		try {
			new DavesReportTester().makeMyReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void makeMyReport() throws Exception {
		this.logger = LogManager.getLogger("com.ansi.scilla.report.reportBuilder");
		logger.info("Start");

		List<Thread> threadList = new ArrayList<Thread>();
		
		threadList.add(new Thread(new Make6MRV()));					// this is a custom report
		threadList.add(new Thread(new MakeAROver60()));   			// this is a datadump
		threadList.add(new Thread(new MakeClientUsage()));
		threadList.add(new Thread(new MakeCRRDetail()));
		threadList.add(new Thread(new MakeCRRSummary()));   		// this is a standard summary
		threadList.add(new Thread(new MakeDO()));					// this is a standard report with banner notes
		threadList.add(new Thread(new MakeInvoiceRegister()));   	// this is a standard report with totals
		threadList.add(new Thread(new MakePACListing()));			// this is a compound report
		threadList.add(new Thread(new makePastDue2()));	
		threadList.add(new Thread(new MakeTicketStatus()));			// this is a standard report

		for ( Thread thread : threadList ) {
			thread.start();
		}
		
		while ( true ) {
			try {
				for ( Thread thread : threadList ) {
					thread.join();
				}
				break;
			} catch ( InterruptedException e) {
				System.err.println("Interrupted");
			}
		}
		logger.info("Done");		
	}


	
	public abstract class ReportMaker implements Runnable {
		@Override		
		public void run() {
			Connection conn = null;

			try {
				conn = AppUtils.getDevConn();
				conn.setAutoCommit(false);
				makeReport(conn);
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			} finally {
				AppUtils.closeQuiet(conn);
			}
		}
		
		public abstract void makeReport(Connection conn) throws Exception;
		
		protected String makeFileName(String fileName) {
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmss");
			return testResultDirectory + fileName + "_" + sdf.format(today) + ".xlsx";
		}
		
		protected String makePdfName(String fileName) {
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmss");
			return testResultDirectory + fileName + "_" + sdf.format(today) + ".pdf";
		}
		
		protected String makeHtmlName(String fileName) {
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmss");
			return testResultDirectory + fileName + "_" + sdf.format(today) + ".html";
		}

	}
	
	public class Make6MRV extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			Integer divisionId = 101;
			Integer month = Calendar.JULY;
			Integer year = 2019;
			String fileName = "6MRV";
			SixMonthRollingVolumeReport report = SixMonthRollingVolumeReport.buildReport(conn, divisionId, month, year);
			XSSFWorkbook workbook = report.makeXLS();
			workbook.write(new FileOutputStream(makeFileName(fileName)));
//			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));
		}
	}

	public class MakeClientUsage extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			Calendar startDate = new GregorianCalendar(2020, Calendar.FEBRUARY, 1);
			Integer divisionId = 101;
			logger.info("PastDueReport");
			String fileName = "ClientUsage.xlsx";
			PastDueReport2 report = PastDueReport2.buildReport(conn, startDate, divisionId);
//			XSSFWorkbook workbook = XLSBuilder.build(userReport);
//			workbook.write(new FileOutputStream(makeFileName(fileName)));
		
//			String html = HTMLBuilder.build(userReport);
//			FileUtils.write(new File(makeHtmlName(fileName)), html);
			
			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));
		}
	}

	public class MakeAROver60 extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start AROver60");
			String fileName = "AROver60";
			AccountsReceivableTotalsOver60Detail report = AccountsReceivableTotalsOver60Detail.buildReport(conn);
//			XSSFWorkbook workbook = report.makeXLS();
//			workbook.write(new FileOutputStream(makeFileName(fileName)));
			
//			ByteArrayOutputStream baos = report.makePDF();
//			baos.writeTo(new FileOutputStream(makePdfName(fileName)));
			
			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));

			logger.info("End AROver60");			
		}
		
	}
	
	
	public class MakeCRRDetail extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start CRR");
			String fileName = "CRR_DETAIL";
			Calendar startDate = new GregorianCalendar(2019, Calendar.AUGUST, 1);
			Calendar endDate = new GregorianCalendar(2019, Calendar.AUGUST, 31);
			CashReceiptsRegisterDetailReport report = CashReceiptsRegisterDetailReport.buildReport(conn, startDate, endDate);
//			XSSFWorkbook workbook = XLSBuilder.build(crrDetail);
//			workbook.write(new FileOutputStream(makeFileName("CRR_DETAIL")));
			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));
			logger.info("End CRR");			
		}
		
	}
	
	
	public class MakeCRRSummary extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start CRR Summary");
			String fileName = CashReceiptsRegisterSummaryReport.FILENAME;
			Calendar startDate = new GregorianCalendar(2019, Calendar.AUGUST, 1);
			Calendar endDate = new GregorianCalendar(2019, Calendar.AUGUST, 31);
			CashReceiptsRegisterSummaryReport report = CashReceiptsRegisterSummaryReport.buildReport(conn, startDate, endDate);
//			XSSFWorkbook workbook = new XSSFWorkbook();
//			report.makeXLS(workbook);
//			workbook.write(new FileOutputStream(makeFileName(fileName)));
			
//			ByteArrayOutputStream baos = PDFSummaryBuilder.build(report);
//			baos.writeTo(new FileOutputStream(makePdfName(fileName)));
			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));
			logger.info("End CRR Summary");			
		}
		
	}
	
	
	public class MakeDO extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start MakeDO");
			String fileName = "DO_Ticket";
			Calendar endDate = new GregorianCalendar(2020, Calendar.APRIL, 1);
			DispatchedOutstandingTicketReport report = DispatchedOutstandingTicketReport.buildReport(conn, 101, endDate);
//			XSSFWorkbook workbook = XLSBuilder.build(report);
//			workbook.write(new FileOutputStream(makeFileName(fileName)));
			
//			ByteArrayOutputStream baos = PDFBuilder.build(report);
//			baos.writeTo(new FileOutputStream(makePdfName(fileName)));
			
			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));

			logger.info("End MakeDO");
		}
		
	}
	
	public class MakeInvoiceRegister extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start IRR");
			String fileName = "IRR";
			Integer divisionId = 101;
			Integer month = Calendar.AUGUST;
			Integer year = 2019;
			InvoiceRegisterReport report = InvoiceRegisterReport.buildReport(conn, divisionId, month, year);
//			XSSFWorkbook workbook = XLSBuilder.build(report);
//			workbook.write(new FileOutputStream(makeFileName(fileName)));	
			
//			ByteArrayOutputStream baos = PDFBuilder.build(report);
//			baos.writeTo(new FileOutputStream(makePdfName(fileName)));
			
			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));
			
			logger.info("End IRR");
		}		
	}

	public class makePastDue2 extends ReportMaker {

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("PastDueReport");
			Calendar startDate = new GregorianCalendar(2020, Calendar.FEBRUARY, 1);
			Integer divisionId = 101;
			String fileName = "PastDueDate.xlsx";
			PastDueReport2 report = PastDueReport2.buildReport(conn, startDate, divisionId);
			if ( report == null ) {
				throw new Exception("Null report");
			}
//			XSSFWorkbook workbook = XLSBuilder.build(report);
//			workbook.write(new FileOutputStream(makeFileName(fileName)));
		
			//		String html = HTMLBuilder.build(userReport);
			//		FileUtils.write(new File((makeHtmlName(fileName)), html);
			
			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));
		}
	}

	public class MakePACListing extends ReportMaker {

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start PAC Listing");
			String fileName = "PACListing";
			Integer divisionId = 101;
			Calendar startDate = new GregorianCalendar(2019, Calendar.AUGUST, 1);
			Calendar endDate = new GregorianCalendar(2019, Calendar.AUGUST, 30);
			PacReport report = PacReport.buildReport(conn, divisionId, startDate, endDate);
			XSSFWorkbook workbook = new XSSFWorkbook();
			report.makeXLS(workbook);
//			workbook.write(new FileOutputStream(makeFileName(fileName)));
			
			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));
			
			logger.info("End PAC Listing");			
		}
	}

	public class MakeTicketStatus extends ReportMaker {

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Ticket STatus");
			Integer divisionId = 101;
			Calendar startDate = new GregorianCalendar(2020, Calendar.MARCH, 1);
			Calendar endDate = new GregorianCalendar(2020, Calendar.APRIL, 30);
			String fileName = "TicketStatus";
			TicketStatusReport report = TicketStatusReport.buildReport(conn, divisionId, startDate, endDate);
//			XSSFWorkbook workbook = XLSBuilder.build(report);
//			workbook.write(new FileOutputStream(makeFileName(fileName)));
			
//			ByteArrayOutputStream baos = PDFBuilder.build(report);
//			baos.writeTo(new FileOutputStream(makePdfName(fileName)));
			
			AnsiReportBuilder.writeXLS(report, makeFileName(fileName));
			AnsiReportBuilder.writePDF(report, makePdfName(fileName));
			logger.info("End Ticket Status");			
		}		
	}


	

	

	



}
