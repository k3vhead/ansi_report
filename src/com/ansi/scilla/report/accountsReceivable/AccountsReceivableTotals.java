package com.ansi.scilla.report.accountsReceivable;

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

public class AccountsReceivableTotals extends CompoundReport implements ReportByNoInput {

	
	private static final long serialVersionUID = 1L;

	public static final String REPORT_TITLE = "Accounts Receivable Totals";
	public static final String FILENAME = "Accounts Receivable Totals";
	
	
	
	protected AccountsReceivableTotals(Connection conn)  throws Exception {
		super(makeReports(conn));
	}
	
	protected AccountsReceivableTotals(Connection conn, Calendar runDate) throws Exception {
		super(makeReports(conn, runDate));
	}
	
	private static AbstractReport[] makeReports(Connection conn) throws Exception {
		return makeReports(conn, Calendar.getInstance());
	}

	private static AbstractReport[] makeReports(Connection conn, Calendar runDate) throws Exception {
		List<AbstractReport> reportList = new ArrayList<AbstractReport>();
		reportList.add(AccountsReceivableTotalsSummary.buildReport(conn));
		
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("select division_id from division order by division_nbr, division_code");
		while ( rs.next() ) {
			Integer divisionId = rs.getInt("division_id");
			reportList.add(AccountsReceivableTotalsByDivision.buildReport(conn, divisionId));
		}
		rs.close();
		
		
		AbstractReport[] reportArray = new AbstractReport[reportList.size()];
		for ( int i = 0; i < reportList.size(); i++ ) {
			reportArray[i] = reportList.get(i);
		}
		return reportArray;
	}

	/**
	 * Add a XSSF worksheet to the input XSSF workbook
	 * @param workbook the workbook to which we are adding a sheet
	 * @throws Exception when something bad happens
	 */
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

	public static AccountsReceivableTotals buildReport(Connection conn) throws Exception {
		return new AccountsReceivableTotals(conn);
	}

	public static AccountsReceivableTotals buildReport(Connection conn, Calendar runDate) throws Exception {
		return new AccountsReceivableTotals(conn, runDate);
	}

}
