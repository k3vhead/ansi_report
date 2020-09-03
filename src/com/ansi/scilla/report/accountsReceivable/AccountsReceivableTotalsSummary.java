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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.CustomCell;
import com.ansi.scilla.report.reportBuilder.common.CustomCellFormat;
import com.ansi.scilla.report.reportBuilder.common.CustomCellFormat.CustomCellAlignment;
import com.ansi.scilla.report.reportBuilder.common.CustomCellFormat.CustomCellColor;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByNoInput;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public class AccountsReceivableTotalsSummary extends StandardReport implements ReportByNoInput {
	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "Accounts Receivable Totals Summary";
	public static final String REPORT_TITLE = "AR Totals Summary";
	
	
	protected final String sql = "select div\n" + 
			", (select sum(case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, ?) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
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
			"			where payment_date <= ?\n" + 
			"			group by ticket_id) as ticket_payment_totals \n" + 
			"		on ticket_payment_totals.ticket_id = ticket.ticket_id \n" + 
			"\n" + 
			"	where concat( division_nbr, '-', division_code) = pivotTable.div \n" + 
			"	) as total_due\n" + 
			", isnull([current],0.00) as [current], isnull([over30],0.00) as [over30], isnull([over60],0.00) as [over60]\n" + 
			", isnull([over90],0.00) as [over90], isnull([over120],0.00) as [over120], isnull([over180],0.00) as [over180] \n" + 
			"from \n" + 
			"(select concat( division_nbr, '-', division_code) as div\n" + 
			", CASE\n" + 
			"	when datediff(d, invoice_date, ?) < 30 then 'current'\n" + 
			"	when datediff(d, invoice_date, ?) < 60 then 'over30'\n" + 
			"	when datediff(d, invoice_date, ?) < 90 then 'over60'\n" + 
			"	when datediff(d, invoice_date, ?) < 120 then 'over90'\n" + 
			"	when datediff(d, invoice_date, ?) < 180 then 'over120'\n" + 
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
			"			where payment_date <= ?\n" + 
			"			group by ticket_id) as ticket_payment_totals \n" + 
			"		on ticket_payment_totals.ticket_id = ticket.ticket_id \n" + 
			"where  \n" + 
			"  case \n" + 
			"	when invoice_date is null then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	when datediff(d, invoice_date, ?) < 0 then '0.00' - isnull(ticket_payment_totals.amount,0.00)\n" + 
			"	else act_price_per_cleaning - isnull(ticket_payment_totals.amount,0.00) end <> 0.00\n" + 
			") as sourceTable \n" + 
			"PIVOT\n" + 
			"(sum(amount_due)\n" + 
			"for dueGroup in ([current], [over30], [over60], [over90], [over120], [over180] )\n" + 
			") as pivotTable\n" + 
			"order by div"
			;

	private List<RowData> data;

	Logger logger = LogManager.getLogger(this.getClass());

	protected AccountsReceivableTotalsSummary(Connection conn, Calendar runDate) throws Exception {
		super();
		this.runDate = runDate;
		this.setTitle(REPORT_TITLE);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		this.setSubtitle("As of " + sdf.format(this.runDate.getTime()));
		super.setReportOrientation(ReportOrientation.PORTRAIT);
		this.data = this.makeData(conn);
		makeReport(data);
	}
	
	
	private List<RowData> makeData(Connection conn) throws SQLException {
		List<RowData> data = new ArrayList<RowData>();
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
		ps.setDate(9, sqlDate);
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
	private void makeReport(List<RowData> data) throws NoSuchMethodException, SecurityException, CloneNotSupportedException {
		super.setTitle(REPORT_TITLE);	
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("div", "Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("totalDue", "Total Due", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),
				new ColumnHeader("current","Current", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),
				new ColumnHeader("over30", "Over 30", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),
				new ColumnHeader("over60", "Over 60", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),
				new ColumnHeader("over90", "Over 90", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),
				new ColumnHeader("over120", "Over 120", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),
				new ColumnHeader("over180", "Over 180", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);

		
		Double totalTotalDue = 0.0D;
		Double totalCurrent = 0.0D;
		Double total30 = 0.0D;
		Double total60 = 0.0D;
		Double total90 = 0.0D;
		Double total120 = 0.0D;
		Double total180 = 0.0D;
		
		for ( RowData rowData : data ) {
			totalTotalDue = totalTotalDue + rowData.getTotalDue();
			totalCurrent = totalCurrent + rowData.getCurrent();
			total30 = total30 + rowData.getOver30();
			total60 = total60 + rowData.getOver60();
			total90 = total90 + rowData.getOver90();
			total120 = total120 + rowData.getOver120();
			total180 = total180 + rowData.getOver180();
		}
		
		Double pctCurrent = totalCurrent / totalTotalDue;
		Double pct30 = total30 / totalTotalDue;
		Double pct60 = total60 / totalTotalDue;
		Double pct90 = total90 / totalTotalDue;
		Double pct120 = total120 / totalTotalDue;
		Double pct180 = total180 / totalTotalDue;
		
		Double whatTheHeck = total90 + total120 + total180;
		Double pctWhatTheHeck = whatTheHeck / totalTotalDue;
		
		CustomCellFormat recapLabelStyle = CustomCellFormat.defaultFormat();
		recapLabelStyle.setBold(true);
		recapLabelStyle.setUnderline(true);
		CustomCellFormat emptyCell = recapLabelStyle.clone();
		emptyCell.setBorder(true);
		CustomCellFormat totalDueStyle = new CustomCellFormat(CustomCellColor.BLACK,CustomCellColor.WHITE, CustomCellAlignment.RIGHT, "#,##0.00", "#,###0.00");
		totalDueStyle.setBorder(true);
		CustomCellFormat currentStyle = totalDueStyle.clone();
		currentStyle.setBackground(CustomCellColor.BRIGHT_GREEN);
		CustomCellFormat over30Style = totalDueStyle.clone();
		over30Style.setBackground(CustomCellColor.YELLOW);
		CustomCellFormat over60Style = totalDueStyle.clone();
		over60Style.setBackground(CustomCellColor.ORANGE);
		CustomCellFormat over90Style = totalDueStyle.clone();
		over90Style.setBackground(CustomCellColor.RED);
		over90Style.setForeground(CustomCellColor.WHITE);
		
		CustomCellFormat currentPctStyle = currentStyle.clone();
		currentPctStyle.setXlsDataFormat("#,##0.00%");
		CustomCellFormat over30PctStyle = over30Style.clone();
		over30PctStyle.setXlsDataFormat("#,##0.00%");
		CustomCellFormat over60PctStyle = over60Style.clone();
		over60PctStyle.setXlsDataFormat("#,##0.00%");
		CustomCellFormat over90PctStyle = over90Style.clone();
		over90PctStyle.setXlsDataFormat("#,##0.00%");

		List<CustomCell> totalRow = new ArrayList<CustomCell>();
		totalRow.add(new CustomCell("", recapLabelStyle));
		totalRow.add(new CustomCell(totalTotalDue, totalDueStyle));
		totalRow.add(new CustomCell(totalCurrent, currentStyle));
		totalRow.add(new CustomCell(total30, over30Style));
		totalRow.add(new CustomCell(total60, over60Style));
		totalRow.add(new CustomCell(total90, over90Style));
		totalRow.add(new CustomCell(total120, over90Style));
		totalRow.add(new CustomCell(total180, over90Style));
		
		List<CustomCell> totalPct = new ArrayList<CustomCell>();
		totalPct.add(new CustomCell("", recapLabelStyle));
		totalPct.add(new CustomCell("", emptyCell));
		totalPct.add(new CustomCell(pctCurrent, currentPctStyle));
		totalPct.add(new CustomCell(pct30, over30PctStyle));
		totalPct.add(new CustomCell(pct60, over60PctStyle));
		totalPct.add(new CustomCell(pct90, over90PctStyle));
		totalPct.add(new CustomCell(pct120, over90PctStyle));
		totalPct.add(new CustomCell(pct180, over90PctStyle));
		
		List<CustomCell> recapRow = new ArrayList<CustomCell>();
		recapRow.add(new CustomCell("", recapLabelStyle));
		recapRow.add(new CustomCell("RECAP", recapLabelStyle));		
		recapRow.add(new CustomCell(totalCurrent, currentStyle));
		recapRow.add(new CustomCell(total30, over30Style));
		recapRow.add(new CustomCell(total60, over60Style));
		recapRow.add(new CustomCell(whatTheHeck, over90Style));
		
		
		List<CustomCell> recapPct = new ArrayList<CustomCell>();
		recapPct.add(new CustomCell("", recapLabelStyle));
		recapPct.add(new CustomCell("", recapLabelStyle));
		recapPct.add(new CustomCell(pctCurrent, currentPctStyle));
		recapPct.add(new CustomCell(pct30, over30PctStyle));
		recapPct.add(new CustomCell(pct60, over60PctStyle));
		recapPct.add(new CustomCell(pctWhatTheHeck, over90PctStyle));
		
		
		super.addendum = new ArrayList<List<CustomCell>>();
		super.addendum.add(totalRow);
		super.addendum.add(totalPct);
		super.addendum.add( new ArrayList<CustomCell>() );   // add a blank row
		super.addendum.add(recapRow);
		super.addendum.add(recapPct);

		
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
		});
		super.makeHeaderLeft(headerLeft);
		
		
		super.setColumnWidths(new ColumnWidth[] {
				new ColumnWidth(2750,32.8F), 	// div
				new ColumnWidth(3000,32.8F),	// total
				new ColumnWidth(3000,32.8F),	// current
				new ColumnWidth(3000,32.8F),	// 30
				new ColumnWidth(3000,32.8F),	// 60
				new ColumnWidth(3000,32.8F),	// 90
				new ColumnWidth(3000,32.8F),	// 120
				new ColumnWidth(3000,32.8F),	// 180
		});

	}


	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	public static AccountsReceivableTotalsSummary buildReport(Connection conn) throws Exception {
		return AccountsReceivableTotalsSummary.buildReport(conn, Calendar.getInstance());
	}

	public static AccountsReceivableTotalsSummary buildReport(Connection conn, Calendar runDate) throws Exception {
		return new AccountsReceivableTotalsSummary(conn, runDate);
	}

	
	public class RowData extends ApplicationObject implements Comparable<RowData> {
		private static final long serialVersionUID = 1L;
		private String div;
		private Double totalDue;
		private Double current;
		private Double over30;
		private Double over60;
		private Double over90;
		private Double over120;
		private Double over180;
		
		public RowData(ResultSet rs) throws SQLException {
			super();
			this.div = rs.getString("div");
			this.totalDue = rs.getDouble("total_due");
			this.current = rs.getDouble("current");
			this.over30 = rs.getDouble("over30");
			this.over60 = rs.getDouble("over60");
			this.over90 = rs.getDouble("over90");
			this.over120 = rs.getDouble("over120");
			this.over180 = rs.getDouble("over180");
		}

		public RowData(String div, Double totalDue, Double current, Double over30, Double over60, Double over90, Double over120, Double over180) {
			super();
			this.div = div;
			this.totalDue = totalDue;
			this.current = current;
			this.over30 = over30;
			this.over60 = over60;
			this.over90 = over90;
			this.over120 = over120;
			this.over180 = over180;
		}

		public String getDiv() {
			return div;
		}

		public void setDiv(String div) {
			this.div = div;
		}

		public Double getTotalDue() {
			return totalDue;
		}

		public void setTotalDue(Double totalDue) {
			this.totalDue = totalDue;
		}

		public Double getCurrent() {
			return current;
		}

		public void setCurrent(Double current) {
			this.current = current;
		}

		public Double getOver30() {
			return over30;
		}

		public void setOver30(Double over30) {
			this.over30 = over30;
		}

		public Double getOver60() {
			return over60;
		}

		public void setOver60(Double over60) {
			this.over60 = over60;
		}

		public Double getOver90() {
			return over90;
		}

		public void setOver90(Double over90) {
			this.over90 = over90;
		}

		public Double getOver120() {
			return over120;
		}

		public void setOver120(Double over120) {
			this.over120 = over120;
		}

		public Double getOver180() {
			return over180;
		}

		public void setOver180(Double over180) {
			this.over180 = over180;
		}

		@Override
		public int compareTo(RowData o) {
			return this.div.compareTo(o.getDiv());
		}
		
	}


}
