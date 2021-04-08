package com.ansi.scilla.report.test.parameters;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.ansi.scilla.common.batch.BatchScheduleFactory;
import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.common.parameters.ReportParmStartEnd;

public class TestScheduleFactory {

	
	public void go() throws Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			Calendar runDate = new GregorianCalendar(2020, Calendar.JANUARY, 15);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			for ( BatchScheduleFactory.BatchSchedule schedule : BatchScheduleFactory.BatchSchedule.values() ) {
//				System.out.println(schedule.name());
				ReportParmStartEnd parm = BatchScheduleFactory.makeDates(conn, schedule, runDate);
				System.out.println(schedule.name() + "\t" + sdf.format(parm.getStartDate().getTime()) + "\t" + sdf.format(parm.getEndDate().getTime()));
				
			}
		} finally {
			conn.close();
		}
	}
	
	public static void main(String[] args) {
		try {
			new TestScheduleFactory().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
