package com.ansi.scilla.report.monthlyServiceTaxReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.DataDumpReport;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;

public class MonthlyServiceTaxReport extends DataDumpReport implements ReportByStartEnd {
	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "Monthly Service Tax";
	
	public static final  String REPORT_TITLE = "Monthly Service Tax Report";

	protected static final String sql = "select division_group.name as company, job_site.state, job_site.county, tax_rate.location, tax_rate.rate, \r\n" + 
			"sum(ticket_payment.tax_amt) as taxes\r\n" + 
			"from ticket\r\n" + 
			"join ticket_payment on ticket.ticket_id = ticket_payment.ticket_id\r\n" + 
			"join payment on payment.payment_id = ticket_payment.payment_id and payment.payment_date >= ? and payment.payment_date <= ?\r\n" + 
			"join division on division.division_id = ticket.act_division_id\r\n" + 
			"join job on job.job_id = ticket.job_id\r\n" + 
			"join quote on quote.quote_id = job.quote_id\r\n" + 
			"join address job_site on job_site.address_id = quote.job_site_address_id\r\n" + 
			"join tax_rate on tax_rate.tax_rate_id = ticket.act_tax_rate_id\r\n" + 
			"join division_group on division_group.group_id = division.group_id and group_type = 'COMPANY'\r\n" + 
			"group by division_group.name, state, county, tax_rate.location, tax_rate.rate\r\n" + 
			"having sum(ticket_payment.tax_amt) <> 0.00";
	
	public MonthlyServiceTaxReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception{
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
	

	public static MonthlyServiceTaxReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new MonthlyServiceTaxReport(conn, startDate, endDate);
	}
	
}
