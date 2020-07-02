package com.ansi.scilla.report.liftAndGenieReport;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Calendar;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.CompoundReport;

public class LiftAndGenieReport extends CompoundReport implements ReportByDivStartEnd {

	private static final long serialVersionUID = 1L;

	public static final String REPORT_TITLE = "Lift And Genie";
	public static final String FILENAME = "LiftAndGenie";
	
	
	
	protected LiftAndGenieReport(Connection conn)  throws Exception {
		super(new AbstractReport[] {			
			new LiftAndGenieDivisionSummary(conn),
			new LiftAndGenieDetailReport(conn)
		});
	}
	
	protected LiftAndGenieReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super(new AbstractReport[] {
			new LiftAndGenieDivisionSummary(conn, startDate, endDate),
			new LiftAndGenieDetailReport(conn, startDate, endDate)
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

	public static LiftAndGenieReport buildReport(Connection conn) throws Exception {
		return new LiftAndGenieReport(conn);
	}

	public static LiftAndGenieReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new LiftAndGenieReport(conn, startDate, endDate);
	}
}
