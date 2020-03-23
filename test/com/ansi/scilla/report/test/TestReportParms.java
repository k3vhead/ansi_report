package com.ansi.scilla.report.test;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.common.parameters.ReportParmStartEndMonth;
import com.ansi.scilla.report.common.parameters.ReportParmStartEndQuarter;
import com.ansi.scilla.report.common.parameters.ReportParmStartEndYear;

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
				ReportParmStartEndMonth month = new ReportParmStartEndMonth(startDate);
				ReportParmStartEndQuarter quarter = new ReportParmStartEndQuarter(conn, startDate);
				ReportParmStartEndYear year = new ReportParmStartEndYear(conn, startDate);
				
				System.out.println(
					sdf.format(startDate.getTime()) + "\t" +
					sdf.format(month.getStartDate().getTime()) + "\t" +
					sdf.format(month.getEndDate().getTime()) + "\t" +
					sdf.format(quarter.getStartDate().getTime()) + "\t" +
					sdf.format(quarter.getEndDate().getTime()) + "\t" +
					sdf.format(year.getStartDate().getTime()) + "\t" +
					sdf.format(year.getEndDate().getTime())
				);
				
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
				ReportParmStartEndQuarter quarter = new ReportParmStartEndQuarter(conn, startDate);
				
				System.out.println(
					sdf.format(startDate.getTime()) + "\t" +					
					sdf.format(quarter.getStartDate().getTime()) + "\t" +
					sdf.format(quarter.getEndDate().getTime())
				);
				
				startDate.add(Calendar.DAY_OF_YEAR, 1);
			}
		} finally {
			AppUtils.closeQuiet(conn);
		}
	}
	
	
	public static void main(String[] args) {
		try {
//			new TestReportParms().go();
			new TestReportParms().goQuarter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}


