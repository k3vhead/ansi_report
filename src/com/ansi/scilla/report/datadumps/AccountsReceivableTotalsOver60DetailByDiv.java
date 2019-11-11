package com.ansi.scilla.report.datadumps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ansi.scilla.report.reportBuilder.DataDumpReport;

public class AccountsReceivableTotalsOver60DetailByDiv extends DataDumpReport {
	
	private static final long serialVersionUID = 1L;
	
	public static final  String REPORT_TITLE = "AR Totals Over 60 Detail By Division";

	protected static final String sql = "select Div, invoice_date as 'Invoiced', DaysDue, Client, Ticket, Site\n" + 
			", isnull([over60],0.00) as [over60], isnull([over90],0.00) as [over90], isnull([over120],0.00) as [over120], isnull([over180],0.00) as [over180] \n" + 
			"from \n" + 
			"(select concat( division_nbr, '-', division_code) as Div\n" + 
			", invoice_date\n" + 
			", case \n" + 
			"	when invoice_date is null then datediff(d, invoice_date, sysdatetime())\n" + 
			"	else datediff(d, invoice_date, sysdatetime())\n" + 
			"	end as DaysDue\n" + 
			", bill_to.name as 'Client'\n" + 
			", ticket.ticket_id as 'Ticket'\n" + 
			", job_site.name as 'Site'\n" + 
			", CASE\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 30 then 'current'\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 60 then 'over30'\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 90 then 'over60'\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 120 then 'over90'\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 180 then 'over120'\n" + 
			"	else 'over180' end as 'dueGroup'\n" + 
			", case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	else act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	end as total_due\n" + 
			", case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	else act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	end as amount_due\n" + 
			"from ticket \n" + 
			"join job on ticket.job_id = job.job_id \n" + 
			"join quote on job.quote_id = quote.quote_id \n" + 
			"join division on division.division_id = act_division_id \n" + 
			"join address as bill_to on bill_to.address_id = bill_to_address_id \n" + 
			"join address as job_site on job_site.address_id = quote.job_site_address_id \n" + 
			"left outer join (\n" + 
			"	select ticket_id, \n" + 
			"		sum(ticket_payment.amount) as amount, \n" + 
			"		sum(ticket_payment.tax_amt) as tax_amt \n" + 
			"		from ticket_payment \n" + 
			"		join payment on payment.payment_id = ticket_payment.payment_id\n" + 
			"		where payment_date <= sysdatetime()\n" + 
			"		group by ticket_id) as ticket_payment_totals \n" + 
			"	on ticket_payment_totals.ticket_id = ticket.ticket_id \n" + 
			"where case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	else act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	end <> 0.00\n" + 
			" and division.division_id = ? " +
			") as sourceTable \n" + 
			"PIVOT\n" + 
			"(sum(amount_due)\n" + 
			"for dueGroup in ([over60], [over90], [over120], [over180] )\n" + 
			") as pivotTable\n" + 
			"where DaysDue >= 60\n" + 
			"order by Div, invoice_date, DaysDue, Client, Ticket, Site\n"
			;

	

	public AccountsReceivableTotalsOver60DetailByDiv(Connection conn, Integer divisionId) throws Exception{
		super();
		super.sql = sql;
		super.setTitle(REPORT_TITLE);
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, divisionId);
		ResultSet rs = ps.executeQuery();
		makeReport(rs);
	}
	

	

	public static AccountsReceivableTotalsOver60DetailByDiv buildReport(Connection conn, Integer divisionId) throws Exception {
		return new AccountsReceivableTotalsOver60DetailByDiv(conn, divisionId);
	}
	
	
}