package com.ansi.scilla.report.pac;

import java.sql.Connection;
import java.util.Calendar;

public class PacProposedListReport extends PacDetailReport {

	private static final long serialVersionUID = 1L;
	
	public static final String REPORT_TITLE = "Proposals Listing";
	

	public PacProposedListReport(Connection conn, Integer divisionId,
			Calendar startDate, Calendar endDate) throws Exception {
		super(conn, PacDetailReportType.PROPOSED, divisionId, startDate, endDate);
	}

	public PacProposedListReport(Connection conn, Integer divisionId) throws Exception {
		super(conn, PacDetailReportType.PROPOSED, divisionId);
	}

	public PacProposedListReport() {
		super(PacDetailReportType.PROPOSED);
	}


}
