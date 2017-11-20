package com.ansi.scilla.report.test;

import java.io.File;
import java.sql.Connection;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.report.cashReceiptsRegister.CashReceiptsRegisterSummaryReport;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.reportBuilder.HTMLSummaryBuilder;

public class TestSummaryReportBuilderHTML {

	public static void main(String[] args) {
		try {
			new TestSummaryReportBuilderHTML().go();
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
			Calendar startDate = new Midnight(2017, Calendar.JULY, 1);
			Calendar endDate = new Midnight(2017, Calendar.JULY, 31);

			CashReceiptsRegisterSummaryReport report = CashReceiptsRegisterSummaryReport.buildReport(conn, startDate, startDate);
			String reportHtml = HTMLSummaryBuilder.build(report);
			System.out.println(reportHtml);
			File file = new File("/home/dclewis/Documents/webthing_v2/projects/ANSI/testResults/htmlSummaryBuilderOut.html");
			FileUtils.write(file, reportHtml);
			
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
