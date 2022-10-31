package com.ansi.scilla.report.invoiceRegisterReport;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;

public class InvoiceRegisterDivisionSummary extends AbstractInvoiceRegisterSummary implements ReportByStartEnd {

	public static final String FILENAME = "IR_Division_Summary";
	
	
	private static final long serialVersionUID = 1L;

	private final String DIVISION_SUMMARY_SQL = 
			"select \n" + 
			"     concat(division.division_nbr, '-', division.division_code) as name \n" + 
			"    , count(*) as ticket_count\n" + 
			"    , sum(ticket.act_price_per_cleaning) as invoiced_amount \n" + 
			"from ticket \n" + 
			"join division \n" + 
			"  on division.division_id = ticket.act_division_id\n" + 
			"where ticket.invoice_date >= ? and invoice_date < ?\n" + 
			"group by division.division_nbr, division.division_code  \n" + 
			"order by division.division_nbr, division.division_code;\n" ;
	

	
	public static final String REPORT_TITLE = "Division";
	
	
	
	public InvoiceRegisterDivisionSummary() {
		super(REPORT_TITLE);
	}

	protected InvoiceRegisterDivisionSummary(Connection conn) throws Exception {
		super(conn, REPORT_TITLE);


	}

	protected InvoiceRegisterDivisionSummary(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super(conn, startDate, endDate, REPORT_TITLE);

	}
	


	
	public static InvoiceRegisterDivisionSummary buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new InvoiceRegisterDivisionSummary(conn, startDate, endDate);
	}
	
	public static InvoiceRegisterDivisionSummary buildReport(Connection conn) throws Exception {
		return new InvoiceRegisterDivisionSummary(conn);
	}

	@Override
	protected String getFile() {
		// TODO Auto-generated method stub
		return FILENAME;
	}

	@Override
	protected String getSql() {
		// TODO Auto-generated method stub
		return DIVISION_SUMMARY_SQL;
	}

}