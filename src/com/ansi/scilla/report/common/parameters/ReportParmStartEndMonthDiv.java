package com.ansi.scilla.report.common.parameters;

import java.util.Calendar;

import com.ansi.scilla.common.db.Division;

public class ReportParmStartEndMonthDiv extends ReportParmStartEndMonth {

	private static final long serialVersionUID = 1L;
	
	private Division division;

	public ReportParmStartEndMonthDiv(Division division, Calendar runDate) {
		super(runDate);
		this.division = division;
	}

	public Division getDivision() {
		return division;
	}

	public void setDivision(Division division) {
		this.division = division;
	}
	
	
}
