package com.ansi.scilla.report.pac;

import java.sql.Connection;
import java.util.Calendar;

public class PacActivationListReport extends PacDetailReport {

	private static final long serialVersionUID = 1L;
	public static final String REPORT_TITLE = "Activations Listing";
	public static final String TAB_LABEL = "Active";

	public PacActivationListReport(Connection conn, Integer divisionId,
			Calendar startDate, Calendar endDate) throws Exception {
		super(conn, PacDetailReportType.ACTIVATION, divisionId, startDate, endDate);
		super.setTabLabel(TAB_LABEL);
	}

	public PacActivationListReport(Connection conn, Integer divisionId)
			throws Exception {
		super(conn, PacDetailReportType.ACTIVATION, divisionId);
		super.setTabLabel(TAB_LABEL);
	}

	public PacActivationListReport() {
		super(PacDetailReportType.ACTIVATION);
		super.setTabLabel(TAB_LABEL);
	}


	
}
