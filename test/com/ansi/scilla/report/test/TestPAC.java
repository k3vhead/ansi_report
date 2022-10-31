package com.ansi.scilla.report.test;

import java.sql.Connection;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.pac.PacReport;
import com.ansi.scilla.report.reportBuilder.AnsiReportBuilder;

public class TestPAC {

	public void go() throws Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			Calendar startDate = new GregorianCalendar(2020, Calendar.MAY, 23);
			Calendar endDate = new GregorianCalendar(2020, Calendar.MAY, 29);
			Integer divisionId = 102;
			PacReport report = PacReport.buildReport(conn, divisionId, startDate, endDate);		// this one fails
//			PacSummaryReport report = new PacSummaryReport(conn, divisionId, startDate, endDate); // this one works
//			PacProposedListReport report = new PacProposedListReport(conn, divisionId, startDate, endDate);	// this one fails
//			PacActivationListReport report = new PacActivationListReport(conn, divisionId, startDate, endDate);	// this one fails
//			PacCancelledListReport report = new PacCancelledListReport(conn, divisionId, startDate, endDate);	// this one fails
			AnsiReportBuilder.writeXLS(report, "/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/pactest.xls");
			AnsiReportBuilder.writePDF(report, "/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/pactest.pdf");
		} finally {
			conn.close();
		}
	}
	
	public static void main(String[] args) {
		try {
			new TestPAC().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
