package com.ansi.scilla.report.common.parameters;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.thewebthing.commons.db2.RecordNotFoundException;

public class ReportParmStartEndQuarterDiv extends ReportParmStartEndQuarter {

	private static final long serialVersionUID = 1L;

	private Division division;
	
	public ReportParmStartEndQuarterDiv(Connection conn, Division division, Calendar runDate) throws RecordNotFoundException, Exception {
		super(conn, runDate);
		this.division = division;
	}

	public Division getDivision() {
		return division;
	}

	public void setDivision(Division division) {
		this.division = division;
	}
	
}
