package com.ansi.scilla.report.reportBuilder.reportBy;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;

public interface ReportByDivMonthYear extends ReportBy {
	@Override
	default String makeFileName(String reportFileName, Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyy-MM");
		String div = division.getDivisionDisplay();
		String asOf = yyyymmdd.format(runDate.getTime());
		String monthYear = yyyyMM.format(startDate.getTime());
		String fileName = reportFileName + " for Div " + div + " for " + monthYear + " as of " + asOf; 
		return fileName.replaceAll(" ", "_");
	}
	
	

}
