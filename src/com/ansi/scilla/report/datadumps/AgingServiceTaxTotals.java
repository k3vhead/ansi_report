package com.ansi.scilla.report.datadumps;

import java.sql.Connection;
import java.util.Calendar;

import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.DataDumpReport;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;

public class AgingServiceTaxTotals extends DataDumpReport implements ReportByNoInput {
	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "Aging Service Tax Totals";
	
	
	public AgingServiceTaxTotals(Connection conn) throws Exception{
		super();
		super.sql = sql;
		super.setTitle(REPORT_TITLE);
		makeReport(conn);
	}
	
	public static final  String REPORT_TITLE = "Aging Service Tax Totals";

	protected static final String sql = "select year, month, \n" + 
			"	isnull([0-AA00],0.00) as '00-AA00',   \n" + 
			"	isnull([12-IL02],0.00) as '12-IL02',   \n" + 
			"	isnull([15-IL05],0.00) as '15-IL05',   \n" + 
			"	isnull([18-IL08],0.00) as '18-IL08',   \n" + 
			"	isnull([19-IL09],0.00) as '19-IL09',   \n" + 
			"	isnull([23-CH03],0.00) as '23-CH03',   \n" + 
			"	isnull([31-IN01],0.00) as '31-IN01',   \n" + 
			"	isnull([32-IN02],0.00) as '32-IN02',   \n" + 
			"	isnull([33-IN03],0.00) as '33-IN03',   \n" + 
			"	isnull([44-MO04],0.00) as '44-MO04',   \n" + 
			"	isnull([65-OH05],0.00) as '65-OH05',   \n" + 
			"	isnull([66-OH06],0.00) as '66-OH06',   \n" + 
			"	isnull([67-OH07],0.00) as '67-OH07', \n" + 
			"	isnull([71-PA01],0.00) as '71-PA01', \n" + 
			"	isnull([77-CL07],0.00) as '77-CL07', \n" + 
			"	isnull([78-CL08],0.00) as '78-CL08', \n" + 
			"	isnull([81-TN01],0.00) as '81-TN01', \n" + 
			"	isnull([89-TN09],0.00) as '89-TN09' \n" + 
			"from \n" + 
			"(select ticket_payment.tax_amt, concat(division_nbr,'-',division_code) as Div, datepart(yy, payment_date) as year, datepart(MM, payment_date) as month\n" + 
			"from ticket_payment\n" + 
			"join ticket on ticket.ticket_id = ticket_payment.ticket_id\n" + 
			"join payment on payment.payment_id = ticket_payment.payment_id\n" + 
			"join division on ticket.act_division_id = division.division_id and ticket_status in ('i','p')) as sourceTable \n" + 
			"PIVOT\n" + 
			"(sum(tax_amt)\n" + 
			"for Div in ([0-AA00], [12-IL02], [15-IL05], [18-IL08], [19-IL09], [23-CH03], [31-IN01], [32-IN02], [33-IN03], [44-MO04],   \n" + 
			"			[65-OH05], [66-OH06], [67-OH07], [71-PA01], [77-CL07], [78-CL08], [81-TN01], [89-TN09])\n" + 
			") as pivotTable\n" + 
			"where year>= 2015\n" + 
			"order by year, month";

	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	public static AgingServiceTaxTotals buildReport(Connection conn) throws Exception {
		return new AgingServiceTaxTotals(conn);
	}
	
}
