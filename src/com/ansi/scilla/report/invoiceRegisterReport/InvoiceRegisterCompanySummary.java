package com.ansi.scilla.report.invoiceRegisterReport;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;

public class InvoiceRegisterCompanySummary extends AbstractInvoiceRegisterSummary implements ReportByStartEnd {

	public static final String FILENAME = "IR_Division_Summary";
	
	
	private static final long serialVersionUID = 1L;

	private final String DIVISION_SUMMARY_SQL = 
			"select division_group.name as name\n" + 
			"    , isnull(sum(ticket_summary.ticket_count),0) as ticket_count\n" + 
			"    , isnull(sum(ticket_summary.invoiced_amount), 0.0) as invoiced_amount\n" + 
			"from division_group \n" + 
			"inner join division on division.group_id = division_group.group_id \n" + 
			"left outer join (\n" + 
			"    select division.division_id,\n" + 
			"        count(*) as ticket_count,\n" + 
			"        sum(ticket.act_price_per_cleaning) as invoiced_amount\n" + 
			"    from division\n" + 
			"    inner join ticket on ticket.act_division_id=division.division_id and ticket.invoice_date>= ? and ticket.invoice_date< ? \n" + 
			"    group by division.division_id\n" + 
			"    ) as ticket_summary on ticket_summary.division_id=division.division_id\n" + 
			"where division_group.group_type='COMPANY'\n" + 
			"group by division_group.name\n" + 
			"order by division_group.name";
	

	
	public static final String REPORT_TITLE = "Company";
	
	
	
	public InvoiceRegisterCompanySummary() {
		super(REPORT_TITLE);
	}

	protected InvoiceRegisterCompanySummary(Connection conn) throws Exception {
		super(conn, REPORT_TITLE);


	}

	protected InvoiceRegisterCompanySummary(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super(conn, startDate, endDate, REPORT_TITLE);

	}
	


	
	public static InvoiceRegisterCompanySummary buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new InvoiceRegisterCompanySummary(conn, startDate, endDate);
	}
	
	public static InvoiceRegisterCompanySummary buildReport(Connection conn) throws Exception {
		return new InvoiceRegisterCompanySummary(conn);
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