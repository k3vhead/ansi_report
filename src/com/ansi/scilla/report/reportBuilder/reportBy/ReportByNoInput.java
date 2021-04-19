package com.ansi.scilla.report.reportBuilder.reportBy;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;

public interface ReportByNoInput extends ReportBy {
	
	@Override
	public default String makeFileName(String reportFileName, Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");
		String asOf = yyyymmdd.format(runDate.getTime());
		String fileName = reportFileName + " as of " + asOf; 
		return fileName.replaceAll(" ", "_");
	}
	
	

}
