package com.ansi.scilla.report.ticket;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.jobticket.TicketStatus;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.DataFormats;
import com.ansi.scilla.report.reportBuilder.DateFormatter;
import com.ansi.scilla.report.reportBuilder.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.SummaryType;

public class TicketStatusReport extends StandardReport {

	private static final long serialVersionUID = 1L;

	private final String sql = "select ticket.process_date, ticket.ticket_id, ticket.ticket_status, "
			+ "\n\tjob.price_per_cleaning, ticket.act_price_per_cleaning, "
			+ "\n\tjob.job_id, job.job_nbr, "
			+ "\n\taddress.name, address.address1 "
			+ "\nfrom ticket "
			+ "\ninner join job on job.job_id=ticket.job_id "
			+ "\ninner join quote on quote.quote_id=job.quote_id "
			+ "\ninner join address on address.address_id=quote.job_site_address_id "
			+ "\nwhere ticket.act_division_id=? and ticket.process_date>=? and ticket.process_date<=? "
			+ "\nand ticket.ticket_status in ('c','i','p') and ticket.ticket_type in ('run','job') "
			+ "\norder by ticket.ticket_id asc";
	
	public static final  String REPORT_TITLE = "Ticket Status Report";
//	private final String REPORT_NOTES = "notes go here";
	
	private String div;
	private Calendar startDate;
	private Calendar endDate;
	private List<RowData> data;
	private Double pricePerCleaningTotal;
	private Double actPricePerCleaningTotal;
	
	private TicketStatusReport() {		
		super();
		this.setTitle(REPORT_TITLE);
	}
	/**
	 * Default date range is current month-to-date
	 * @param conn
	 * @param divisionId
	 * @throws Exception
	 */
	private TicketStatusReport(Connection conn,  Integer divisionId) throws Exception {
		this();
		this.div = makeDivision(conn, divisionId);
		
		startDate = Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		
		endDate = Midnight.getInstance(new AnsiTime());
		endDate.add(Calendar.DAY_OF_MONTH, 1);
		
		this.data = makeData(conn, divisionId, startDate, endDate);
		makeReport(div, startDate, endDate, data, "Current Month to Date");
		this.pricePerCleaningTotal = makePricePerCleaningTotal(conn, divisionId, startDate, endDate);
		this.actPricePerCleaningTotal = makeActPricePerCleaningTotal(conn, divisionId, startDate, endDate);

	}

	private TicketStatusReport(Connection conn,  Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		this();
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		this.div = makeDivision(conn, divisionId);
		this.startDate = startDate;
		this.endDate = endDate;
		this.data = makeData(conn, divisionId, startDate, endDate);		
		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(div, startDate, endDate, data, subtitle);
		this.pricePerCleaningTotal = makePricePerCleaningTotal(conn, divisionId, startDate, endDate);
		this.actPricePerCleaningTotal = makeActPricePerCleaningTotal(conn, divisionId, startDate, endDate);
	}
	
	
	public String getDiv() {
		return div;
	}

	public void setDiv(String div) {
		this.div = div;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public Integer makeDataSize() {
		return this.data.size();
	}
	
	private String makeDivision(Connection conn, Integer divisionId) throws Exception {
		Division division = new Division();
		division.setDivisionId(divisionId);
		division.selectOne(conn);
		return division.getDivisionNbr() + "-" + division.getDivisionCode();
	}

	private List<RowData> makeData(Connection conn, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.add(Calendar.DAY_OF_MONTH, 1);
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, divisionId);
		ps.setDate(2, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(3, new java.sql.Date(endDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		
		List<RowData> data = new ArrayList<RowData>();
		while ( rs.next() ) {
			data.add(new RowData(rs));
		}
		rs.close();
		
		return data;
	}

	private Double makePricePerCleaningTotal(Connection conn, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		Double ppcTotal = 0.00;
		String sql = "select isnull(sum(job.price_per_cleaning),'0.00') as price_per_cleaning_total"  
					+ "\n from ticket "
					+ "\n join job on job.job_id = ticket.job_id " 
					+ "\n where ticket.act_division_id=? and ticket.process_date>=? and ticket.process_date<=? " 
					+ "\n and ticket.ticket_status in ('c','i','p') and ticket.ticket_type in ('run','job') ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, divisionId);
		ps.setDate(2, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(3, new java.sql.Date(endDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			ppcTotal = rs.getDouble("price_per_cleaning_total");
		}
		rs.close();
		
		return ppcTotal;
	}
	private Double makeActPricePerCleaningTotal(Connection conn, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		Double actPpcTotal = 0.00;
		String sql = "select isnull(sum(act_price_per_cleaning),'0.00') as act_price_per_cleaning_total"  
					+ "\n from ticket " 
					+ "\n where ticket.act_division_id=? and ticket.process_date>=? and ticket.process_date<=? " 
					+ "\n and ticket.ticket_status in ('c','i','p') and ticket.ticket_type in ('run','job') ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, divisionId);
		ps.setDate(2, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(3, new java.sql.Date(endDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			actPpcTotal = rs.getDouble("act_price_per_cleaning_total");
		}
		rs.close();
		
		return actPpcTotal;
	}
	public Double getCompletedINV() {
		return this.actPricePerCleaningTotal;
	}
	
	public Double getCompletedPPC() {
		return this.pricePerCleaningTotal;
	}
	
	
	@SuppressWarnings("unchecked")	
	private void makeReport(String div, Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("processDate", "Date Completed", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobId", "Job Id", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("ticketId","Ticket #", 1, DataFormats.NUMBER_FORMAT, SummaryType.COUNT),
				new ColumnHeader("ticketStatus","Status", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("PricePerCleaning","PPC", 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM),
				new ColumnHeader("actPricePerCleaning","Invoiced", 1, DataFormats.CURRENCY_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobNbr","Job #", 1, DataFormats.NUMBER_CENTERED, SummaryType.NONE),
				new ColumnHeader("name","Site Name", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("address1","Site Address", 2, DataFormats.STRING_FORMAT, SummaryType.NONE),
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getDivMethod = this.getClass().getMethod("getDiv", (Class<?>[])null);
		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
				new ReportHeaderRow("Division:", getDivMethod, 1, DataFormats.STRING_FORMAT),
				new ReportHeaderRow("From:", getStartDateMethod, 2, DataFormats.DATE_FORMAT),
				new ReportHeaderRow("To:", getEndDateMethod, 3, DataFormats.DATE_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		
		
		Method dataSizeMethod = this.getClass().getMethod("makeDataSize", (Class<?>[])null);
		Method getCompletedPPC = this.getClass().getMethod("getCompletedPPC", (Class<?>[])null);
		Method getCompletedINV = this.getClass().getMethod("getCompletedINV", (Class<?>[])null);
		
		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Completed PPC:", getCompletedPPC, 0, DataFormats.CURRENCY_FORMAT),
				new ReportHeaderRow("Completed INV:", getCompletedINV, 1, DataFormats.CURRENCY_FORMAT),
				new ReportHeaderRow("Tickets:", dataSizeMethod, 2, DataFormats.INTEGER_FORMAT)
		});
		super.makeHeaderRight(headerRight);
	}
	
	
	public static TicketStatusReport buildReport(Connection conn, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		return new TicketStatusReport(conn, divisionId, startDate, endDate);
	}
	
	public static TicketStatusReport buildReport(Connection conn, Integer divisionId) throws Exception {
		return new TicketStatusReport(conn, divisionId);
	}
	
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		public Date processDate;
		public Integer ticketId;
		public String ticketStatus;
		public Double pricePerCleaning;
		public Double actPricePerCleaning;
		public Integer jobId;
		public Integer jobNbr;
		public String name;
		public String address1;
		
		public RowData(ResultSet rs) throws SQLException {
			this.processDate = new Date( rs.getDate("process_date").getTime());
			this.ticketId = rs.getInt("ticket_id");
			this.ticketStatus = TicketStatus.lookup(rs.getString("ticket_status")).display();
			this.pricePerCleaning = rs.getBigDecimal("price_per_cleaning").doubleValue();
			this.actPricePerCleaning = rs.getBigDecimal("act_price_per_cleaning").doubleValue();
			this.jobId = rs.getInt("job_id");
			this.jobNbr = rs.getInt("job_nbr");
			this.name = rs.getString("name");
			this.address1 = rs.getString("address1");
		}

		public Date getProcessDate() {
			return processDate;
		}
		public Integer getTicketId() {
			return ticketId;
		}
		public String getTicketStatus() {
			return ticketStatus;
		}
		public Double getPricePerCleaning() {
			return actPricePerCleaning;
		}
		public Double getActPricePerCleaning() {
			return actPricePerCleaning;
		}
		public Integer getJobId() {
			return jobId;
		}
		public Integer getJobNbr() {
			return jobNbr;
		}
		public String getName() {
			return name;
		}
		public String getAddress1() {
			return address1;
		}

	}
}
