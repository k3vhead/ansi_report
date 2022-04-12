package com.ansi.scilla.report.pac;

import java.sql.Connection;
import java.util.Calendar;

public class PacProposedListReport extends PacDetailReport {

	private static final long serialVersionUID = 1L;
	
	public static final String REPORT_TITLE = "Proposals Listing";
	public static final String TAB_LABEL = "Proposed";
	

	public PacProposedListReport(Connection conn, Integer divisionId,
			Calendar startDate, Calendar endDate) throws Exception {
		super(conn, PacDetailReportType.PROPOSED, divisionId, startDate, endDate);
		super.setTabLabel(TAB_LABEL);
	}

	public PacProposedListReport(Connection conn, Integer divisionId) throws Exception {
		super(conn, PacDetailReportType.PROPOSED, divisionId);
		super.setTabLabel(TAB_LABEL);
	}

	public PacProposedListReport() {
		super(PacDetailReportType.PROPOSED);
		super.setTabLabel(TAB_LABEL);
	}


}
