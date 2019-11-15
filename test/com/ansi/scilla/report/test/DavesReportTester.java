package com.ansi.scilla.report.test;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.db.ApplicationProperties;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterDetailReport;
import com.ansi.scilla.report.invoiceRegisterReport.InvoiceRegisterReport;
import com.ansi.scilla.report.pac.PacReport;
import com.ansi.scilla.report.pastDue.PastDueReport2;
import com.ansi.scilla.report.reportBuilder.HTMLBuilder;
import com.ansi.scilla.report.reportBuilder.XLSBuilder;
import com.ansi.scilla.report.sixMonthRollingVolume.SixMonthRollingVolumeReport;
import com.ansi.scilla.report.ticket.DispatchedOutstandingTicketReport;
import com.ansi.scilla.report.ticket.TicketStatusReport;;


public class DavesReportTester {

	private final String testResultDirectory = "/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_headers/ansi_report/";
	
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
		this.logger = Logger.getLogger("com.ansi.scilla.report.reportBuilder");
		logger.info("Start");

		List<Thread> threadList = new ArrayList<Thread>();
		
//			make6mrv(conn);
//			makeClientUsage(conn);
		threadList.add(new Thread(new MakeCRRDetail()));
		threadList.add(new Thread(new MakeDO()));
		threadList.add(new Thread(new MakeInvoiceRegister()));
		threadList.add(new Thread(new MakePACListing()));
//			makePastDue2(conn);
		threadList.add(new Thread(new MakeTicketStatus()));

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

	}
	

	private void make6mrv(Connection conn) throws Exception {
		Integer divisionId = 101;
		Integer month = Calendar.JULY;
		Integer year = 2019;
		SixMonthRollingVolumeReport report = SixMonthRollingVolumeReport.buildReport(conn, divisionId, month, year);
		XSSFWorkbook workbook = report.makeXLS();
		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_headers/6MRV.xlsx"));
	}

	private void makeClientUsage(Connection conn) throws Exception {
		Calendar startDate = null;
		Integer divisionId = null;
		logger.info("PastDueReport");
		PastDueReport2 userReport = PastDueReport2.buildReport(conn, startDate, divisionId);
		XSSFWorkbook workbook = XLSBuilder.build(userReport);
		workbook.write(new FileOutputStream(testResultDirectory + "ClientUsage.xlsx"));
	
		String html = HTMLBuilder.build(userReport);
		FileUtils.write(new File(testResultDirectory + "ClientUsage.html"), html);
	
	}

	public class MakeCRRDetail extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start CRR");
			Calendar startDate = new GregorianCalendar(2019, Calendar.AUGUST, 1);
			Calendar endDate = new GregorianCalendar(2019, Calendar.AUGUST, 31);
			CashReceiptsRegisterDetailReport crrDetail = CashReceiptsRegisterDetailReport.buildReport(conn, startDate, endDate);
			XSSFWorkbook workbook = XLSBuilder.build(crrDetail);
			workbook.write(new FileOutputStream(makeFileName("CRR_DETAIL")));
			logger.info("End CRR");			
		}
		
	}
	
	
	public class MakeDO extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start MakeDO");
			Calendar endDate = new GregorianCalendar(2019, Calendar.AUGUST, 31);
			DispatchedOutstandingTicketReport report = DispatchedOutstandingTicketReport.buildReport(conn, 101, endDate);
			XSSFWorkbook workbook = XLSBuilder.build(report);
			workbook.write(new FileOutputStream(makeFileName("DO_Ticket")));
			logger.info("End MakeDO");
		}
		
	}
	
	public class MakeInvoiceRegister extends ReportMaker {
		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start IRR");
			Integer divisionId = 101;
			Integer month = Calendar.AUGUST;
			Integer year = 2019;
			InvoiceRegisterReport report = InvoiceRegisterReport.buildReport(conn, divisionId, month, year);
			XSSFWorkbook workbook = XLSBuilder.build(report);
			workbook.write(new FileOutputStream(makeFileName("IRR")));	
			logger.info("End IRR");
		}		
	}


	private void makePastDue2(Connection conn) throws Exception {
		logger.info("PastDueReport");
		Calendar startDate = null;
		Integer divisionId = null;
		PastDueReport2 report = PastDueReport2.buildReport(conn, startDate, divisionId);
		if ( report == null ) {
			throw new Exception("Null report");
		}
		XSSFWorkbook workbook = XLSBuilder.build(report);
		workbook.write(new FileOutputStream(testResultDirectory + "PastDueDate.xlsx"));
	
		//		String html = HTMLBuilder.build(userReport);
		//		FileUtils.write(new File(testResultDirectory + "PastDueDate.html"), html);		
	}

	public class MakePACListing extends ReportMaker {

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start PAC Listing");
			Integer divisionId = 101;
			Calendar startDate = new GregorianCalendar(2019, Calendar.AUGUST, 1);
			Calendar endDate = new GregorianCalendar(2019, Calendar.AUGUST, 30);
			PacReport report = PacReport.buildReport(conn, divisionId, startDate, endDate);
			XSSFWorkbook workbook = new XSSFWorkbook();
			report.makeXLS(workbook);
			workbook.write(new FileOutputStream(makeFileName("PACListing")));
			logger.info("End PAC Listing");			
		}
	}

	public class MakeTicketStatus extends ReportMaker {

		@Override
		public void makeReport(Connection conn) throws Exception {
			logger.info("Start Ticket STatus");
			Integer divisionId = 101;
			Calendar startDate = new GregorianCalendar(2019, Calendar.AUGUST, 1);
			Calendar endDate = new GregorianCalendar(2019, Calendar.AUGUST, 30);
			TicketStatusReport report = TicketStatusReport.buildReport(conn, divisionId, startDate, endDate);
			XSSFWorkbook workbook = XLSBuilder.build(report);
			workbook.write(new FileOutputStream(makeFileName("TicketStatus")));
			logger.info("End Ticket Status");			
		}		
	}


	private String makeFileName(String fileName) {
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmss");
		return testResultDirectory + fileName + "_" + sdf.format(today) + ".xlsx";
	}

	

	



}
