package com.ansi.scilla.report.test.parameters;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.ansi.scilla.common.batch.BatchTrigger;
import com.ansi.scilla.common.utils.AppUtils;
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
			testLastWeek(conn);
			
		} finally {
			conn.close();
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
