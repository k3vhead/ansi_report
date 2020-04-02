package com.ansi.scilla.report.reportBuilder.reportBy;

import java.util.Calendar;

import com.ansi.scilla.common.db.Division;

public interface ReportBy {

	/**
	 * Create a parameter-specific filename for this instance of a report
	 * 
	 * @param reportFileName
	 * @param runDate
	 * @param division
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public abstract String makeFileName(String reportFileName, Calendar runDate, Division division, Calendar startDate, Calendar endDate);
	
	
	
}
