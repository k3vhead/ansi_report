package com.ansi.scilla.report.common.parameters;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateUtils;

import com.ansi.scilla.common.ApplicationObject;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * First and last days of the input run date (eg. If run date is 3/11/2020, return 2/1/2020 and 2/29/2020)
 * @author dclewis
 *
 */
public class ReportParmStartEndDate extends ApplicationObject implements ReportParameter {

	private static final long serialVersionUID = 1L;

	private Calendar startDate;
	private Calendar endDate;
	
	public ReportParmStartEndDate(Calendar runDate) {
		super();
		this.startDate = new GregorianCalendar(runDate.get(Calendar.YEAR), runDate.get(Calendar.MONTH), 1, 0, 0, 0);
		this.startDate.add(Calendar.MONTH, -1);
		DateUtils.truncate(this.startDate, Calendar.DAY_OF_MONTH);
		
		this.endDate = new GregorianCalendar(runDate.get(Calendar.YEAR), runDate.get(Calendar.MONTH), 1, 0, 0, 0);
		this.endDate.add(Calendar.DAY_OF_YEAR, -1);
		DateUtils.truncate(this.endDate, Calendar.DAY_OF_MONTH);
		
	}
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MM/dd/yyyy HH:mm:ss", timezone="America/Chicago")
	public Calendar getStartDate() {
		return startDate;
	}
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MM/dd/yyyy HH:mm:ss", timezone="America/Chicago")
	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
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
