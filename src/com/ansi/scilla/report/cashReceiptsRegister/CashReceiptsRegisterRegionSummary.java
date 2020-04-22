package com.ansi.scilla.report.cashReceiptsRegister;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.db.DivisionGroup;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;

public class CashReceiptsRegisterRegionSummary extends StandardReport implements ReportByStartEnd {

	private static final long serialVersionUID = 1L;
	
	public static final String FILENAME = "CRR_Region_Summary";
	

//	 Region summary - includes a recursive join with division_group to join the parent regions for the companies
	private final String REGION_SUMMARY_SQL = "select division_group.name " +
			"\n, isnull(region.amount,'0.00') as amount " +
			"\n, isnull(region.tax_amt,'0.00') as tax_amt " +
			"\n, isnull(region.total,'0.00') as total " +
			"\nfrom division_group " +
			"\nleft outer join (select " +
			"\n    region.name " +
			"\n	, isnull(sum(ticket_payment.amount),'0.00') as amount " +
			"\n	, isnull(sum(ticket_payment.tax_amt),'0.00') as tax_amt " +
			"\n	, isnull(sum(ticket_payment.amount+ticket_payment.tax_amt),'0.00') as total " +
			"\n	from division_group as region " +
			"\n	join division_group as company on company.parent_id = region.group_id " +
			"\n	join division on division.group_id = company.group_id " +
			"\n	join ticket on division.division_id = ticket.act_division_id " +
			"\n	join ticket_payment on ticket_payment.ticket_id = ticket.ticket_id " +
			"\n	join payment on payment.payment_id = ticket_payment.payment_id " +
			"\n	where payment_date >= ? and payment_date <= ? and company.group_type = ? " +
			"\n	group by region.name) as region on region.name = division_group.name " +
			"\nwhere division_group.group_type = ? " +
			"\norder by division_group.name"; 
	

	
	public static final String REPORT_TITLE = "Cash Summary By Region";
	private final String REPORT_NOTES = null;

	private Calendar startDate;
	private Calendar endDate;
	private List<RowData> data;

	Logger logger = LogManager.getLogger(this.getClass());
	
	public CashReceiptsRegisterRegionSummary() {
		super();
		this.setTitle(REPORT_TITLE);
	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database connection
	 * @throws Exception something bad happened
	 */
	protected CashReceiptsRegisterRegionSummary(Connection conn) throws Exception {
		this();

		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		startDate = (Calendar)Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);
		
		endDate = (Calendar)Midnight.getInstance(new AnsiTime());
		
		this.data = makeData(conn, this, startDate, endDate);
		logger.info("CashReceiptsRegisterSummaryReport:this.data:"+data+"\tStart:"+startDate.getTime()+"\tEnd:"+endDate.getTime());

		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(startDate, endDate, data, subtitle);
	}

	protected CashReceiptsRegisterRegionSummary(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		this();
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		this.startDate = startDate;
		this.endDate = endDate;
		this.data = makeData(conn, this, startDate, endDate);
		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(startDate, endDate, data, subtitle);
	}
	
	
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	
	
	private List<RowData> makeData(Connection conn, CashReceiptsRegisterRegionSummary report, Calendar startDate, Calendar endDate) throws SQLException {
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);

		List<RowData> data = new ArrayList<RowData>();
		PreparedStatement ps = conn.prepareStatement(REGION_SUMMARY_SQL);
		ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));
		ps.setString(3, DivisionGroup.GroupType.COMPANY.toString());
		ps.setString(4, DivisionGroup.GroupType.REGION.toString());
		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			data.add(new RowData(rs));
		}
		rs.close();
		
		return data;
	}
	@SuppressWarnings("unchecked")
	private void makeReport(Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
		super.setHeaderNotes(REPORT_NOTES);

		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("name", "Region", 2, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("amount", "Invoices\nPaid\nAmount", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
				new ColumnHeader("taxAmt", "Taxes\nPaid\nAmount", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
				new ColumnHeader("total", "Total\nPayment\nAmount", 2, DataFormats.DECIMAL_FORMAT, SummaryType.SUM)//,
//				new ColumnHeader("excess", "Excess Cash Amount", DataFormats.DECIMAL_FORMAT, SummaryType.SUM)
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
//		List<ReportHeaderRow> headerLeft = new ArrayList<ReportHeaderRow>();
//		super.makeHeaderLeft(headerLeft);
		
//		List<ReportHeaderRow> headerRight = new ArrayList<ReportHeaderRow>();
//		super.makeHeaderRight(headerRight);
	}
	
	
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		private String name;
		private Double amount;
		private Double taxAmt;
		private Double total;
		private Double excess;
		
		public RowData(ResultSet rs) throws SQLException {
			super();
			this.name = rs.getString("name");
			this.amount = rs.getDouble("amount");
			this.taxAmt = rs.getDouble("tax_amt");
			this.total = rs.getDouble("total");
			this.excess = -1.0;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Double getAmount() {
			return amount;
		}
		public void setAmount(Double amount) {
			this.amount = amount;
		}
		public Double getTaxAmt() {
			return taxAmt;
		}
		public void setTaxAmt(Double taxAmt) {
			this.taxAmt = taxAmt;
		}
		public Double getTotal() {
			return total;
		}
		public void setTotal(Double total) {
			this.total = total;
		}
		public Double getExcess() {
			return excess;
		}
		public void setExcess(Double excess) {
			this.excess = excess;
		}		
	}
	
	public static CashReceiptsRegisterRegionSummary buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new CashReceiptsRegisterRegionSummary( conn, startDate, endDate);
	}
	public static CashReceiptsRegisterRegionSummary buildReport(Connection conn) throws Exception {
		return new CashReceiptsRegisterRegionSummary( conn);
	}
}
