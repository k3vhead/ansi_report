package com.ansi.scilla.report.woAndFees;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.CompoundReport;

public class WOAndFeesReport extends CompoundReport implements ReportByNoInput {

	
	private static final long serialVersionUID = 1L;

	public static final String REPORT_TITLE = "WO and Fees Report";
	public static final String FILENAME = "WO and Fees Report";
	
	
	
	protected WOAndFeesReport(Connection conn)  throws Exception {
		super(makeReports(conn));
	}
	
	protected WOAndFeesReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super(makeReports(conn, startDate, endDate));
	}
	
	private static AbstractReport[] makeReports(Connection conn) throws Exception {
		return makeReports(conn, Calendar.getInstance(), Calendar.getInstance());
	}

	private static AbstractReport[] makeReports(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		List<AbstractReport> reportList = new ArrayList<AbstractReport>();
		reportList.add(WOAndFeesSummaryReport.buildReport(conn));
		
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("select division_id from division order by division_nbr, division_code");
		while ( rs.next() ) {
			Integer divisionId = rs.getInt("division_id");
			reportList.add(WOAndFeesDetailReport.buildReport(conn, divisionId, startDate, endDate));
		}
		rs.close();
		
		
		AbstractReport[] reportArray = new AbstractReport[reportList.size()];
		for ( int i = 0; i < reportList.size(); i++ ) {
			reportArray[i] = reportList.get(i);
		}
		return reportArray;
	}

	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		for ( AbstractReport report : this.getReports() ) {
			Method method = report.getClass().getMethod("makeXLS", new Class<?>[] {XSSFWorkbook.class});
			method.invoke(report, new Object[] {workbook});
		}		
	}

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	public static WOAndFeesReport buildReport(Connection conn) throws Exception {
		return new WOAndFeesReport(conn);
	}

	public static WOAndFeesReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new WOAndFeesReport(conn, startDate, endDate);
	}

}
