package com.ansi.scilla.report.test;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.report.cashReceiptsRegister.CashReceiptsRegisterReport;
import com.ansi.scilla.common.report.datadumps.AddressUsage;
import com.ansi.scilla.common.report.datadumps.ClientContact;
import com.ansi.scilla.common.report.datadumps.UserListReport;
import com.ansi.scilla.common.report.invoiceRegisterReport.InvoiceRegisterReport;
import com.ansi.scilla.common.report.pac.PacReport;
import com.ansi.scilla.common.report.sixMonthRollingVolume.SixMonthRollingVolumeReport;
import com.ansi.scilla.common.report.ticket.TicketStatusReport;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.reportBuilder.AbstractReport;
import com.ansi.scilla.report.reportBuilder.AnsiReport;
import com.ansi.scilla.report.reportBuilder.CustomReport;
import com.ansi.scilla.report.reportBuilder.HTMLBuilder;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.StandardSummaryReport;
import com.ansi.scilla.report.reportBuilder.XLSBuilder;
import com.ansi.scilla.report.reportBuilder.XLSSummaryBuilder;


public class TestReportReflection {

	protected ReportType reportType;
	protected Calendar startDate;
	protected Calendar endDate;
	private Integer divisionId;
	private Integer month;
	private Integer year;
	private HashMap<String, String> reportDisplay;
	private Logger logger;

	
	public static void main(String[] args) {
		try {
			TesterUtils.makeLoggers();
			TesterUtils.makeLogger("com.ansi.scilla.common.test", Level.DEBUG);
			new TestReportReflection().makEmAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void makEmAll() throws Exception {
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
			
//			makeCashReceipts(conn);
//			makeUserList(conn);
//			makeInvoiceRegister(conn);
//			makePac(conn);
//			make6mrv(conn);
//			makeTicketStatus(conn);
			makeAddressUsage(conn);
//			makeClientUsage(conn);
			
			conn.rollback();
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}
		logger.info("Done");		
	}

	private void makeTicketStatus(Connection conn) throws Exception {
		logger.info("Starting Ticket Status");
		TicketStatusReport tsr = TicketStatusReport.buildReport(conn, divisionId);
		XSSFWorkbook workbook = XLSBuilder.build(tsr);
		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/ticketStatusReport.xlsx"));
				
	}

	private void make6mrv(Connection conn) throws Exception {
		logger.info("Starting 6mrv");
		SixMonthRollingVolumeReport smrv = SixMonthRollingVolumeReport.buildReport(conn, divisionId, month, year);
		XSSFWorkbook workbook = smrv.makeXLS();
		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/smrv.xlsx"));
		String html = smrv.makeHTML();
		FileUtils.write(new File("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/smrv.html"), html);
	}

	private void makePac(Connection conn) throws Exception {
		logger.info("Starting PAC");
		PacReport pacReport = PacReport.buildReport(conn, divisionId);
		for ( int idx = 0; idx < pacReport.getReports().length; idx++ ) {
			AbstractReport report = pacReport.getReports()[idx];
			try {
				XSSFWorkbook workbook = XLSBuilder.build((StandardReport)report);
				workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/pacReport" +idx + ".xlsx"));
			} catch ( ClassCastException e) {
				logger.info("Can't cast pac report " + idx);
			}
		}		
	}

	private void makeInvoiceRegister(Connection conn) throws Exception {
		logger.info("Starting Invoice REgister");
		InvoiceRegisterReport irr = InvoiceRegisterReport.buildReport(conn, divisionId, month, year);
		XSSFWorkbook workbook = XLSBuilder.build(irr);
		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/invoiceRegisterReport.xlsx"));
				
	}

	private void makeUserList(Connection conn) throws Exception {
		logger.info("User Report");
		XSSFWorkbook workbook = new XSSFWorkbook();
		UserListReport userReport = UserListReport.buildReport(conn);
		workbook = userReport.makeXLS();
		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/userReport.xlsx"));		
	}


	private void makeAddressUsage(Connection conn) throws Exception {
		logger.info("Address Report");
		XSSFWorkbook workbook = new XSSFWorkbook();
		AddressUsage userReport = AddressUsage.buildReport(conn);
//		workbook = userReport.makeXLS();
//		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/addressUsage.xlsx"));
		String html = userReport.makeHTML();
		FileUtils.write(new File("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/addressUsage.html"), html);
	}

	
	private void makeClientUsage(Connection conn) throws Exception {
		logger.info("Client Report");
		XSSFWorkbook workbook = new XSSFWorkbook();
		ClientContact userReport = ClientContact.buildReport(conn);
		workbook = userReport.makeXLS();
		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/clientContact.xlsx"));		
	}

	
	private void makeCashReceipts(Connection conn) throws Exception {
		logger.info("Starting Cash Receipts");
		XSSFWorkbook workbook = new XSSFWorkbook();
		CashReceiptsRegisterReport crrReport = CashReceiptsRegisterReport.buildReport(conn, startDate, endDate);
		try {
			workbook = XLSSummaryBuilder.build((StandardSummaryReport)crrReport.getReports()[0]);
			workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/crrReportSummary.xlsx"));			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		for ( int idx = 1; idx < crrReport.getReports().length; idx++ ) {
			AbstractReport report = crrReport.getReports()[idx];
			try {
				workbook = XLSBuilder.build((StandardReport)report);
				workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/crrReport" +idx + ".xlsx"));
			} catch ( ClassCastException e) {
				logger.info("Can't cast crr report " + idx);
			}
		}
		
	}

	public void go3() throws Exception {
		this.logger = Logger.getLogger("com.ansi.scilla.common.test");
		logger.info("Start");
		Connection conn = null;
		
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			logger.debug("building report");
			UserListReport report = UserListReport.buildReport(conn);
//			String html = report.makeHTML();
//			FileUtils.write(new File("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/htmlBuilderOut.html"), html);
			XSSFWorkbook wb = report.makeXLS();
			wb.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/xlsBuilderOut.xlsx"));
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}
		logger.info("Done");
	}
	
	public void go2() throws Exception {
		this.logger = Logger.getLogger("com.ansi.scilla.common.test");
		logger.debug("Start");
		Connection conn = null;
		
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			this.reportType = ReportType.SIX_MONTH_ROLLING_VOLUME_REPORT;
			this.divisionId = 102;
			this.month=7;
			this.year=2017;
//			this.startDate = new Midnight(2017, Calendar.JULY, 5);
//			this.endDate = new Midnight(2017, Calendar.JULY, 5);

			CustomReport report1 = SixMonthRollingVolumeReport.buildReport(conn, divisionId, month, year);
//			CustomReport report2 = SixMonthRollingVolumeReport.buildReport(conn, divisionId, 1, 2018);
//			XSSFWorkbook wb = report.makeXLS();
//			XSSFWorkbook wb = new XSSFWorkbook();
//			report1.add2XLS(wb);
//			report2.add2XLS(wb);
//			wb.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/xlsBuilderOut.xlsx"));
			String html = report1.makeHTML();
			FileUtils.write(new File("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/htmlBuilderOut.html"), html);
		} finally {
			AppUtils.closeQuiet(conn);
		}
	}
	public void go() throws Exception {
		this.logger = Logger.getLogger("com.ansi.scilla.common.test");
		logger.debug("Start");
		Connection conn = null;
		
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			this.reportType = ReportType.INVOICE_REGISTER_REPORT;
			this.divisionId = 102;
			this.month=7;
			this.year=2017;
//			this.startDate = new Midnight(2017, Calendar.JULY, 5);
//			this.endDate = new Midnight(2017, Calendar.JULY, 5);

			StandardReport report = (StandardReport)build(conn);
			
			String reportHtml = HTMLBuilder.build(report);
//			System.out.println(reportHtml);
			File file = new File("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/testHtmlBuilder.html");
			FileUtils.write(file, reportHtml);
			
			XSSFWorkbook reportXLS = XLSBuilder.build(report);
			reportXLS.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/testXLSBuilder.xlsx"));
			
			conn.rollback();
		} finally {
			conn.close();
		}
		logger.debug("End");
	}

	public ReportType getReportType() {
		return reportType;
	}
	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}
	public Calendar getStartDate() {
		return startDate;
	}
	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public Integer getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Integer divisionId) {
		this.divisionId = divisionId;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public HashMap<String, String> getReportDisplay() {
		return reportDisplay;
	}

	public void setReportDisplay(HashMap<String, String> reportDisplay) {
		this.reportDisplay = reportDisplay;
	}

	/**
	 * Copied/butchered from ansi_web ReportDefinition (used by servlet to create standard reports
	 * 
	 * @param conn
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public AnsiReport build(Connection conn) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> reportClass = Class.forName(reportType.reportClassName());
		int arrayLength = reportType.builderParms().length + 1;   // connection + all parms
		Class<?>[] classList = new Class<?>[arrayLength];
		Object[] objectList = new Object[arrayLength];
		
		classList[0] = Connection.class;
		objectList[0] = conn;
		
		for ( int i = 0; i < reportType.builderParms().length; i++ ) {
			int idx = i + 1;
			String methodName = "get" + StringUtils.capitalize(reportType.builderParms()[i]);
			Method getter = this.getClass().getMethod(methodName, (Class<?>[])null);
			classList[idx] = getter.getReturnType();
			objectList[idx] = getter.invoke(this, (Object[])null);
		}
		
		Method builderMethod = reportClass.getMethod("buildReport", classList);
		AnsiReport report = (AnsiReport)builderMethod.invoke(null, objectList);
		return report;
	}
}
