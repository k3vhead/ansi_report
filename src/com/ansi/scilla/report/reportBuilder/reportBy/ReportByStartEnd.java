package com.ansi.scilla.report.reportBuilder.reportBy;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;

public interface ReportByStartEnd extends ReportBy {
	
	@Override
	public default String makeFileName(String reportFileName, Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");
		String startDateName = yyyymmdd.format(startDate.getTime());
		String endDateName = yyyymmdd.format(endDate.getTime());

		String asOf = yyyymmdd.format(runDate.getTime());
		String fileName = reportFileName + " for " + startDateName + " to " + endDateName + " as of " + asOf;
		return fileName.replaceAll(" ", "_");		
	}
	
	

}
