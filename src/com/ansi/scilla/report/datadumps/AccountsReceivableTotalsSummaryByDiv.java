package com.ansi.scilla.report.datadumps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;
import com.ansi.scilla.report.reportBuilder.reportType.DataDumpReport;

@Deprecated
public class AccountsReceivableTotalsSummaryByDiv extends DataDumpReport implements ReportByNoInput {
	
	private static final long serialVersionUID = 1L;
	
	public static final  String REPORT_TITLE = "AR Totals Summary By Division";
	public static final String FILENAME = "Accounts Receivable Totals Summary By Division";

	protected static final String sql = "select Div\n" + 
			", (select sum(case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	else act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	end) as total_due\n" + 
			"	from ticket \n" + 
			"	join job on ticket.job_id = job.job_id \n" + 
			"	join quote on job.quote_id = quote.quote_id \n" + 
			"	join division on division.division_id = act_division_id \n" + 
			"	join address as bill_to on bill_to.address_id = bill_to_address_id \n" + 
			"	join address as job_site on job_site.address_id = quote.job_site_address_id \n" + 
			"	left outer join (\n" + 
			"		select ticket_id, \n" + 
			"			sum(ticket_payment.amount) as amount, \n" + 
			"			sum(tax_amt) as tax_amt \n" + 
			"			from ticket_payment \n" + 
			"			join payment on payment.payment_id = ticket_payment.payment_id\n" + 
			"			where payment_date <= sysdatetime()\n" + 
			"			group by ticket_id) as ticket_payment_totals \n" + 
			"		on ticket_payment_totals.ticket_id = ticket.ticket_id \n" + 
			"\n" + 
			"	where concat( division_nbr, '-', division_code) = pivotTable.Div \n" + 
			"	) as total_due\n" + 
			", isnull([current],0.00) as [current], isnull([over30],0.00) as [over30], isnull([over60],0.00) as [over60]\n" + 
			", isnull([over90],0.00) as [over90], isnull([over120],0.00) as [over120], isnull([over180],0.00) as [over180] \n" + 
			"from \n" + 
			"(select concat( division_nbr, '-', division_code) as Div\n" + 
			", CASE\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 30 then 'current'\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 60 then 'over30'\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 90 then 'over60'\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 120 then 'over90'\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 180 then 'over120'\n" + 
			"	else 'over180' end as 'dueGroup'\n" + 
			", act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00) as amount_due\n" + 
			"from ticket \n" + 
			"join job on ticket.job_id = job.job_id \n" + 
			"join quote on job.quote_id = quote.quote_id \n" + 
			"join division on division.division_id = act_division_id \n" + 
			"join address as bill_to on bill_to.address_id = bill_to_address_id \n" + 
			"join address as job_site on job_site.address_id = quote.job_site_address_id \n" + 
			"	left outer join (\n" + 
			"		select ticket_id, \n" + 
			"			sum(ticket_payment.amount) as amount, \n" + 
			"			sum(tax_amt) as tax_amt \n" + 
			"			from ticket_payment \n" + 
			"			join payment on payment.payment_id = ticket_payment.payment_id\n" + 
			"			where payment_date <= sysdatetime()\n" + 
			"			group by ticket_id) as ticket_payment_totals \n" + 
			"		on ticket_payment_totals.ticket_id = ticket.ticket_id \n" + 
			"where  \n" +
			"  division.division_id = ? and " +
			"  case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, sysdatetime()) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	else act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00) end <> 0.00\n" + 
			") as sourceTable \n" + 
			"PIVOT\n" + 
			"(sum(amount_due)\n" + 
			"for dueGroup in ([current], [over30], [over60], [over90], [over120], [over180] )\n" + 
			") as pivotTable\n" + 
			"order by Div"
			;


	

	public AccountsReceivableTotalsSummaryByDiv(Connection conn, Integer divisionId) throws Exception{
		super();
		super.sql = sql;
		super.setTitle(REPORT_TITLE);
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, divisionId);
		ResultSet rs = ps.executeQuery();
		makeReport(rs);
	}
	

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	

	public static AccountsReceivableTotalsSummaryByDiv buildReport(Connection conn, Integer divisionId) throws Exception {
		return new AccountsReceivableTotalsSummaryByDiv(conn, divisionId);
	}
	
	
}
