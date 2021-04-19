package com.ansi.scilla.report.common.parameters;

import java.util.Calendar;

import com.ansi.scilla.common.ApplicationObject;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ReportParmStartEnd extends ApplicationObject implements ReportParameter {

	private static final long serialVersionUID = 1L;

	protected Calendar startDate;
	protected Calendar endDate;
	
	public ReportParmStartEnd() {
		super();		
	}
	
	public ReportParmStartEnd(Calendar startDate, Calendar endDate) {
		this();
		this.startDate = startDate;
		this.endDate = endDate;
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
