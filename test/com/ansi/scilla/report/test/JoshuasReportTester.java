package com.ansi.scilla.report.test;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.pastDue.PastDueReport2;
import com.ansi.scilla.report.reportBuilder.HTMLBuilder;
import com.ansi.scilla.report.reportBuilder.XLSBuilder;;


public class JoshuasReportTester {

	private final String joshuasTestResultDirectory = "/Users/jwlew/Documents/";
	
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
			new JoshuasReportTester().makeMyReport();
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
			this.month=12;
			this.year=2017;
			this.startDate = new Midnight(2017, Calendar.DECEMBER, 31);
			this.endDate = new Midnight(2017, Calendar.DECEMBER, 31);
			
			makeClientUsage(conn);
			
			conn.rollback();
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}
		logger.info("Done");		
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
		workbook.write(new FileOutputStream(joshuasTestResultDirectory + "PastDueDate.xlsx"));

		String html = HTMLBuilder.build(userReport);
		FileUtils.write(new File(joshuasTestResultDirectory + "PastDueDate.html"), html);
		
	}

	



}
