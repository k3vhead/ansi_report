package com.ansi.scilla.common.report.pac;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.report.reportBuilder.AbstractReport;
import com.ansi.scilla.report.reportBuilder.CompoundReport;

public class PacReport extends CompoundReport {

	private static final long serialVersionUID = 1L;
	
	public static final String REPORT_TITLE = "PAC Listing Report";
	

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

	public static PacReport buildReport(Connection conn, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		return new PacReport(conn, divisionId, startDate, endDate);
	}

	public static PacReport buildReport(Connection conn, Integer divisionId) throws Exception {
		return new PacReport(conn, divisionId);
	}

}
