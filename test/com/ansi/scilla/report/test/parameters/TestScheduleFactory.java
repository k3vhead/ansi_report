package com.ansi.scilla.report.test.parameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateUtils;

import com.ansi.scilla.common.batch.BatchTrigger;
import com.ansi.scilla.common.calendar.CalendarDateType;
import com.ansi.scilla.common.db.ApplicationProperties;
import com.ansi.scilla.common.utils.AnsiDateUtils;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.common.utils.ApplicationPropertyName;
import com.ansi.scilla.report.common.BatchScheduleFactory;
import com.ansi.scilla.report.common.BatchScheduleFactory.BatchSchedule;
import com.ansi.scilla.report.common.parameters.ReportParmStartEnd;
import com.thewebthing.commons.db2.RecordNotFoundException;

public class TestScheduleFactory {
	private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	private final Calendar runDate = new GregorianCalendar(2022, Calendar.OCTOBER, 3);
	
	public void go() throws Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
//			testSchedules(conn);
//			testLastWeek(conn);
			testWorkday(conn);
			
		} finally {
			conn.close();
		}
	}
	
	
	private void testWorkday(Connection conn) throws RecordNotFoundException, Exception {
		Calendar checkDate = new GregorianCalendar(2022, Calendar.SEPTEMBER, 1);
		Calendar lastDate = new GregorianCalendar(2023, Calendar.JANUARY, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Integer triggerValue = Integer.valueOf(1);
		
		while ( checkDate.before(lastDate)) {
//			System.out.println( sdf.format(checkDate.getTime()) + ":");

			ApplicationProperties firstReportDay = ApplicationPropertyName.CALENDAR_REPORTING_WEEK_STARTS_ON.getProperty(conn);
			Calendar firstReportDate = AppUtils.getPriorDayOfWeek(checkDate, firstReportDay.getValueString());						

			PreparedStatement ps = conn.prepareStatement("select ansi_date, date_type \n" + 
					"from ansi_calendar\n" + 
					"where ansi_date >= ? and ansi_date<=?\n" + 
					"order by ansi_date asc");
			ps.setDate(1, new java.sql.Date(firstReportDate.getTime().getTime()));
			ps.setDate(2, new java.sql.Date(checkDate.getTime().getTime()));
			ResultSet rs = ps.executeQuery();
			int dateAdjustment = 0;  // how many non-work days between start of report week and check day 
			while ( rs.next() ) {
				CalendarDateType dateType = CalendarDateType.valueOf(rs.getString("date_type"));
//				System.out.println("** " + sdf.format(rs.getDate("ansi_date")) + "\t" + dateType.name());
				if ( ! dateType.isWorkDay() ) {
					dateAdjustment++;
				}
			}
			rs.close();
			Calendar nthDayOfReportingWeek = (Calendar)firstReportDate.clone();
			nthDayOfReportingWeek.add(Calendar.DAY_OF_YEAR, dateAdjustment + triggerValue.intValue());
//			System.out.println( "firstReportDate\t" + sdf.format(firstReportDate.getTime()));	
//			System.out.println( "nthDayOfReportingWeek\t" + sdf.format(nthDayOfReportingWeek.getTime()));			

//			System.out.println("\n");
			boolean thisIsTheDay = DateUtils.isSameDay(checkDate, nthDayOfReportingWeek);
			System.out.println( sdf.format(checkDate.getTime()) + "\t" + thisIsTheDay);
			checkDate.add(Calendar.DAY_OF_YEAR, 1);
		}
	}

	private void testLastWeek(Connection conn) throws RecordNotFoundException, Exception {		
//		ReportParmStartEnd parm = BatchScheduleFactory.makeDates(conn, BatchSchedule.LAST_WEEK, runDate);
//		System.out.println("*******");
//		System.out.println(parm);
		
		Calendar runDate = new GregorianCalendar(2022, Calendar.SEPTEMBER, 1);
		Calendar endDate = new GregorianCalendar(2023, Calendar.JANUARY, 1);
		while ( runDate.before(endDate)) {
			boolean isMonday = BatchTrigger.MONDAY.isNow(conn, runDate);
			if ( isMonday ) {
				ReportParmStartEnd parm = BatchScheduleFactory.makeDates(conn, BatchSchedule.LAST_WEEK, runDate);
				System.out.println(sdf.format(runDate.getTime()) + "\t" + sdf.format(parm.getStartDate().getTime()) + "\t" + sdf.format(parm.getEndDate().getTime()));
			} else {
				System.out.println(sdf.format(runDate.getTime()));
			}
			runDate.add(Calendar.DAY_OF_YEAR, 1);
		}
	}


	private void testSchedules(Connection conn) throws Exception {
		for ( BatchScheduleFactory.BatchSchedule schedule : BatchScheduleFactory.BatchSchedule.values() ) {
//			System.out.println(schedule.name());
			ReportParmStartEnd parm = BatchScheduleFactory.makeDates(conn, schedule, runDate);
			System.out.println(schedule.name() + "\t" + sdf.format(parm.getStartDate().getTime()) + "\t" + sdf.format(parm.getEndDate().getTime()));
			
		}

	}
	
	private void testThisMonth() throws Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			Calendar runDate = new GregorianCalendar(2021, Calendar.JANUARY, 1);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			Calendar lastDate = new GregorianCalendar(2022, Calendar.DECEMBER, 31);
			while ( runDate.before(lastDate) ) {
				ReportParmStartEnd parm = BatchScheduleFactory.makeDates(conn, BatchScheduleFactory.BatchSchedule.THIS_MONTH, runDate);
				System.out.println(sdf.format(runDate.getTime()) + "\t" + sdf.format(parm.getStartDate().getTime()) + "\t" + sdf.format(parm.getEndDate().getTime()));
				runDate.add(Calendar.DAY_OF_YEAR, 30);
			}
		} finally {
			conn.close();
		}		
	}


	public static void main(String[] args) {
		try {
			new TestScheduleFactory().go();
//			new TestScheduleFactory().testThisMonth();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
