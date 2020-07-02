package com.ansi.scilla.report.reportBuilder.formatter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.ansi.scilla.common.Midnight;

public class DateFormatter extends ReportFormatter {
	
	private static final long serialVersionUID = 1L;
	
	private String formatString;
	private SimpleDateFormat sdf;
	
	public DateFormatter(String formatString) {
		super();
		this.formatString = formatString;
		this.sdf = new SimpleDateFormat(this.formatString);
		this.textAlignment = TextAlignment.LEFT;
	}
	
	public String getFormatString() {
		return formatString;
	}

	public void setFormatString(String formatString) {
		this.formatString = formatString;
	}

	public String format(Calendar date) {		
		return this.sdf.format(date.getTime());
	}
	
	public String format(GregorianCalendar date) {		
		return this.sdf.format(date.getTime());
	}
	
	public String format(Date date) {		
		return this.sdf.format(date);
	}

	public String format(java.sql.Date date ) {
		return this.sdf.format(date);
	}
		
	public String format(Midnight date) {
		return this.sdf.format(date.getTime());
	}
	
	public String format(Timestamp date) {
		return sdf.format(date);
	}
}
