package com.ansi.scilla.report.sixMonthRollingVolume;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.report.reportBuilder.AbstractReport;
import com.ansi.scilla.report.reportBuilder.CompoundReport;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.XLSBuilder;

public class SmrvReport extends CompoundReport {

	private static final long serialVersionUID = 1L;
	
	public static final String REPORT_TITLE = "SMRV Listing Report";
	
	private static Logger logger;

	private SmrvReport(Connection conn, Integer divisionId, Integer month1, Integer year1, Integer month2, Integer year2) throws Exception {
		super(new AbstractReport[] {
				new SmrvDetailReport(conn, divisionId, month1, year1),
				new SmrvDetailReport(conn, divisionId, month2, year2)
		});
	}
	

	public static SmrvReport buildReport(Connection conn, Integer divisionId, Integer month, Integer year) throws Exception {
		Integer month2 = month < 7 ? month + 6 : month+6-12;
		Integer year2 = month < 7 ? year : year + 1;
		return new SmrvReport(conn, divisionId, month, year, month2, year2);
	}
	

	protected SmrvReport(Connection conn, Integer divisionId) throws Exception {
		super(new AbstractReport[] {
				new SmrvDetailReport(conn, divisionId)
		});
	}
	

	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		for ( AbstractReport report : this.reports ) {
			XLSBuilder.build((StandardReport)report, workbook);
		}
	}
	
//	public static SmrvReport buildReport(Connection conn, Integer divisionId, Integer month, Integer year) throws Exception {
//		return new SmrvReport(conn, divisionId, month, year);
//	}

	public static SmrvReport buildReport(Connection conn, Integer divisionId) throws Exception {
		return new SmrvReport(conn, divisionId);
	}


}
