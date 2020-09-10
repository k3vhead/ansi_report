package com.ansi.scilla.report.woAndFees;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public class WOAndFeesDetailReport extends StandardReport implements ReportByDivStartEnd {

	private static final long serialVersionUID = 1L;

	public static final  String REPORT_TITLE = "WO and Fees Detail by Division";
	public static final String FILENAME = "WOandFeesDetail";
	
	private final String sql = "select " +
			", concat(division_nbr, '-', division_code) as div" +
			", ticket.ticket_type as ticket_type\n" + 
			", ticket.ticket_id as ticket_id, job_site.name as job_site_name" +
			", job.job_id as job_id, job_site.address1 as job_address\n" + 
			", job.job_nbr as job_nbr, \n" + 
			"ticket.invoice_id as invoice_id, ticket.invoice_date as invoice_date" +
			", ticket.act_price_per_cleaning as ppc\n" + 
			", ticket.process_notes as notes\n" + 
			"from ticket\n" + 
			"join division on division.division_id = ticket.act_division_id and division.division_id=?\n" + 
			"join job on job.job_id = ticket.job_id\n" + 
			"join quote on quote.quote_id = job.quote_id\n" + 
			"join address as job_site on job_site.address_id = job_site_address_id\n" + 
			"where invoice_date >= ? and invoice_date < ? \n" + 
			"and ticket.ticket_type in ('fee','writeoff')\n" + 
			"order by division_nbr, ticket_type, name, job_nbr, invoice_date";
			
	private Division division;
	private List<RowData> data;

	Logger logger = LogManager.getLogger(this.getClass());

	private WOAndFeesDetailReport(Connection conn, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);
		this.division = new Division();
		this.division.setDivisionId(divisionId);
		this.division.selectOne(conn);
		this.runDate = startDate;
		this.data = this.makeData(conn, divisionId, runDate, endDate);
		makeReport(data);
	}
	
	public Division getDivision() {
		return division;
	}

	private List<RowData> makeData(Connection conn, Integer divisionId, Calendar runDate, Calendar endDate) throws SQLException {
		List<RowData> data = new ArrayList<RowData>();
		logger.log(Level.DEBUG, sql);
		PreparedStatement ps = conn.prepareStatement(sql);
		java.sql.Date sqlDate = new java.sql.Date(runDate.getTime().getTime());
		ps.setInt(1, divisionId);
		ps.setDate(2, sqlDate);
		sqlDate = new java.sql.Date(endDate.getTime().getTime());
		ps.setDate(3, sqlDate);
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
				new ColumnHeader("ticket_type", "Fee/WO", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("ticket_id","Ticket", 1, DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("job_site_name", "Job Site", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("job_id", "Job", 1, DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("job_address", "Job Address", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("job_nbr", "Job #", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),				
				new ColumnHeader("invoice", "Invoice", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("invoice_date", "Inv Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("ppc", "PPC", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
				new ColumnHeader("notes", "Notes", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
		});
		
		List<Object> oData = IterableUtils.toList(CollectionUtils.collect(data, new ObjectTransformer()));
		super.setDataRows(oData);		
		
		super.setColumnWidths(new ColumnWidth[] {
				new ColumnWidth(2000,15.0F), 	// div
				new ColumnWidth(2750,20.0F),	// ticketType
				new ColumnWidth(2000,15.0F),	// ticketId
				new ColumnWidth(6000,45.0F),	// jobSiteName
				new ColumnWidth(2800,25.0F),	// jobId
				new ColumnWidth(6000,45.0F),	// jobAddress
				new ColumnWidth(2000,20.0F),	// jobNbr
				new ColumnWidth(3000,25.8F),	// invoice
				new ColumnWidth(3000,25.8F),	// InvoiceDate
				new ColumnWidth(3000,25.8F),	// ppc
				new ColumnWidth(3000,25.8F),	// notes
		});
	}

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

	
	public static WOAndFeesDetailReport buildReport(Connection conn, Calendar runDate, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		return new WOAndFeesDetailReport(conn, divisionId, startDate, endDate);
	}
	
	public static WOAndFeesDetailReport buildReport(Connection conn, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		return WOAndFeesDetailReport.buildReport(conn, Calendar.getInstance(), divisionId, startDate, endDate);
	}
	
	
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		
		private String div;
		private String ticketType;
		private String ticketId;
		private String jobSiteName;
		private String jobId;
		private String jobAddress;
		private String jobNbr;
		private Integer invoice;
		private java.sql.Date invoiceDate;
		private BigDecimal ppc;
		private String notes;
		
		public RowData(ResultSet rs) throws SQLException {
			super();
			this.div = rs.getString("div");
			this.ticketType = rs.getString("ticket_type");
			this.ticketId = rs.getString("ticket_id");
			this.jobSiteName = rs.getString("job_site_name");
			this.jobId = rs.getString("job_id");
			this.jobAddress = rs.getString("job_address");
			this.jobNbr = rs.getString("job_nbr");
			this.invoice = rs.getInt("invoice_id");
			this.invoiceDate = rs.getDate("invoice_date");
			this.ppc = rs.getBigDecimal("ppc");
			this.notes = rs.getString("notes");
		}

		public String getDiv() {
			return div;
		}
		public void setDiv(String div) {
			this.div = div;
		}
		
		public String getTicketType() {
			return ticketType;
		}
		public void setTicketType(String ticketType) {
			this.ticketType = ticketType;
		}
		
		public String getTicketId() {
			return ticketId;
		}
		public void setTicketId(String ticketId) {
			this.ticketId = ticketId;
		}
		
		public String getJobSiteName() {
			return jobSiteName;
		}
		public void setJobSiteName(String jobSiteName) {
			this.jobSiteName = jobSiteName;
		}
		
		public String getJobId() {
			return jobId;
		}
		public void setJobId(String jobId) {
			this.jobId = jobId;
		}
		
		public String getJobAddress() {
			return jobAddress;
		}
		public void setJobAddress(String jobAddress) {
			this.jobAddress = jobAddress;
		}
		
		public String getJobNbr() {
			return jobNbr;
		}
		public void setJobNbr(String jobNbr) {
			this.jobNbr = jobNbr;
		}
		
		public Integer getInvoice() {
			return invoice;
		}
		public void setJobInvoice(Integer invoice) {
			this.invoice = invoice;
		}
		
		public java.sql.Date getInvoiceDate() {
			return invoiceDate;
		}
		public void setInvoiceDate(java.sql.Date invoiceDate) {
			this.invoiceDate = invoiceDate;
		}
		
		public BigDecimal getPpc() {
			return ppc;
		}
		public void setPpc(BigDecimal ppc) {
			this.ppc = ppc;
		}
		
		public String getNotes() {
			return notes;
		}
		public void setNotes(String notes) {
			this.notes = notes;
		}
	}
}
