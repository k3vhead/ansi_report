package com.ansi.scilla.report.test;

import java.io.File;
import java.sql.Connection;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterDetailReport;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.htmlBuilder.HTMLBuilder;

public class TestReportBuilderHTML {

	public static void main(String[] args) {
		try {
//			TesterUtils.makeLoggers();
			new TestReportBuilderHTML().go();
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
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			conn.setAutoCommit(false);
			
			Integer divisionId = 110;
			Calendar startDate = new Midnight(2017, Calendar.JULY, 5);
			Calendar endDate = new Midnight(2017, Calendar.JULY, 5);

//			TicketStatusReport report = new TicketStatusReport(conn, divisionId, startDate, endDate);
//			PacDetailReport report = new PacProposedListReport(conn, divisionId);
			StandardReport report = CashReceiptsRegisterDetailReport.buildReport(conn, startDate, endDate);
			String reportHtml = HTMLBuilder.build(report);
//			System.out.println(reportHtml);
			File file = new File("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/testHtmlBuilder.html");
			FileUtils.write(file, reportHtml);
			
			conn.rollback();
		} catch ( Exception e) {
			if ( conn != null ) {
				conn.rollback();
			}
			throw e;
		} finally {
			if ( conn != null ) {
				conn.close();
			}
		}
	}

}
