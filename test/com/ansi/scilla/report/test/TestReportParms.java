package com.ansi.scilla.report.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.ansi.scilla.report.common.parameters.ReportParmStartEndDate;

public class TestReportParms {

	public static void main(String[] args) {
		Calendar runDate = new GregorianCalendar(2020, Calendar.MARCH, 2);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.S z");
		
		ReportParmStartEndDate parm = new ReportParmStartEndDate(runDate);
		System.out.println(sdf.format(parm.getStartDate().getTime()));
		System.out.println(sdf.format(parm.getEndDate().getTime()));
	}

}
