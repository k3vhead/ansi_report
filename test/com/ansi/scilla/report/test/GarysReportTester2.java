package com.ansi.scilla.report.test;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.datadumps.AccountsReceivableTotalsOver60Detail;
import com.ansi.scilla.report.pac.PacReport;
import com.ansi.scilla.report.pastDue.PastDueReport2;
import com.ansi.scilla.report.reportBuilder.htmlBuilder.HTMLBuilder;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;
import com.ansi.scilla.report.sixMonthRollingVolume.SmrvReport;;


public class GarysReportTester2 {

	private final String testResultDirectory = "";
	
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
//			TesterUtils.makeLoggers();
//			TesterUtils.makeLogger("com.ansi.scilla.common.test", Level.DEBUG);
			new GarysReportTester2().makeMyReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void makeMyReport() throws Exception {
//		this.logger = Logger.getLogger("com.ansi.scilla.report.reportBuilder");
		this.logger = LogManager.getLogger("com.ansi.scilla.report.reportBuilder");
		logger.info("Start");
		Connection conn = null;
		
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			
			this.divisionId = 101;
			this.month=01;
			this.year=2020;
			this.startDate = new Midnight(2018, Calendar.JULY, 20);
			this.endDate = new Midnight(2017, Calendar.DECEMBER, 31);
			
			makeSmrvReport(conn);
//			makePacReport(conn);
//			makeClientUsage(conn);
			
			conn.rollback();
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}
		logger.info("Done");		
	}


	public void makePacReport(Connection conn) throws Exception {
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

	private void makeOver60DetailReport(Connection conn) throws Exception {
		Integer divisionId = 100;
		Integer month = Calendar.JULY;
		Integer year = 2019;
		AccountsReceivableTotalsOver60Detail report = AccountsReceivableTotalsOver60Detail.buildReport(conn);
//		Logger logger = LogManager.getLogger(JobUtils.class);
		logger.log(Level.INFO, "<"+report+">");
		XSSFWorkbook workbook = new XSSFWorkbook();
//		report.makeXLS(workbook);
		workbook.write(new FileOutputStream(makeFileName("SMRVListing")));
//		String html = HTMLBuilder.build(report);
//		FileUtils.write(new File(joshuasTestResultDirectory + "PastDueDate.html"), html);
//		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_headers/6MRV.xlsx"));
	}

	private void makeSmrvReport(Connection conn) throws Exception {
		Integer divisionId = 100;
		Integer month = Calendar.JULY;
		Integer year = 2019;
		SmrvReport report = SmrvReport.buildReport(conn, divisionId, month, year);
//		Logger logger = LogManager.getLogger(JobUtils.class);
		logger.log(Level.INFO, "<"+report+">");
		XSSFWorkbook workbook = new XSSFWorkbook();
		report.makeXLS(workbook);
		workbook.write(new FileOutputStream(makeFileName("SMRVListing")));
//		String html = HTMLBuilder.build(report);
//		FileUtils.write(new File(joshuasTestResultDirectory + "PastDueDate.html"), html);
//		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_headers/6MRV.xlsx"));
	}

	private void makeClientUsage(Connection conn) throws Exception {
		logger.info("PastDueReport");
		//DispatchedOutstandingTicketReport userReport = DispatchedOutstandingTicketReport.buildReport(conn, divisionId, endDate);
		//java.util.Date sDate;
		PastDueReport2 userReport = PastDueReport2.buildReport(conn, startDate, divisionId);
		XSSFWorkbook workbook = XLSBuilder.build(userReport);
//		XSSFWorkbook workbook = userReport.makeXLS();
//		CashReceiptsRegisterDetailReport userReport = CashReceiptsRegisterDetailReport.buildReport(conn, startDate, endDate);
//		XSSFWorkbook workbook = XLSBuilder.build(userReport);
		workbook.write(new FileOutputStream(testResultDirectory + "PastDueDate.xlsx"));

		String html = HTMLBuilder.build(userReport);
		FileUtils.write(new File(testResultDirectory + "PastDueDate.html"), html);
		
	}

	private String makeFileName(String fileName) {
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmss");
		return testResultDirectory + fileName + "_" + sdf.format(today) + ".xlsx";
	}




}
