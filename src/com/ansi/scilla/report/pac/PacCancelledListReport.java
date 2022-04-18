package com.ansi.scilla.report.pac;

import java.sql.Connection;
import java.util.Calendar;

public class PacCancelledListReport extends PacDetailReport {

	private static final long serialVersionUID = 1L;
	public static final String REPORT_TITLE = "Cancellations Listing";
	public static final String TAB_LABEL = "Cancelled";
	

	public PacCancelledListReport(Connection conn, Integer divisionId,
			Calendar startDate, Calendar endDate) throws Exception {
		super(conn, PacDetailReportType.CANCELLED, divisionId, startDate, endDate);
		super.setTabLabel(TAB_LABEL);
	}

	public PacCancelledListReport(Connection conn, Integer divisionId)
			throws Exception {
		super(conn, PacDetailReportType.CANCELLED, divisionId);
		super.setTabLabel(TAB_LABEL);
	}

	public PacCancelledListReport() {
		super(PacDetailReportType.CANCELLED);
		super.setTabLabel(TAB_LABEL);
	}

}
