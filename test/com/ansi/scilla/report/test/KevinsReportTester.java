package com.ansi.scilla.report.test;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.datadumps.ClientContact;


public class KevinsReportTester {

	private final String kevinsTestResultDirectory = "/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/";
	
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
			TesterUtils.makeLoggers();
			TesterUtils.makeLogger("com.ansi.scilla.common.test", Level.DEBUG);
			new KevinsReportTester().makeMyReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void makeMyReport() throws Exception {
		this.logger = Logger.getLogger("com.ansi.scilla.common.test");
		logger.info("Start");
		Connection conn = null;
		
		try {
			conn = AppUtils.getProdConn();
			conn.setAutoCommit(false);
			
			this.divisionId = 102;
			this.month=7;
			this.year=2017;
			this.startDate = new Midnight(2017, Calendar.JULY, 5);
			this.endDate = new Midnight(2017, Calendar.JULY, 5);
			
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
		logger.info("Client Report");
		ClientContact userReport = ClientContact.buildReport(conn);
		XSSFWorkbook workbook = userReport.makeXLS();
		workbook.write(new FileOutputStream(kevinsTestResultDirectory + "clientContact.xlsx"));

		String html = userReport.makeHTML();
		FileUtils.write(new File(kevinsTestResultDirectory + "clientContact.html"), html);
		
	}

	



}
