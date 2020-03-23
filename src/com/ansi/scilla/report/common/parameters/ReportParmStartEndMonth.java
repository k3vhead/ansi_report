package com.ansi.scilla.report.common.parameters;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateUtils;

/**
 * First and last days of the input run date (eg. If run date is 3/11/2020, return 2/1/2020 and 2/29/2020)
 * @author dclewis
 *
 */
public class ReportParmStartEndMonth extends ReportParmStartEnd implements ReportParameter {

	private static final long serialVersionUID = 1L;

	
	public ReportParmStartEndMonth(Calendar runDate) {
		super();
		this.startDate = new GregorianCalendar(runDate.get(Calendar.YEAR), runDate.get(Calendar.MONTH), 1, 0, 0, 0);
		this.startDate.add(Calendar.MONTH, -1);
		DateUtils.truncate(this.startDate, Calendar.DAY_OF_MONTH);
		
		this.endDate = new GregorianCalendar(runDate.get(Calendar.YEAR), runDate.get(Calendar.MONTH), 1, 0, 0, 0);
		this.endDate.add(Calendar.DAY_OF_YEAR, -1);
		DateUtils.truncate(this.endDate, Calendar.DAY_OF_MONTH);
		
	}
		
	
}
