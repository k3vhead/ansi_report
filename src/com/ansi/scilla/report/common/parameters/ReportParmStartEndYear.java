package com.ansi.scilla.report.common.parameters;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.report.common.ReportUtils;
import com.ansi.scilla.report.common.StartEndDate;
import com.thewebthing.commons.db2.RecordNotFoundException;

public class ReportParmStartEndYear extends ReportParmStartEnd {

	private static final long serialVersionUID = 1L;

	public ReportParmStartEndYear(Connection conn, Calendar runDate) throws RecordNotFoundException, Exception {
		makeDates(conn, runDate);
	}

	private void makeDates(Connection conn, Calendar runDate) throws RecordNotFoundException, Exception {
		StartEndDate dates = ReportUtils.getFiscalYear(conn, runDate);
		this.startDate = dates.getStartDate();
		this.endDate = dates.getEndDate();
	}

	
	
	
	

}
