package com.ansi.scilla.report.monthlyServiceTaxReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.DataDumpReport;

public class MonthlyServiceTaxByDayReport extends DataDumpReport implements ReportByStartEnd {
	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "Monthly Service Tax By Day";
	
	public static final  String REPORT_TITLE = "Monthly Service Tax By Day Report";

	protected static final String sql = "select payment_date, isnull([65],0.00) as [65], isnull([66],0.00) as [66], \r\n" +
			"isnull([67],0.00) as [67], isnull([71],0.00) as [71], isnull([77],0.00) as [77], isnull([81],0.00) as [81]\r\n" + 
			"from \r\n" + 
			"(select tax_amt, division_nbr, payment_date\r\n" + 
			"from ticket_payment\r\n" + 
			"join ticket on ticket.ticket_id = ticket_payment.ticket_id\r\n" + 
			"join payment on payment.payment_id = ticket_payment.payment_id\r\n" + 
			"join division on ticket.act_division_id = division.division_id) as sourceTable\r\n" + 
			"PIVOT\r\n" + 
			"(sum(tax_amt)\r\n" + 
			"for division_nbr in ([65], [66], [67], [71], [77], [81])\r\n" + 
			") as pivotTable\r\n" + 
			"where payment_date >= ? and payment_date <= ?\r\n" + 
			"order by payment_date";
	
	public MonthlyServiceTaxByDayReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception{
		super();
		super.sql = sql;
		super.setTitle(REPORT_TITLE);
		PreparedStatement ps = conn.prepareStatement(sql);
		java.sql.Date start = new java.sql.Date(startDate.getTimeInMillis());
		java.sql.Date end = new java.sql.Date(endDate.getTimeInMillis());
		ps.setDate(1, start);
		ps.setDate(2, end);
		ResultSet rs = ps.executeQuery();
		makeReport(rs);
	}
	

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	public static MonthlyServiceTaxByDayReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new MonthlyServiceTaxByDayReport(conn, startDate, endDate);
	}
	
}
