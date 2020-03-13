package com.ansi.scilla.report.common.parameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;

/**
 * First and last days of the input run date (eg. If run date is 3/11/2020, return 2/1/2020 and 2/29/2020)
 * @author dclewis
 *
 */
public class ReportParmDivMonthYear extends ApplicationObject implements ReportParameter {

	private static final long serialVersionUID = 1L;

	private Division division;
	private Integer month;
	private Integer year;
	
	public ReportParmDivMonthYear(Division division, Calendar runDate) {
		super();
		this.division = division;
		
		Calendar workDate = new GregorianCalendar(runDate.get(Calendar.YEAR), runDate.get(Calendar.MONTH), 1, 0, 0, 0);
		workDate.add(Calendar.MONTH, -1);
		
		// this converts month to 01, 02, ... 12
		// instead of the java zero-based 00, 01, ... 11 for month of the year
		SimpleDateFormat sdf = new SimpleDateFormat("MM");
		this.month = Integer.valueOf(sdf.format(workDate.getTime()));   
		this.year = workDate.get(Calendar.YEAR);
	}
	
	
	public Division getDivision() {
		return division;
	}
	public void setDivision(Division division) {
		this.division = division;
	}
	public Integer getMonth() {
		return month;
	}
	public void setMonth(Integer month) {
		this.month = month;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	
	
}
