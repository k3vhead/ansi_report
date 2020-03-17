package com.ansi.scilla.report.common.parameters;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateUtils;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * First and last days of the input run date (eg. If run date is 3/11/2020, return 2/1/2020 and 2/29/2020)
 * @author dclewis
 *
 */
public class ReportParmDivEndDate extends ApplicationObject implements ReportParameter {

	private static final long serialVersionUID = 1L;

	private Division division;
	private Calendar endDate;
	
	public ReportParmDivEndDate(Division division, Calendar runDate) {
		super();
		this.division = division;
		
		this.endDate = new GregorianCalendar(runDate.get(Calendar.YEAR), runDate.get(Calendar.MONTH), 1, 0, 0, 0);
		this.endDate.add(Calendar.DAY_OF_YEAR, -1);
		DateUtils.truncate(this.endDate, Calendar.DAY_OF_MONTH);
		
	}
	
	
	public Division getDivision() {
		return division;
	}

	public void setDivision(Division division) {
		this.division = division;
	}

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MM/dd/yyyy HH:mm:ss", timezone="America/Chicago")
	public Calendar getEndDate() {
		return endDate;
	}
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MM/dd/yyyy HH:mm:ss", timezone="America/Chicago")
	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}
		
}
