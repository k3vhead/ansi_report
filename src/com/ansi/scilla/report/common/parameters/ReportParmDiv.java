package com.ansi.scilla.report.common.parameters;

import java.util.Calendar;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;

/**
 * First and last days of the input run date (eg. If run date is 3/11/2020, return 2/1/2020 and 2/29/2020)
 * @author dclewis
 *
 */
public class ReportParmDiv extends ApplicationObject implements ReportParameter {

	private static final long serialVersionUID = 1L;

	private Division division;
	
	public ReportParmDiv(Division division, Calendar runDate) {
		super();
		this.division = division;
	}
	
	
	public Division getDivision() {
		return division;
	}

	public void setDivision(Division division) {
		this.division = division;
	}
	
}
