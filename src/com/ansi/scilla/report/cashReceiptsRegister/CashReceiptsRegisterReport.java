package com.ansi.scilla.report.cashReceiptsRegister;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Calendar;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.AbstractReport;
import com.ansi.scilla.report.reportBuilder.CompoundReport;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;

public class CashReceiptsRegisterReport extends CompoundReport implements ReportByStartEnd {

	private static final long serialVersionUID = 1L;

	public static final String REPORT_TITLE = "Cash Receipts Register";
	public static final String FILENAME = "CRR";
	
	
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
	
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	
	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		for ( AbstractReport report : this.getReports() ) {
			Method method = report.getClass().getMethod("makeXLS", new Class<?>[] {XSSFWorkbook.class});
			method.invoke(report, new Object[] {workbook});
		}		
	}

	public static CashReceiptsRegisterReport buildReport(Connection conn) throws Exception {
		return new CashReceiptsRegisterReport(conn);
	}

	public static CashReceiptsRegisterReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new CashReceiptsRegisterReport(conn, startDate, endDate);
	}
}
