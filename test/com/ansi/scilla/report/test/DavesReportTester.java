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
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterDetailReport;
import com.ansi.scilla.report.pastDue.PastDueReport2;
import com.ansi.scilla.report.reportBuilder.HTMLBuilder;
import com.ansi.scilla.report.reportBuilder.XLSBuilder;
import com.ansi.scilla.report.sixMonthRollingVolume.SixMonthRollingVolumeReport;
import com.ansi.scilla.report.ticket.DispatchedOutstandingTicketReport;;


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
		Connection conn = null;
		
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			
			this.divisionId = 101;
			this.month=Calendar.SEPTEMBER;
			this.year=2019;
			this.startDate = new Midnight(2018, Calendar.JULY, 20);
			this.endDate = new Midnight(2017, Calendar.DECEMBER, 31);
			
//			makeClientUsage(conn);
//			make6mrv(conn);
//			makePastDue2(conn);
//			makeCRR(conn);
			makeDO(conn);
			
			conn.rollback();
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}
		logger.info("Done");		
	}


	private void makeDO(Connection conn) throws Exception {
		Calendar endDate = new GregorianCalendar(2019, Calendar.AUGUST, 31);
		DispatchedOutstandingTicketReport report = DispatchedOutstandingTicketReport.buildReport(conn, 101, endDate);
		XSSFWorkbook workbook = XLSBuilder.build(report);
		workbook.write(new FileOutputStream(makeFileName("DO_Ticket")));
	}

	private void makeCRR(Connection conn) throws Exception {
		Calendar startDate = new GregorianCalendar(2019, Calendar.AUGUST, 1);
		Calendar endDate = new GregorianCalendar(2019, Calendar.AUGUST, 31);
		CashReceiptsRegisterDetailReport crrDetail = CashReceiptsRegisterDetailReport.buildReport(conn, startDate, endDate);
		XSSFWorkbook workbook = XLSBuilder.build(crrDetail);
		workbook.write(new FileOutputStream(makeFileName("CRR_DETAIL")));
	}

	private String makeFileName(String fileName) {
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmss");
//		testResultDirectory + "CRR_Detail.xlsx"
		return testResultDirectory + fileName + "_" + sdf.format(today) + ".xlsx";
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

	private void makePastDue2(Connection conn) throws Exception {
		logger.info("PastDueReport");
		PastDueReport2 report = PastDueReport2.buildReport(conn, startDate, divisionId);
		if ( report == null ) {
			throw new Exception("Null report");
		}
		XSSFWorkbook workbook = XLSBuilder.build(report);
		workbook.write(new FileOutputStream(testResultDirectory + "PastDueDate.xlsx"));

//		String html = HTMLBuilder.build(userReport);
//		FileUtils.write(new File(testResultDirectory + "PastDueDate.html"), html);		
	}

	



}
