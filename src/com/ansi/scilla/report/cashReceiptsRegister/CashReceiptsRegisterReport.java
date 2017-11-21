package com.ansi.scilla.report.cashReceiptsRegister;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.report.reportBuilder.AbstractReport;
import com.ansi.scilla.report.reportBuilder.CompoundReport;

public class CashReceiptsRegisterReport extends CompoundReport {

	private static final long serialVersionUID = 1L;

	public static final String REPORT_TITLE = "Cash Receipts Register";
	
	protected CashReceiptsRegisterReport(Connection conn)  throws Exception {
		super(new AbstractReport[] {
			new CashReceiptsRegisterSummaryReport(conn),
			new CashReceiptsRegisterDetailReport(conn)
		});
	}
	
	protected CashReceiptsRegisterReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super(new AbstractReport[] {
			new CashReceiptsRegisterSummaryReport(conn, startDate, endDate),
			new CashReceiptsRegisterDetailReport(conn, startDate, endDate)
		});
	}
	

	public static CashReceiptsRegisterReport buildReport(Connection conn) throws Exception {
		return new CashReceiptsRegisterReport(conn);
	}

	public static CashReceiptsRegisterReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new CashReceiptsRegisterReport(conn, startDate, endDate);
	}
}
