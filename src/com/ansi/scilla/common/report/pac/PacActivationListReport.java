package com.ansi.scilla.common.report.pac;

import java.sql.Connection;
import java.util.Calendar;

public class PacActivationListReport extends PacDetailReport {

	private static final long serialVersionUID = 1L;
	public static final String REPORT_TITLE = "Activations Listing";

	public PacActivationListReport(Connection conn, Integer divisionId,
			Calendar startDate, Calendar endDate) throws Exception {
		super(conn, PacDetailReportType.ACTIVATION, divisionId, startDate, endDate);
	}

	public PacActivationListReport(Connection conn, Integer divisionId)
			throws Exception {
		super(conn, PacDetailReportType.ACTIVATION, divisionId);
	}

	public PacActivationListReport() {
		super(PacDetailReportType.ACTIVATION);
	}


	
}
