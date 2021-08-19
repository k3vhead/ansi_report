package com.ansi.scilla.report.test.parameters;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.common.BatchScheduleFactory;
import com.ansi.scilla.report.common.BatchScheduleFactory.BatchSchedule;
import com.ansi.scilla.report.common.parameters.ReportParmStartEnd;
import com.thewebthing.commons.db2.RecordNotFoundException;

public class TestReportParms {
	private void go() throws Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			Calendar startDate = AppUtils.getFiscalYearStart(conn);
			Calendar endDate = (Calendar)startDate.clone();
			endDate.add(Calendar.YEAR, 1);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			
			while ( startDate.before(endDate) ) {
//				ReportParmStartEndMonth month = new ReportParmStartEndMonth(startDate);
//				ReportParmStartEndQuarter quarter = new ReportParmStartEndQuarter(conn, startDate);
//				ReportParmStartEndYear year = new ReportParmStartEndYear(conn, startDate);
//				
//				System.out.println(
//					sdf.format(startDate.getTime()) + "\t" +
//					sdf.format(month.getStartDate().getTime()) + "\t" +
//					sdf.format(month.getEndDate().getTime()) + "\t" +
//					sdf.format(quarter.getStartDate().getTime()) + "\t" +
//					sdf.format(quarter.getEndDate().getTime()) + "\t" +
//					sdf.format(year.getStartDate().getTime()) + "\t" +
//					sdf.format(year.getEndDate().getTime())
//				);
				
				startDate.add(Calendar.DAY_OF_YEAR, 1);
			}
		} finally {
			AppUtils.closeQuiet(conn);
		}
	}
	
	
	public void goQuarter() throws Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			Calendar startDate = AppUtils.getFiscalYearStart(conn);
			Calendar endDate = (Calendar)startDate.clone();
			endDate.add(Calendar.MONTH, 13);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			
			while ( startDate.before(endDate) ) {
//				ReportParmStartEndQuarter quarter = new ReportParmStartEndQuarter(conn, startDate);
//				
//				System.out.println(
//					sdf.format(startDate.getTime()) + "\t" +					
//					sdf.format(quarter.getStartDate().getTime()) + "\t" +
//					sdf.format(quarter.getEndDate().getTime())
//				);
				
				startDate.add(Calendar.DAY_OF_YEAR, 1);
			}
		} finally {
			AppUtils.closeQuiet(conn);
		}
	}
	
	public void goLastWeek() throws RecordNotFoundException, Exception {
		Connection conn = null;
		try {
			conn = AppUtils.getDevConn();
			Calendar runDate = new GregorianCalendar(2021,Calendar.AUGUST, 16);
			ReportParmStartEnd reportParameter = (ReportParmStartEnd)BatchScheduleFactory.makeDates(conn, BatchSchedule.LAST_WEEK, runDate);
			System.out.println(reportParameter);
		} finally {
			conn.close();
		}
	}
	
	public static void main(String[] args) {
		try {
//			new TestReportParms().go();
//			new TestReportParms().goQuarter();
			new TestReportParms().goLastWeek();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}


