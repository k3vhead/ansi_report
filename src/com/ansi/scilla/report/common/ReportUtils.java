package com.ansi.scilla.report.common;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.exceptions.InvalidSystemStateException;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.common.parameters.ReportParameter;
import com.ansi.scilla.report.common.parameters.ReportParmDiv;
import com.ansi.scilla.report.common.parameters.ReportParmDivEndDate;
import com.ansi.scilla.report.common.parameters.ReportParmDivMonthYear;
import com.ansi.scilla.report.common.parameters.ReportParmStartEndMonthDiv;
import com.ansi.scilla.report.common.parameters.ReportParmStartEnd;
import com.thewebthing.commons.db2.RecordNotFoundException;

public class ReportUtils {

	public static StartEndDate getQuarter(Connection conn, Calendar runDate) throws Exception {
		Calendar q1 = AppUtils.getFiscalYearStart(conn, runDate);		
		Calendar q2 = (Calendar)q1.clone();
		q2.add(Calendar.MONTH, 3);
		Calendar q3 = (Calendar)q1.clone();
		q3.add(Calendar.MONTH, 6);
		Calendar q4 = (Calendar)q1.clone();
		q4.add(Calendar.MONTH, 9);
		
		
		Calendar startDate = null;
		if ( runDate.after(q4) || DateUtils.isSameDay(runDate, q4) ) {
			startDate = (Calendar)q3.clone();
		} else if ( runDate.after(q3) || DateUtils.isSameDay(runDate, q3)) {
			startDate = (Calendar)q2.clone();
		} else if ( runDate.after(q2) || DateUtils.isSameDay(runDate, q2) ) {
			startDate = (Calendar)q1.clone();
		} else if ( runDate.after(q1) || DateUtils.isSameDay(runDate, q1) ) {
			startDate = (Calendar)q4.clone();
			startDate.add(Calendar.YEAR, -1);
		} else {
			throw new InvalidSystemStateException("This Shouldn't happen");
		}
		
		Calendar endDate = (Calendar)startDate.clone();
		endDate.add(Calendar.MONTH, 3);
		endDate.add(Calendar.DAY_OF_YEAR, -1);
		
		StartEndDate dates = new StartEndDate(startDate, endDate);
		
		return dates;
	}
	
	
	public static StartEndDate getFiscalYear(Connection conn, Calendar runDate) throws RecordNotFoundException, Exception {
		Calendar fiscalStart = AppUtils.getFiscalYearStart(conn, runDate);
		Calendar fiscalEnd = (Calendar)fiscalStart.clone();
		fiscalEnd.add(Calendar.YEAR, 1);
		fiscalEnd.add(Calendar.DAY_OF_YEAR, -1);
		return new StartEndDate(fiscalStart, fiscalEnd);
	}
	
	
	
	public static String makeReportFileName(Connection conn, Calendar runDate, ReportInputType reportInputType, String downloadFileName, ReportParameter parameters) {
		Logger logger = LogManager.getLogger(ReportUtils.class);
		SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");
		DecimalFormat nn = new DecimalFormat("00");
		
		String asOf = yyyymmdd.format(runDate.getTime());
	
		List<String> names = new ArrayList<String>();
		names.add(downloadFileName);	
		
		if ( reportInputType.equals(ReportInputType.reportNoInput)) {
			names.add("as of " + asOf);
		} else if ( reportInputType.equals(ReportInputType.reportByDiv)) {
			ReportParmDiv reportParm = (ReportParmDiv)parameters;
			String div = reportParm.getDivision().getDivisionDisplay();
			names.add("for Div " + div);
			names.add("as of " + asOf);
		} else if ( reportInputType.equals(ReportInputType.reportByStartEnd)) {
			ReportParmStartEnd reportParm = (ReportParmStartEnd)parameters;
			String startDate = yyyymmdd.format(reportParm.getStartDate().getTime());
			String endDate = yyyymmdd.format(reportParm.getEndDate().getTime());
			names.add("for " + startDate);
			names.add("to " + endDate);
			names.add("as of " + asOf);
		} else if ( reportInputType.equals(ReportInputType.reportByDivEnd)) {
			ReportParmDivEndDate reportParm = (ReportParmDivEndDate)parameters;
			String div = reportParm.getDivision().getDivisionDisplay();
			String endDate = yyyymmdd.format(reportParm.getEndDate().getTime());
			names.add("for Div " + div);
			names.add("to " + endDate);
			names.add("as of " + asOf);
		} else if ( reportInputType.equals(ReportInputType.reportByDivMonthYear)) {
			ReportParmDivMonthYear reportParm = (ReportParmDivMonthYear)parameters;
			String div = reportParm.getDivision().getDivisionDisplay();
			String monthYear = reportParm.getYear() + "-" + nn.format(reportParm.getMonth());
			names.add("for Div " + div);
			names.add("for " + monthYear);
			names.add("as of " + asOf);
		} else if ( reportInputType.equals(ReportInputType.reportByDivStartEnd)) {
			ReportParmStartEndMonthDiv reportParm = (ReportParmStartEndMonthDiv)parameters;
			String div = reportParm.getDivision().getDivisionDisplay();
			String startDate = yyyymmdd.format(reportParm.getStartDate().getTime());
			String endDate = yyyymmdd.format(reportParm.getEndDate().getTime());
			names.add("for Div " + div);
			names.add("for " + startDate);
			names.add("to " + endDate);
			names.add("as of " + asOf);
		} else {
			logger.log(Level.DEBUG, "No report input type match: " + reportInputType);
		}
		
		String reportFileName = StringUtils.join(names, " ");
		logger.log(Level.DEBUG, "Filename: " + reportFileName);
		// the attachment header sees "end of name" when it finds a space
		return reportFileName.replaceAll(" ", "_");

	}
}
