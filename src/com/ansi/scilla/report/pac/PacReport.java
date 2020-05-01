package com.ansi.scilla.report.pac;

import java.sql.Connection;
import java.util.Calendar;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivStartEnd;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivision;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.CompoundReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;

public class PacReport extends CompoundReport implements ReportByDivStartEnd, ReportByDivision {

	private static final long serialVersionUID = 1L;
	
	public static final String REPORT_TITLE = "PAC Listing Report";
	public static final String FILENAME = "PAC";
	

	private PacReport(Connection conn, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		super(new AbstractReport[] {
			new PacSummaryReport(conn, divisionId, startDate, endDate),
			new PacProposedListReport(conn, divisionId, startDate, endDate),
			new PacActivationListReport(conn, divisionId, startDate, endDate),
			new PacCancelledListReport(conn, divisionId, startDate, endDate)
		});
	}

	private PacReport(Connection conn, Integer divisionId) throws Exception {
		super(new AbstractReport[] {
			new PacSummaryReport(conn, divisionId),
			new PacProposedListReport(conn, divisionId),
			new PacActivationListReport(conn, divisionId),
			new PacCancelledListReport(conn, divisionId)
		});
	}

	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		for ( AbstractReport report : this.reports ) {
			XLSBuilder.build((StandardReport)report, workbook);
		}
	}
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	
	public static PacReport buildReport(Connection conn, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		return new PacReport(conn, divisionId, startDate, endDate);
	}

	public static PacReport buildReport(Connection conn, Integer divisionId) throws Exception {
		return new PacReport(conn, divisionId);
	}

}
