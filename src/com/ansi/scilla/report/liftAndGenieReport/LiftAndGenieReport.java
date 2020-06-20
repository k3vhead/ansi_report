package com.ansi.scilla.report.liftAndGenieReport;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Calendar;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.report.reportBuilder.AbstractReport;
import com.ansi.scilla.report.reportBuilder.CompoundReport;

public class LiftAndGenieReport extends CompoundReport {

	private static final long serialVersionUID = 1L;

	public static final String REPORT_TITLE = "Lift And Genie";
	
	
	
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

	public static LiftAndGenieReport buildReport(Connection conn) throws Exception {
		return new LiftAndGenieReport(conn);
	}

	public static LiftAndGenieReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new LiftAndGenieReport(conn, startDate, endDate);
	}
}
