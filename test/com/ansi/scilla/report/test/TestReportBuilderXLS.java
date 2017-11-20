package com.ansi.scilla.report.test;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.Calendar;

import org.apache.log4j.Level;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.report.cashReceiptsRegister.CashReceiptsRegisterDetailReport;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.reportBuilder.XLSBuilder;

public class TestReportBuilderXLS {

	public static void main(String[] args) {
		try {
			new TestReportBuilderXLS().go();
//			Calendar calendar = Calendar.getInstance();
//			GregorianCalendar gCal = new GregorianCalendar();
//			
//			System.out.println(GregorianCalendar.class.isInstance(gCal));
//			System.out.println(GregorianCalendar.class.isInstance(calendar));
//			System.out.println(Calendar.class.isInstance(gCal));
//			System.out.println(Calendar.class.isInstance(calendar));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void go() throws Exception {
		AppUtils.makeLogger("com.ansi.scilla.common.report", Level.DEBUG, "/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/xlsBuilderOut.log");
		TesterUtils.makeLoggers();
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			
			Integer divisionId = 110;
			Calendar startDate = new Midnight(2017, Calendar.JULY, 5);
			Calendar endDate = new Midnight(2017, Calendar.JULY, 5);
//			TicketStatusReport report = new TicketStatusReport(conn, divisionId, startDate, endDate);
			CashReceiptsRegisterDetailReport report = CashReceiptsRegisterDetailReport.buildReport(conn, startDate, endDate);
//			CashReceiptsRegisterRegionSummary report = new CashReceiptsRegisterRegionSummary(conn, startDate, endDate);
//			PacActivationListReport report = new PacActivationListReport(conn, divisionId, startDate, endDate);
//			PacDetailReport report = new PacProposedListReport(conn, divisionId);
			XSSFWorkbook reportXLS = XLSBuilder.build(report);
			reportXLS.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/xlsBuilderOut.xlsx"));
//			reportXLS.write(new FileOutputStream("ticketStatusReport.xlsx"));
			
			
			conn.rollback();
		} catch ( Exception e) {
			conn.rollback();
			throw e;
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}
	}

}
