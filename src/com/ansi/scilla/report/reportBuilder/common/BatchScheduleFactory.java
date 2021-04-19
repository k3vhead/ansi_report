package com.ansi.scilla.report.reportBuilder.common;

import java.sql.Connection;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateUtils;

import com.ansi.scilla.common.exceptions.InvalidSystemStateException;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.common.utils.ApplicationPropertyName;
import com.ansi.scilla.report.common.parameters.ReportParmStartEnd;
import com.thewebthing.commons.db2.RecordNotFoundException;


public class BatchScheduleFactory {
	
	public static ReportParmStartEnd makeDates(Connection conn, BatchSchedule schedule, Calendar runDate) throws RecordNotFoundException, Exception {
		ReportParmStartEnd parm = null;
		switch (schedule) {
		case CALENDAR_YTD:
			parm = makeCalendarYTD(runDate);
			break;
		case FISCAL_YTD:
			parm = makeFiscalYTD(conn, runDate);
			break;
		case LAST_CALENDAR_YEAR:
			parm = makeLastCalendarYear(runDate);
			break;
		case LAST_FISCAL_YEAR:
			parm = makeLastFiscalYear(conn, runDate);
			break;
		case LAST_FISCAL_QUARTER:
			parm = makeLastFiscalQuarter(conn, runDate);
			break;
		case LAST_6_MONTH:
			parm = makeLast(runDate, Calendar.MONTH, 6);
			break;
		case LAST_60_DAY:
			parm = makeLast(runDate, Calendar.DAY_OF_YEAR, 60);
			break;
		case LAST_45_DAY:
			parm = makeLast(runDate, Calendar.DAY_OF_YEAR, 45);
			break;
		case LAST_MONTH:
			parm = makeLastMonth(runDate);
			break;
		case LAST_WEEK:
			parm = makeLastWeek(conn, runDate);
			break;
		default:
			break;
			
		}
		return parm;
	}


	/**
	 * Returns the last &quote;amount&quote; of &quote;units&quote; from runDate
	 * 
	 * @param runDate
	 * @param units   What we're adding, (eg Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_YEAR) to runDate   
	 * @param amount  How much we're adding
	 * @return
	 */
	private static ReportParmStartEnd makeLast(Calendar runDate, int units, int amount) {
		Calendar startDate = (Calendar)runDate.clone();
		startDate.add(units, -1 * amount);
		return new ReportParmStartEnd(startDate, runDate);
	}


	/**
	 * Returns January 1 of rundate year thru run date
	 * eg. If run date is 1/15/2020, returns 1/1/2020 thru 1/15/2020
	 * 
	 * @param runDate
	 * @return
	 */
	private static ReportParmStartEnd makeCalendarYTD(Calendar runDate) {
		GregorianCalendar start = new GregorianCalendar(runDate.get(Calendar.YEAR), Calendar.JANUARY, 1);
		ReportParmStartEnd parm = new ReportParmStartEnd(start, runDate);		
		return parm;
	}


	/**
	 * Returns start of current fiscal year through run date
	 * eg If run date is 1/15/2020 and fiscal year starts 10/1, returns 10/1/2019 through 1/15/2020
	 * 
	 * @param conn
	 * @param runDate
	 * @return
	 * @throws RecordNotFoundException  First day of fiscal year is not defined
	 * @throws Exception
	 */
	private static ReportParmStartEnd makeFiscalYTD(Connection conn, Calendar runDate) throws RecordNotFoundException, Exception {
		Calendar start = AppUtils.getFiscalYearStart(conn);
		return new ReportParmStartEnd(start, runDate);
	}


	/**
	 * January 1 through December 31 of year prior to run date
	 * eg if urn date is 1/15/2020, return 1/1/2019 thru 12/31/2019
	 * @param runDate
	 * @return
	 */
	private static ReportParmStartEnd makeLastCalendarYear(Calendar runDate) {
		Integer year = runDate.get(Calendar.YEAR) - 1;
		Calendar start = new GregorianCalendar(year, Calendar.JANUARY, 1);
		Calendar end = new GregorianCalendar(year, Calendar.DECEMBER, 31);
		return new ReportParmStartEnd(start, end);
	}


	/**
	 * Returns beginning to end of fiscal year prior to run date
	 * eg if run date is 1/15/2020 and fiscal year starts 10/1, returns 10/2/2018 through 9/30/2019
	 * @param conn
	 * @param runDate
	 * @return
	 * @throws RecordNotFoundException Fiscal year start is not defined
	 * @throws Exception
	 */
	private static ReportParmStartEnd makeLastFiscalYear(Connection conn, Calendar runDate) throws RecordNotFoundException, Exception {
		Calendar currentFiscalStart = AppUtils.getFiscalYearStart(conn, runDate);		
		Calendar fiscalStart = (Calendar)currentFiscalStart.clone();
		fiscalStart.add(Calendar.YEAR, -1);
		Calendar fiscalEnd = (Calendar)currentFiscalStart.clone();
		fiscalEnd.add(Calendar.DAY_OF_YEAR, -1);
		return new ReportParmStartEnd(fiscalStart, fiscalEnd);		
	}


	/**
	 * Returns start to end of fiscal quarter prior to run date
	 * eg if run date is 1/15/2020 and fiscal year starts 10/1, return, 10/1/2019 thru 12/31/2019
	 * @param conn
	 * @param runDate
	 * @return
	 * @throws RecordNotFoundException Start of fiscal year is not defined
	 * @throws Exception
	 */
	private static ReportParmStartEnd makeLastFiscalQuarter(Connection conn, Calendar runDate) throws RecordNotFoundException, Exception {
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
		
		ReportParmStartEnd dates = new ReportParmStartEnd(startDate, endDate);
		
		return dates;
	}


	/**
	 * Returns first through last dates of prior calendar month
	 * eg. If run date is 1/15/2020, returns 12/1/2019 thru 12/31/2019
	 * @param runDate
	 * @return
	 */
	private static ReportParmStartEnd makeLastMonth(Calendar runDate) {
		Calendar startDate = (Calendar)runDate.clone();
		startDate.add(Calendar.MONTH, -1);
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		Calendar endDate = (Calendar)runDate.clone();
		endDate.set(Calendar.DAY_OF_MONTH, 1);
		endDate.add(Calendar.DAY_OF_YEAR, -1);
		return new ReportParmStartEnd(startDate, endDate);
	}


	/**
	 * Returns first work day thru last work day of prior week.
	 * eg If run date is Wednesday 1/15/2020 and week is Sat-Fri, returns Saturday 1/4/2020 thru Friday 1/10/2020
	 * @param conn
	 * @param runDate
	 * @return
	 * @throws RecordNotFoundException Work week is not defined
	 * @throws Exception
	 */
	private static ReportParmStartEnd makeLastWeek(Connection conn, Calendar runDate)  throws RecordNotFoundException, Exception {
		String weekStartsOn = ApplicationPropertyName.CALENDAR_WEEK_STARTS_ON.getProperty(conn).getValueString();
		Calendar startDate = AppUtils.getPriorDayOfWeek(runDate, weekStartsOn); //first day of current week
		Calendar endDate = (Calendar)startDate.clone();
		startDate.add(Calendar.DAY_OF_YEAR, -7);
		endDate.add(Calendar.DAY_OF_YEAR, -1);
		return new ReportParmStartEnd(startDate, endDate);
	}


	public enum BatchSchedule {
		LAST_WEEK,
		LAST_MONTH,
		LAST_45_DAY,
		LAST_60_DAY,
		LAST_6_MONTH,
		LAST_FISCAL_QUARTER,
		LAST_FISCAL_YEAR,
		LAST_CALENDAR_YEAR,
		FISCAL_YTD,
		CALENDAR_YTD,
		;

	}
}
