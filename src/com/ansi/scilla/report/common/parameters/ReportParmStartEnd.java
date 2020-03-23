package com.ansi.scilla.report.common.parameters;

import java.util.Calendar;

import com.ansi.scilla.common.ApplicationObject;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * First and last days of the input run date (eg. If run date is 3/11/2020, return 2/1/2020 and 2/29/2020)
 * @author dclewis
 *
 */
public abstract class ReportParmStartEnd extends ApplicationObject implements ReportParameter {

	private static final long serialVersionUID = 1L;

	protected Calendar startDate;
	protected Calendar endDate;
	
	protected ReportParmStartEnd() {
		super();		
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
