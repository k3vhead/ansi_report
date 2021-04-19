package com.ansi.scilla.report.monthlyServiceTaxReport;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Calendar;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.CompoundReport;

public class MonthlyServiceTax extends CompoundReport implements ReportByStartEnd {

	private static final long serialVersionUID = 1L;

	public static final String REPORT_TITLE = "Monthly Service Tax";
	public static final String FILENAME = "MonthlyServiceTax";
	
	
	
	protected MonthlyServiceTax(Connection conn)  throws Exception {
		super(new AbstractReport[] {			
			new MonthlyServiceTaxByDayReport(conn),
			new MonthlyServiceTaxReport(conn)
		});
	}
	
	protected MonthlyServiceTax(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super(new AbstractReport[] {
			new MonthlyServiceTaxByDayReport(conn, startDate, endDate),
			new MonthlyServiceTaxReport(conn, startDate, endDate)
		});
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

	public static MonthlyServiceTax buildReport(Connection conn) throws Exception {
		return new MonthlyServiceTax(conn);
	}

	public static MonthlyServiceTax buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new MonthlyServiceTax(conn, startDate, endDate);
	}
}
