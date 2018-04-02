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
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.invoice.InvoiceStyle;
import com.ansi.scilla.common.jobticket.TicketStatus;
import com.ansi.scilla.common.jobticket.TicketType;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.DataFormats;
import com.ansi.scilla.report.reportBuilder.DateFormatter;
import com.ansi.scilla.report.reportBuilder.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.SummaryType;

public class DispatchedOutstandingTicketReport extends StandardReport {
	
	private static final long serialVersionUID = 1L;
	
	public static final String REPORT_TITLE = "Dispatched and Outstanding Tickets List -- Trailing";
	public static final String REPORT_NOTE = "* = Tickets with statuses 'Non Dispatched' or 'Locked Freq' " +
					"require updating before they can run. Please contact the administrative team and let them " +
					"know what you would like to do with these tickets. ** = A status of 'Finished' means that the " +
					"ticket has been processed as being completed but has not yet been assigned to an invoice for billing";
	
	private final String sql = "select division.division_nbr, division.division_id, ticket.ticket_id, ticket.fleetmatics_id, address.name, address.address1, address.city, ticket.start_date, "
			+ "\n\tjob.price_per_cleaning, job.job_nbr, job.job_frequency, ticket.ticket_status, "
			+ "\n\t(	"
			+ "\n\tselect top 1 process_date "
			+ "\n\tfrom ticket "
			+ "\n\twhere ticket.job_id = job.job_id "
			+ "\n\tand ticket.process_date is not null "
			+ "\n\tand ticket.ticket_status in ('c','p','i') "
			+ "\n\tand ticket.ticket_type in ('run','job') "
			+ "\n\torder by ticket.process_date desc "
			+ "\n\t) as last_run, "
			+ "\n\tticket.ticket_type, job.invoice_style, GetDate() as ReportCreatedDate "
			+ "\nfrom ticket "
			+ "\ninner join job on job.job_id=ticket.job_id "
			+ "\ninner join quote on quote.quote_id=job.quote_id "
			+ "\ninner join address on address.address_id=quote.job_site_address_id "
			+ "\ninner join division on division.division_id=ticket.act_division_id "
			+ "\nwhere ticket.start_date <= ? "
			+ "\n\tand division.division_id=? "
			+ "\n\tand ticket_type = ? "
			+ "\nand ticket_status in (?,?) "
			+ "\norder by ticket.ticket_status, ticket.start_date, address.name";
	
	
	private final String sqlHeader = "select sum(job.price_per_cleaning) as sumppc, ticket.ticket_status "
			+ "from ticket "
			+ "inner join job on job.job_id=ticket.job_id "
			+ "inner join division on division.division_id=ticket.act_division_id "
			+ "where ticket.start_date <= ? "
			+ "and division.division_id=? "
			+ "and ticket_type = ? "
			+ "and ticket_status in (?,?) "
			+ "group by ticket_status";
	
	
	
	private String div;
	private Calendar startDate;
	private Calendar endDate;
	private List<RowData> data;
	
	Logger logger = LogManager.getLogger(this.getClass());
	private HashMap <String, Float> bannerData;
	
	private DispatchedOutstandingTicketReport() {		
		super();
		this.setTitle(REPORT_TITLE);
		this.setHeaderNotes(REPORT_NOTE);
	}
	
	private DispatchedOutstandingTicketReport(Connection conn,  Integer divisionId) throws Exception {
		this();
		this.div = makeDivision(conn, divisionId);
		
//		startDate = Midnight.getInstance(new AnsiTime());
//		startDate.set(Calendar.DAY_OF_MONTH, 1);
		
		endDate = Midnight.getInstance(new AnsiTime());
		
		this.data = makeData(conn, divisionId, endDate);
		this.bannerData = makeBannerData(conn, divisionId, endDate);
		makeReport(div, endDate, data, "Prior to Today");
	}

	private DispatchedOutstandingTicketReport(Connection conn,  Integer divisionId, Calendar endDate) throws Exception {
		this();
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		this.div = makeDivision(conn, divisionId);
		//this.startDate = startDate;
		this.endDate = endDate;
		this.data = makeData(conn, divisionId, endDate);
		this.bannerData = makeBannerData(conn, divisionId, endDate);
		//String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = "Beginning through " + endTitle;
		makeReport(div, endDate, data, subtitle);
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

	private List<RowData> makeData(Connection conn, Integer divisionId, Calendar endDate) throws Exception {
		logger.log(Level.DEBUG, sql);
		logger.log(Level.DEBUG, divisionId + "\n" + endDate);
		PreparedStatement ps = conn.prepareStatement(sql);
		int n = 1;
		ps.setDate(n, new java.sql.Date(endDate.getTimeInMillis()));
		n++;
		ps.setInt(n, divisionId);
		n++;
		ps.setString(n, TicketType.JOB.code());
		n++;
		ps.setString(n, TicketStatus.DISPATCHED.code());
		n++;
		ps.setString(n, TicketStatus.NOT_DISPATCHED.code());
		ResultSet rs = ps.executeQuery();
		
		List<RowData> data = new ArrayList<RowData>();
		while ( rs.next() ) {
			data.add(new RowData(rs));
		}
		rs.close();
		
		return data;
	}
	
	private HashMap<String, Float> makeBannerData(Connection conn, Integer divisionId, Calendar endDate) throws Exception {
		logger.log(Level.DEBUG, sqlHeader);
		logger.log(Level.DEBUG, divisionId + "\n" + endDate);
		PreparedStatement ps = conn.prepareStatement(sqlHeader);
		int n = 1;
		ps.setDate(n, new java.sql.Date(endDate.getTimeInMillis()));
		n++;
		ps.setInt(n, divisionId);
		n++;
		ps.setString(n, TicketType.JOB.code());
		n++;
		ps.setString(n, TicketStatus.DISPATCHED.code());
		n++;
		ps.setString(n, TicketStatus.NOT_DISPATCHED.code());
		ResultSet rs = ps.executeQuery();
		
		HashMap<String, Float> bannerData = new HashMap<String, Float>();
		while ( rs.next() ) {
			String ticketStatus = rs.getString("ticket_status");
			Float sumPPC = rs.getFloat("sumppc");
			bannerData.put(ticketStatus, sumPPC);
		}
		rs.close();
		
		return bannerData;
	}
	
	public Double getOutstanding() {
		return 99.99D;
	}
	
	public Float getDispatched() {
		Float dispatched = 0F;
		if (bannerData.containsKey(TicketStatus.DISPATCHED.code())){
			dispatched = bannerData.get(TicketStatus.DISPATCHED.code());
		}
		return dispatched;
	}
	
	public Float getNonDispatched() {
		Float nDis = 0F;
		if(bannerData.containsKey(TicketStatus.NOT_DISPATCHED.code())){
			nDis = bannerData.get(TicketStatus.NOT_DISPATCHED.code());
		}
		return nDis;
	}
	
	public Double getLockedFreq() {
		return 99.99D;
	}
		
	public Double getDispOutstanding() {
		return 99.99D;
	}
	
	public Double getFinished() {
		return 99.99D;
	}
	
	public Float getAllTickets() {
		Float all = 0F;
		for(Float value: bannerData.values()){
			all = all + value;
		}
		return all;
	}
	
	public Float getSubtotal(){
		Float sub = 0F;
		for(Float value: bannerData.values()){
			sub = getAllTickets() - value;
		}
		return sub;
	}
	
	
	@SuppressWarnings("unchecked")	
	private void makeReport(String div, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
		super.setHeaderNotes(REPORT_NOTE);
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("ticketId", "Ticket", DataFormats.NUMBER_CENTERED, SummaryType.NONE),
				new ColumnHeader("fleetmaticsId", "Tkt # FM", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("name","Site", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("address1","Street 1", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("city","City", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("lastRun","Last Run", DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("startDate","Run Date", DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("pricePerCleaning","PPC", DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "ticketStatus"),
				new ColumnHeader("jobNbr","J#", DataFormats.NUMBER_CENTERED, SummaryType.NONE),
				new ColumnHeader("jobFrequency", "FREQ", DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("ticketStatus", "ST", DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("invoiceStyle", "Invoice Style", DataFormats.STRING_FORMAT, SummaryType.NONE),
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		Method dataSizeMethod = this.getClass().getMethod("makeDataSize", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
				new ReportHeaderRow("Prior To:", getEndDateMethod, 0, DataFormats.DATE_FORMAT),
				new ReportHeaderRow("Tickets:", dataSizeMethod, 2, DataFormats.INTEGER_FORMAT)				
		});
		super.makeHeaderLeft(headerLeft);
		
		
		//Method getOutstanding = this.getClass().getMethod("getOutstanding", (Class<?>[])null);
		Method getDivMethod = this.getClass().getMethod("getDiv", (Class<?>[])null);
		Method getDispatched = this.getClass().getMethod("getDispatched", (Class<?>[])null);
		Method getNonDispatched = this.getClass().getMethod("getNonDispatched", (Class<?>[])null);
		Method getAllTickets = this.getClass().getMethod("getAllTickets", (Class<?>[])null);
		//Method getLockedFreq = this.getClass().getMethod("getLockedFreq", (Class<?>[])null);
		
		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
				//new ReportHeaderRow("Outstanding:", getOutstanding, 0, DataFormats.DECIMAL_FORMAT),
				new ReportHeaderRow("Division:", getDivMethod, 0, DataFormats.STRING_FORMAT),
				new ReportHeaderRow("Dispatched:", getDispatched, 1, DataFormats.DECIMAL_FORMAT),
				new ReportHeaderRow("Non-Dispatched:", getNonDispatched, 2, DataFormats.DECIMAL_FORMAT),
				new ReportHeaderRow("All Tickets:", getAllTickets, 3, DataFormats.DECIMAL_FORMAT)
				//new ReportHeaderRow("Locked Freq*:", getLockedFreq, 3, DataFormats.DECIMAL_FORMAT)
		});
		super.makeHeaderRight(headerRight);

		
		//Method getDispOutstanding = this.getClass().getMethod("getDispOutstanding", (Class<?>[])null);
		//Method getFinished = this.getClass().getMethod("getFinished", (Class<?>[])null);
		

//		List<ReportHeaderRow> headerFarRight = Arrays.asList(new ReportHeaderRow[] {
//				
//				//new ReportHeaderRow("Disp + Outst:", getDispOutstanding, 1, DataFormats.DECIMAL_FORMAT),
//				//new ReportHeaderRow("**Finished:", getFinished, 2, DataFormats.DECIMAL_FORMAT),
//				
//		});
//		super.makeHeaderRight(headerFarRight);
//
//		List<ReportHeaderCol> headerRightCols = new ArrayList<ReportHeaderCol>();
//		ReportHeaderCol col1 = new ReportHeaderCol(headerRight, 0);
//		headerRightCols.add(col1);
//		ReportHeaderCol col2 = new ReportHeaderCol(headerFarRight, 1);
//		headerRightCols.add(col2);
//		setHeaderRight(headerRightCols);
		
	}
	
	
	public static DispatchedOutstandingTicketReport buildReport(Connection conn, Integer divisionId, Calendar endDate) throws Exception {
		return new DispatchedOutstandingTicketReport(conn, divisionId, endDate);
	}
	
	public static DispatchedOutstandingTicketReport buildReport(Connection conn, Integer divisionId) throws Exception {
		return new DispatchedOutstandingTicketReport(conn, divisionId);
	}
	
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

//		public Date processDate;
//		public Integer jobId;
//		public Date invoiceDate;
		
		public Integer divisionNbr;	//
		public Integer divisionId;	//
		public Integer ticketId;	//
		public String fleetmaticsId;	//
		public String name;	//
		public String address1;	//
		public String city;	//
		public Date startDate;	//
		public Double pricePerCleaning;	//
		public Double ppcSubtotal;
		public Integer jobNbr;	//
		public String jobFrequency;
		public String ticketStatus;	//
		public String ticketType;
		public String invoiceStyle;
		public Date lastRun;

		public RowData(ResultSet rs) throws SQLException {
			this.startDate = new Date( rs.getDate("start_date").getTime());
			this.ticketId = rs.getInt("ticket_id");
			this.ticketStatus = TicketStatus.lookup(rs.getString("ticket_status")).display();
			this.pricePerCleaning = rs.getBigDecimal("price_per_cleaning").doubleValue();
			this.ppcSubtotal = rs.getBigDecimal("price_per_cleaning").doubleValue();
			//this.jobId = rs.getInt("job_id");
			this.jobNbr = rs.getInt("job_nbr");
			this.name = rs.getString("name");
			this.address1 = rs.getString("address1");
			this.divisionNbr = rs.getInt("division_nbr");
			this.divisionId = rs.getInt("division_id");
			this.fleetmaticsId = rs.getString("fleetmatics_id");
			this.city = rs.getString("city");
			this.jobFrequency = rs.getString("job_frequency");
			String ticketType = rs.getString("ticket_type"); 
			this.ticketType = TicketType.lookup(ticketType).display();
			//String invoiceStyle = rs.getString("invoice_style");
			Object invoiceStyle = rs.getObject("invoice_style");
			if( invoiceStyle != null ){
				this.invoiceStyle = new String(rs.getString("invoice_style"));
			}
			Object lastRun = rs.getObject("last_run");
			if ( lastRun != null ) {
				this.lastRun = new Date(rs.getDate("last_run").getTime());
			}
		}

		public String getCity() {
			return city;
		}
		public Integer getDivisionNbr() {
			return divisionNbr;
		}
		public Integer getDivisionId() {
			return divisionId;
		}
		public String getFleetmaticsId() {
			return fleetmaticsId;
		}
		public String getJobFrequency() {
			return jobFrequency;
		}
		public Date getStartDate() {
			return startDate;
		}
		public Integer getTicketId() {
			return ticketId;
		}
		public String getTicketStatus() {
			return ticketStatus;
		}
		public Double getPricePerCleaning() {
			return pricePerCleaning;
		}
		public Double getPPCSubtotal(){
			return ppcSubtotal;
		}
//		public Integer getJobId() {
//			return jobId;
//		}
		public Integer getJobNbr() {
			return jobNbr;
		}
		public String getName() {
			return name;
		}
		public String getAddress1() {
			return address1;
		}
//		public Date getInvoiceDate() {
//			return invoiceDate;
//		}
		public String getTicketType() {
			return ticketType;
		}
		public String getInvoiceStyle() {
			return invoiceStyle;
		}
		public Date getLastRun() {
			return lastRun;
		}

	}
	
	
}
