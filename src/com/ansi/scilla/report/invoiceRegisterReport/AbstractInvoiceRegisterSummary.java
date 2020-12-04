package com.ansi.scilla.report.invoiceRegisterReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public abstract class AbstractInvoiceRegisterSummary extends StandardReport implements ReportByStartEnd {

	
	
	private static final long serialVersionUID = 1L;

	

	private final String REPORT_NOTES = null;

	private Calendar startDate;
	private Calendar endDate;
	private List<RowData> data;
	private String label;

	Logger logger = LogManager.getLogger(this.getClass());
	
	protected AbstractInvoiceRegisterSummary(String label) {
		super();
		this.setTitle("Cash Summary By " + label);
		this.label = label;
	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database connection
	 * @throws Exception Something bad happened
	 */
	protected AbstractInvoiceRegisterSummary(Connection conn, String reportTitle) throws Exception {
		this(reportTitle);

		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		startDate = (Calendar)Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);
		
		endDate = (Calendar)Midnight.getInstance(new AnsiTime());
		
		this.data = makeData(conn, this, startDate, endDate);
		logger.info("InvoiceRegisterSummaryReport:this.data:"+data+"\tStart:"+startDate.getTime()+"\tEnd:"+endDate.getTime());

		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(startDate, endDate, data, subtitle);
	}

	protected AbstractInvoiceRegisterSummary(Connection conn, Calendar startDate, Calendar endDate, String reportTitle) throws Exception {
		this(reportTitle);
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
		return makeFileName(getFile(), runDate, division, startDate, endDate);
	}
	
	protected abstract String getFile();
	protected abstract String getSql();
	
	private List<RowData> makeData(Connection conn, AbstractInvoiceRegisterSummary report, Calendar startDate, Calendar endDate) throws SQLException {
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);

		List<RowData> data = new ArrayList<RowData>();
		PreparedStatement ps = conn.prepareStatement(getSql());
		ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));

		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			data.add(new RowData(rs));
		}
		rs.close();
		
		return data;
	}
	
	

	private void makeReport(Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(getTitle());	
		super.setSubtitle(subtitle);
		super.setHeaderNotes(REPORT_NOTES);

		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("name", label, 2, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("ticketCount", "Ticket Count", 1, DataFormats.INTEGER_FORMAT, SummaryType.NONE),
				new ColumnHeader("invoicedAmount", "Invoiced Amount", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),

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
		private Integer ticketCount;
		private Double invoicedAmount;

		
		public RowData(ResultSet rs) throws SQLException {
			super();
			this.name = rs.getString("name");
			this.ticketCount = rs.getInt("ticket_count");
			this.invoicedAmount = rs.getDouble("invoiced_amount");

		}


		public String getName() {
			return name;
		}


		public void setName(String name) {
			this.name = name;
		}

		public Integer getTicketCount() {
			return ticketCount;
		}


		public void setTicketCount(Integer ticketCount) {
			this.ticketCount = ticketCount;
		}


		public Double getInvoicedAmount() {
			return invoicedAmount;
		}


		public void setInvoicedAmount(Double invoicedAmount) {
			this.invoicedAmount = invoicedAmount;
		}

		
	}
	
	
}