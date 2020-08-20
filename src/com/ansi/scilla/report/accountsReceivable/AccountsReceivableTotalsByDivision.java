package com.ansi.scilla.report.accountsReceivable;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDiv;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivision;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public class AccountsReceivableTotalsByDivision extends StandardReport implements ReportByDiv, ReportByDivision {

	private static final long serialVersionUID = 1L;

	public static final  String REPORT_TITLE = "AR Totals Summary By Division";
	public static final String FILENAME = "Accounts Receivable Totals Summary By Division";
	
	private final String sql = "select div, invoice_date as 'invoiced', days_due, client, ticket, site, terms--, total_due\n" + 
//			"--, isnull([current],0.00) as [current], isnull([over30],0.00) as [over30], isnull([over60],0.00) as [over60]\n" + 
			", isnull([over60],0.00) as [over60], isnull([over90],0.00) as [over90], isnull([over120],0.00) as [over120], isnull([over180],0.00) as [over180] \n" + 
			"from \n" + 
			"(select concat( division_nbr, '-', division_code) as div\n" + 
			", invoice_date\n" + 
			", case \n" + 
			"	when invoice_date is null then datediff(d, invoice_date, sysdatetime())\n" + 
			"	else datediff(d, invoice_date, ?)\n" + 
			"	end as days_due\n" + 
			", bill_to.name as 'client'\n" + 
			", ticket.ticket_id as 'ticket'\n" + 
			", job_site.name as 'site'\n" + 
			", job.invoice_terms as 'terms'\n" + 
			", CASE\n" + 
			"	when datediff(d, invoice_date, ?) < 30 then 'current'\n" + 
			"	when datediff(d, invoice_date, ?) < 60 then 'over30'\n" + 
			"	when datediff(d, invoice_date, ?) < 90 then 'over60'\n" + 
			"	when datediff(d, invoice_date, ?) < 120 then 'over90'\n" + 
			"	when datediff(d, invoice_date, ?) < 180 then 'over120'\n" + 
			"	else 'over180' end as 'dueGroup'\n" + 
			", case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, ?) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	else act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	end as total_due\n" + 
			", case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, ?) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	else act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	end as amount_due\n" + 
			"from ticket \n" + 
			"join job on ticket.job_id = job.job_id \n" + 
			"join quote on job.quote_id = quote.quote_id \n" + 
			"join division on division.division_id = act_division_id and act_division_id=? \n" + 
			"join address as bill_to on bill_to.address_id = bill_to_address_id \n" + 
			"join address as job_site on job_site.address_id = quote.job_site_address_id \n" + 
			"left outer join (\n" + 
			"	select ticket_id, \n" + 
			"		sum(ticket_payment.amount) as amount, \n" + 
			"		sum(ticket_payment.tax_amt) as tax_amt \n" + 
			"		from ticket_payment \n" + 
			"		join payment on payment.payment_id = ticket_payment.payment_id\n" + 
			"		where payment_date <= ?\n" + 
			"		group by ticket_id) as ticket_payment_totals \n" + 
			"	on ticket_payment_totals.ticket_id = ticket.ticket_id \n" + 
			"--join (select \n" + 
			"where case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, ?) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	else act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	end <> 0.00\n" + 
//			"--and invoice_date is not null\n" + 
//			"--and invoice_date <= @target_date\n" + 
			") as sourceTable \n" + 
			"PIVOT\n" + 
			"(sum(amount_due)\n" + 
			"for dueGroup in (--[current], [over30], [over60], \n" + 
			"[over60], [over90], [over120], [over180] )\n" + 
			") as pivotTable\n" + 
			"where days_due >= 60\n" + 
			"order by div, invoice_date, days_due, client, ticket, site";
			
	private Division division;
	private List<RowData> data;

	Logger logger = LogManager.getLogger(this.getClass());

	private AccountsReceivableTotalsByDivision(Connection conn, Integer divisionId, Calendar runDate) throws Exception {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);
		this.division = new Division();
		this.division.setDivisionId(divisionId);
		this.division.selectOne(conn);
		this.runDate = runDate;
		this.data = this.makeData(conn, divisionId, runDate);
		makeReport(data);
	}
	
	public Division getDivision() {
		return division;
	}

	private List<RowData> makeData(Connection conn, Integer divisionId, Calendar runDate) throws SQLException {
		List<RowData> data = new ArrayList<RowData>();
		logger.log(Level.DEBUG, sql);
		PreparedStatement ps = conn.prepareStatement(sql);
		java.sql.Date sqlDate = new java.sql.Date(runDate.getTime().getTime());
		ps.setDate(1, sqlDate);
		ps.setDate(2, sqlDate);
		ps.setDate(3, sqlDate);
		ps.setDate(4, sqlDate);
		ps.setDate(5, sqlDate);
		ps.setDate(6, sqlDate);
		ps.setDate(7, sqlDate);
		ps.setDate(8, sqlDate);
		ps.setInt(9, divisionId);
		ps.setDate(10, sqlDate);
		ps.setDate(11, sqlDate);
		ResultSet rs = ps.executeQuery();
		
		this.data = new ArrayList<RowData>();
		RowData newRow;
		while ( rs.next() ) {
			newRow = new RowData(rs);
			data.add(newRow);
		}
		rs.close();
		return data;
	}
	
	
	@SuppressWarnings("unchecked")
	private void makeReport(List<RowData> data) throws NoSuchMethodException, SecurityException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		super.setTitle(REPORT_TITLE);	
		super.setSubtitle("Division: " + this.division.getDivisionDisplay() + " as of " + sdf.format(this.runDate.getTime()));
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("div", "Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("invoiced", "Invoiced", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("daysDue","Days Due", 1, DataFormats.NUMBER_CENTERED, SummaryType.NONE),
				new ColumnHeader("client", "Client", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("ticket", "Ticket", 1, DataFormats.NUMBER_CENTERED, SummaryType.NONE),
				new ColumnHeader("site", "Site", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("terms", "Terms", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),				
				new ColumnHeader("over60", "Over 60", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
				new ColumnHeader("over90", "Over 90", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
				new ColumnHeader("over120", "Over 120", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
				new ColumnHeader("over180", "Over 180", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
		});
		
		List<Object> oData = IterableUtils.toList(CollectionUtils.collect(data, new ObjectTransformer()));
		super.setDataRows(oData);		
		
		super.setColumnWidths(new ColumnWidth[] {
				new ColumnWidth(2000,15.0F), 	// div
				new ColumnWidth(2750,20.0F),	// invoiced
				new ColumnWidth(2000,15.0F),	// daysDue
				new ColumnWidth(6000,45.0F),	// client
				new ColumnWidth(2800,25.0F),	// ticket
				new ColumnWidth(6000,45.0F),	// site
				new ColumnWidth(2000,20.0F),	// terms
				new ColumnWidth(3000,25.8F),	// 60
				new ColumnWidth(3000,25.8F),	// 90
				new ColumnWidth(3000,25.8F),	// 120
				new ColumnWidth(3000,25.8F),	// 180
		});
	}

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	
	public static AccountsReceivableTotalsByDivision buildReport(Connection conn, Integer divisionId) throws Exception {
		return AccountsReceivableTotalsByDivision.buildReport(conn, divisionId, Calendar.getInstance());
	}
	
	public static AccountsReceivableTotalsByDivision buildReport(Connection conn, Integer divisionId, Calendar runDate) throws Exception {
		return new AccountsReceivableTotalsByDivision(conn, divisionId, runDate);
	}
	
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		
		private String div;
		private java.sql.Date invoiced;
		private Integer daysDue;
		private String client;
		private Integer ticket;
		private String site;
		private String terms;
		private Double over60;
		private Double over90;
		private Double over120;
		private Double over180;
		
		public RowData(ResultSet rs) throws SQLException {
			super();
			this.div = rs.getString("div");
			this.invoiced = rs.getDate("invoiced");
			this.daysDue = rs.getInt("days_due");
			this.client = rs.getString("client");
			this.ticket = rs.getInt("ticket");
			this.site = rs.getString("site");
			this.terms = rs.getString("terms");
			this.over60 = rs.getDouble("over60");
			this.over90 = rs.getDouble("over90");
			this.over120 = rs.getDouble("over120");
			this.over180 = rs.getDouble("over180");
		}

		public String getDiv() {
			return div;
		}
		public java.sql.Date getInvoiced() {
			return invoiced;
		}
		public Integer getDaysDue() {
			return daysDue;
		}
		public String getClient() {
			return client;
		}
		public Integer getTicket() {
			return ticket;
		}
		public String getSite() {
			return site;
		}
		public String getTerms() {
			return terms;
		}
		public Double getOver60() {
			return over60;
		}
		public Double getOver90() {
			return over90;
		}
		public Double getOver120() {
			return over120;
		}
		public Double getOver180() {
			return over180;
		}
	}
}
