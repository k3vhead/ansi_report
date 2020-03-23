package com.ansi.scilla.report.common;

import java.util.Calendar;

import com.ansi.scilla.common.ApplicationObject;

public class StartEndDate extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	private Calendar startDate;
	private Calendar endDate;
	
	public StartEndDate() {
		super();
	}
	
	public StartEndDate(Calendar startDate, Calendar endDate) {
		this();
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Calendar getStartDate() {
		return startDate;
	}
	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}
	public Calendar getEndDate() {
		return endDate;
	}
	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}
	
	
}
