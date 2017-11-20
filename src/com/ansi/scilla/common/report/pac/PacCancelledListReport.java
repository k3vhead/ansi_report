package com.ansi.scilla.common.report.pac;

import java.sql.Connection;
import java.util.Calendar;

public class PacCancelledListReport extends PacDetailReport {

	private static final long serialVersionUID = 1L;
	public static final String REPORT_TITLE = "Cancellations Listing";
	

	public PacCancelledListReport(Connection conn, Integer divisionId,
			Calendar startDate, Calendar endDate) throws Exception {
		super(conn, PacDetailReportType.CANCELLED, divisionId, startDate, endDate);
	}

	public PacCancelledListReport(Connection conn, Integer divisionId)
			throws Exception {
		super(conn, PacDetailReportType.CANCELLED, divisionId);
	}

	public PacCancelledListReport() {
		super(PacDetailReportType.CANCELLED);
	}

}
