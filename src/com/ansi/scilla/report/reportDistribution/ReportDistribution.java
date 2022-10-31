package com.ansi.scilla.report.reportDistribution;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.CompoundReport;

/**
 * 
 * A compound report consisting a detail by report and a detail by subscriber
 *
 */
public class ReportDistribution extends CompoundReport implements ReportByNoInput {

	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "reportDistribution";
	
	public ReportDistribution(Connection conn) throws Exception {
		super(new AbstractReport[] {
			new ReportDistributionByReport(conn),
			new ReportDistributionBySubscriber(conn),
		});
	}

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	public static ReportDistribution buildReport(Connection conn) throws Exception {		
		return new ReportDistribution(conn);
	}
}
