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
	
	public static final  String REPORT_TITLE = "DO List";
	
//	private final String sql = "select ticket.process_date, ticket.ticket_id, ticket.ticket_status, ticket.act_price_per_cleaning, "
//			+ "\n\tjob.job_id, job.job_nbr, "
//			+ "\n\taddress.name, address.address1 "
//			+ "\nfrom ticket "
//			+ "\ninner join job on job.job_id=ticket.job_id "
//			+ "\ninner join quote on quote.quote_id=job.quote_id "
//			+ "\ninner join address on address.address_id=quote.job_site_address_id "
//			+ "\nwhere ticket.act_division_id=? and ticket.process_date>? and ticket.process_date<? "
//			+ "\norder by ticket.ticket_id asc";
	
	private final String sql = "select division.division_nbr, division.division_id, ticket.ticket_id, ticket.fleetmatics_id, address.name, address.address1, address.city, ticket.start_date, "
			+ "\n\tjob.price_per_cleaning, job.job_nbr, job.job_frequency, ticket.ticket_status, "
			+ "\n\t(	"
			+ "\n\tselect top 1 process_date "
			+ "\n\tfrom ticket "
			+ "\n\twhere ticket.job_id = job.job_id "
			+ "\n\tand ticket.process_date is not null "
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
			+ "\norder by division_nbr, ticket.start_date asc, address.name";
	
	/*
	 * SET NOCOUNT ON;

DECLARE @PRIOR_TO DateTime2(0);
DECLARE @DIVISION_START int;
DECLARE @DIVISION_END int;

--set @PRIOR_TO = EOMONTH(getdate()
set @DIVISION_START = 12;
SET @DIVISION_END = 89;

SELECT         
	division_nbr
	, division_id
	, ticket.ticket_id 
	, ticket.fleetmatics_id 
	, address.name 
	, address.address1 
	, address.city 
	, ticket.start_date 
	, job.price_per_cleaning 
	, job.job_nbr 
	, job.job_frequency 
	, ticket.ticket_status 
	, (	
		select top 1 process_date 
		from ticket 
		where ticket.job_id = job.job_id 
		and ticket.process_date is not null 
		order by ticket.process_date desc
	  ) as last_run     
	, ticket.ticket_type
	, job.invoice_style 
	, dateadd(day,1, EOMONTH(getdate())) as PriorToDate 
	, GetDate() as ReportCreatedDate 
FROM ticket 
INNER JOIN job 
	ON job.job_id = ticket.job_id 
INNER JOIN quote 
    ON quote.quote_id = job.quote_id 
INNER JOIN address 
	ON address.address_id = quote.job_site_address_id 
INNER JOIN division 
    ON division.division_id = ticket.act_division_id 
where ticket.start_date <= EOMONTH(getdate())
   and division.division_nbr >= @DIVISION_START AND DIVISION.division_nbr <= @DIVISION_END
   and ticket_type = 'job' 
and ticket_status in ('D','N')
order by division_nbr, ticket.start_date asc, address.name 
	 * 
	 * */
	
	
	
	//Asana has correct above sql
	
	private String div;
	private Calendar startDate;
	private Calendar endDate;
	private List<RowData> data;
	
	private DispatchedOutstandingTicketReport() {		
		super();
		this.setTitle(REPORT_TITLE);
	}
	
	private DispatchedOutstandingTicketReport(Connection conn,  Integer divisionId) throws Exception {
		this();
		this.div = makeDivision(conn, divisionId);
		
//		startDate = Midnight.getInstance(new AnsiTime());
//		startDate.set(Calendar.DAY_OF_MONTH, 1);
		
		endDate = Midnight.getInstance(new AnsiTime());
		
		this.data = makeData(conn, divisionId, endDate);
		makeReport(div, endDate, data, "Prior to Today");
	}

	private DispatchedOutstandingTicketReport(Connection conn,  Integer divisionId, Calendar endDate) throws Exception {
		this();
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		this.div = makeDivision(conn, divisionId);
		//this.startDate = startDate;
		this.endDate = endDate;
		this.data = makeData(conn, divisionId, endDate);
		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
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
		System.out.println(sql);
		System.out.println(divisionId + "\n" + endDate);
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
	
	public Double getCompletedINV() {
		return 1.23D;
	}
	
	public Double getCompletedPPC() {
		return 4.56D;
	}
	
	@SuppressWarnings("unchecked")	
	private void makeReport(String div, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		

		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("ticketId", "Ticket", DataFormats.INTEGER_FORMAT, SummaryType.NONE),
				new ColumnHeader("fleetmaticsId", "Tkt # FM", DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("name","Site", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("address1","Street 1", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("city","City", DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("lastRun","Last Run", DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("startDate","Run Date", DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("pricePerCleaning","PPC", DataFormats.CURRENCY_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobNbr","J#", DataFormats.NUMBER_CENTERED, SummaryType.NONE),
				new ColumnHeader("jobFrequency", "FREQ", DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("ticketType", "ST", DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("invoiceStyle", "Invoice Style", DataFormats.STRING_FORMAT, SummaryType.NONE),
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getDivMethod = this.getClass().getMethod("getDiv", (Class<?>[])null);
		//Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
				new ReportHeaderRow("Division:", getDivMethod, 1, DataFormats.STRING_FORMAT),
				//new ReportHeaderRow("From:", getStartDateMethod, 2, DataFormats.DATE_FORMAT),
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
		public Integer fleetmaticsId;	//
		public String name;	//
		public String address1;	//
		public String city;	//
		public Date startDate;	//
		public Double pricePerCleaning;	//
		public Integer jobNbr;	//
		public Integer jobFrequency;
		public String ticketStatus;	//
		public String ticketType;
		public String invoiceStyle;
		public Date lastRun;

		public RowData(ResultSet rs) throws SQLException {
			this.startDate = new Date( rs.getDate("start_date").getTime());
			this.ticketId = rs.getInt("ticket_id");
			this.ticketStatus = TicketStatus.lookup(rs.getString("ticket_status")).display();
			this.pricePerCleaning = rs.getBigDecimal("price_per_cleaning").doubleValue();
			//this.jobId = rs.getInt("job_id");
			this.jobNbr = rs.getInt("job_nbr");
			this.name = rs.getString("name");
			this.address1 = rs.getString("address1");
			this.divisionNbr = rs.getInt("division_nbr");
			this.divisionId = rs.getInt("division_id");
			this.fleetmaticsId = rs.getInt("fleetmatics_id");
			this.city = rs.getString("city");
			this.jobFrequency = rs.getInt("job_frequency");
			this.ticketType = rs.getString("ticket_type");
			this.invoiceStyle = rs.getString("invoice_style");
			this.lastRun = new Date(rs.getDate("last_run").getTime());
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
		public Integer getFleetmaticsId() {
			return fleetmaticsId;
		}
		public Integer getJobFrequency() {
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
