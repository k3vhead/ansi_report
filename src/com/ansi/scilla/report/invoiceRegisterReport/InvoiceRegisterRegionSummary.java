package com.ansi.scilla.report.invoiceRegisterReport;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;

public class InvoiceRegisterRegionSummary extends AbstractInvoiceRegisterSummary implements ReportByStartEnd {

	public static final String FILENAME = "IR_Division_Summary";
	
	
	private static final long serialVersionUID = 1L;

	private final String DIVISION_SUMMARY_SQL = 
			"select \n" + 
			"    division_group.name as name\n" + 
			"    , isnull(ticket_summary.ticket_count, 0) as ticket_count\n" + 
			"    , isnull(ticket_summary.invoiced_amount, 0.0) as invoiced_amount\n" + 
			"from division_group\n" + 
			"left outer join (select \n" + 
			"    region.name as region_name\n" + 
			"    , count(*) as ticket_count\n" + 
			"    , sum(ticket.act_price_per_cleaning) as invoiced_amount\n" + 
			"    from division_group as region \n" + 
			"    join division_group as company on company.parent_id = region.group_id \n" + 
			"    join division on division.group_id = company.group_id \n" + 
			"    inner join ticket on ticket.act_division_id=division.division_id and ticket.invoice_date>= ? and ticket.invoice_date< ? \n" + 
			"    group by region.name) as ticket_summary\n" + 
			"    on ticket_summary.region_name=division_group.name\n" + 
			"where division_group.group_type='REGION'\n" + 
			"order by division_group.name";
	

	
	public static final String REPORT_TITLE = "Region";
	
	
	
	public InvoiceRegisterRegionSummary() {
		super(REPORT_TITLE);
	}

	protected InvoiceRegisterRegionSummary(Connection conn) throws Exception {
		super(conn, REPORT_TITLE);


	}

	protected InvoiceRegisterRegionSummary(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super(conn, startDate, endDate, REPORT_TITLE);

	}
	


	
	public static InvoiceRegisterRegionSummary buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new InvoiceRegisterRegionSummary(conn, startDate, endDate);
	}
	
	public static InvoiceRegisterRegionSummary buildReport(Connection conn) throws Exception {
		return new InvoiceRegisterRegionSummary(conn);
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