package com.ansi.scilla.report.common.parameters;

import java.sql.Connection;
import java.util.Calendar;

import com.thewebthing.commons.db2.RecordNotFoundException;

public class ReportParmStartEndQuarter extends ReportParmStartEnd {

	private static final long serialVersionUID = 1L;

	public ReportParmStartEndQuarter(Connection conn, Calendar runDate) throws RecordNotFoundException, Exception {
		makeDates(conn, runDate);
	}

	private void makeDates(Connection conn, Calendar runDate) throws RecordNotFoundException, Exception {
//		StartEndDate dates = ReportUtils.getLastFiscalQuarter(conn, runDate);
//		this.startDate = dates.getStartDate();
//		this.endDate = dates.getEndDate();
	}

	
	
	
	

}
