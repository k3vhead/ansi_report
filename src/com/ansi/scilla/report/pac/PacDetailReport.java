package com.ansi.scilla.report.pac;

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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.invoice.InvoiceStyle;
import com.ansi.scilla.common.jobticket.JobFrequency;
import com.ansi.scilla.common.jobticket.JobStatus;
import com.ansi.scilla.common.jobticket.JobUtils;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivStartEnd;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivision;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public class PacDetailReport extends StandardReport implements ReportByDivStartEnd, ReportByDivision {

	private static final long serialVersionUID = 1L;
	
	public static final String FILENAME = "PAC Detail";

	private final String sqlSelectClause = "select "
			+ "\n\t$REPORT_DATE$ as report_date, "
			+ "\n\tjob.job_id, "
			+ "\n\tjob_site.name, "
			+ "\n\tjob_site.address1, "
			+ "\n\tjob_site.city, "
			+ "\n\tjob_site.state, "
			+ "\n\tjob.budget, "
			+ "\n\tjob.price_per_cleaning, "
			+ "\n\tjob.job_nbr, "
			+ "\n\tjob.job_frequency, "
			+ "\n\tjob.job_status, "
			+ "\n\t$COLUMN_DATA$ as column_data, "
			+ "\n\tCASE job.invoice_style "
			+ "\n\t  when '"+ InvoiceStyle.COD.code() +"' then 'Y' "
			+ "\n\t  else 'N'"
			+ "\n\tEND, "
			+ "\n\ttag_count.tag_list"
			+ "\n\tfrom job "
			+ "\n\tjoin quote on quote.quote_id = job.quote_id "
			+ "\n\tjoin address as bill_to on bill_to.address_id = bill_to_address_id "
			+ "\n\tjoin address as job_site on job_site.address_id = job_site_address_id "
			+ "\n\tjoin division on division.division_id = job.division_id ";
	
			
	private final String sqlWhereClause = 
			"\n\twhere division.division_id = ? "
			+ "\n\tand $REPORT_DATE$ >= ? "
			+ "\n\tand $REPORT_DATE$ <= ? "
			+ "\n\tand job.job_status in ('" + JobStatus.PROPOSED.code() +"','" + JobStatus.ACTIVE.code() + "','"+ JobStatus.CANCELED.code()+"') "
			+ "\n\torder by job_site.name";
	
//	private final String REPORT_NOTES = "notes go here";
	
	private String div;
	private Calendar startDate;
	private Calendar endDate;
	private Double totalPpc;
	private Double totalVolume;
	private List<RowData> data;
	private PacDetailReportType reportType;
	private Logger logger;
	
	public PacDetailReport(PacDetailReportType reportType) {
		super();
		this.logger = LogManager.getLogger(this.getClass());
		this.reportType = reportType;
		this.setTitle(this.reportType.reportTitle);		
	}
	
	/**
	 * Default date range is current month-to-date
	 * @param conn Database Connection
	 * @param reportType Which report is to be generated
	 * @param divisionId Division Filter
	 * @throws Exception Something bad happened
	 */
	public PacDetailReport(Connection conn, PacDetailReportType reportType, Integer divisionId) throws Exception {
		this(reportType);
		this.div = makeDivision(conn, divisionId);
		
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		startDate = (Calendar)Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);
		
		endDate = (Calendar)Midnight.getInstance(new AnsiTime());
		
		this.totalPpc = 0.00;
		this.totalVolume = 0.00;
		this.data = makeData(conn, this, divisionId, startDate, endDate);

		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(div, startDate, endDate, data, subtitle);
	}

	public PacDetailReport(Connection conn,  PacDetailReportType reportType, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		this(reportType);
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		this.div = makeDivision(conn, divisionId);
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalPpc = 0.00;
		this.totalVolume = 0.00;
		this.data = makeData(conn, this, divisionId, startDate, endDate);
		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(div, startDate, endDate, data, subtitle);
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

	public PacDetailReportType getReportType() {
		return reportType;
	}
	public Integer makeDataSize() {
		return this.data.size();
	}
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	
	private String makeDivision(Connection conn, Integer divisionId) throws Exception {
		Division division = new Division();
		division.setDivisionId(divisionId);
		division.selectOne(conn);
		return division.getDivisionNbr() + "-" + division.getDivisionCode();
	}

	private List<RowData> makeData(Connection conn, PacDetailReport report, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		
		String sql = makeSql(conn);
		PreparedStatement ps = conn.prepareStatement(sql);
		this.logger.log(Level.DEBUG, sql);
		ps.setInt(1, divisionId);
		ps.setDate(2, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(3, new java.sql.Date(endDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		
		List<RowData> data = new ArrayList<RowData>();
		while ( rs.next() ) {
			data.add(new RowData(rs, report));
		}
		rs.close();
		
		return data;
	}


	private String makeSql(Connection conn) throws Exception {
		String selectClauseDate = this.sqlSelectClause.replaceAll("\\$REPORT_DATE\\$", this.reportType.dateName); //get the right date for this report
		String selectClause = selectClauseDate.replaceAll("\\$COLUMN_DATA\\$", this.reportType.fieldName); //get the right date for this report
		String whereClause = this.sqlWhereClause.replaceAll("\\$REPORT_DATE\\$", this.reportType.dateName); //get the right date for this report
		String sql = selectClause 
				+ "\n\tleft outer join (" + JobUtils.makeTagSql(conn, "my_tags") +") as tag_count on tag_count.job_id=job.job_id\n"
				+ whereClause;
		
		return sql;
	}

	public Double getTotalPpc() {
		return this.totalPpc;
	}
	
	public Double getTotalVolume() {
		return this.totalVolume;
	}
	

	private void makeReport(String div, Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(this.reportType.reportTitle);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		
//		Font arialFont = new Font("Arial", Font.PLAIN, 18);
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("reportDate", this.reportType.dateHeader, 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobId", "Job", 1, DataFormats.NUMBER_LEFT, SummaryType.NONE),
				new ColumnHeader("name","Site Name", 1, DataFormats.STRING_FORMAT, SummaryType.NONE, null, 20),
				new ColumnHeader("address1","Street 1", 1, DataFormats.STRING_FORMAT, SummaryType.NONE, null, 20),
				new ColumnHeader("city","City", 1, DataFormats.STRING_FORMAT, SummaryType.NONE, null, 20),
				new ColumnHeader("state","State", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("budget","Budget", 1, DataFormats.CURRENCY_FORMAT, SummaryType.NONE),
				new ColumnHeader("pricePerCleaning","PPC", 1, DataFormats.CURRENCY_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobNbr","Job #", 1, DataFormats.NUMBER_CENTERED, SummaryType.NONE),
				new ColumnHeader("freq","Freq", 1, DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("jobStatus","Status", 1, DataFormats.STRING_CENTERED, SummaryType.NONE, null, 6),
				new ColumnHeader("columnData",this.reportType.columnHeader, 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("tagList", "Tags", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("volume","Volume", 1, DataFormats.CURRENCY_FORMAT, SummaryType.NONE),
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		Method dataSizeMethod = this.getClass().getMethod("makeDataSize", (Class<?>[])null);
		Method getTotalPpc = this.getClass().getMethod("getTotalPpc", (Class<?>[])null);
		Method getTotalVolume = this.getClass().getMethod("getTotalVolume", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
				new ReportHeaderRow("Total PPC:", getTotalPpc, 0, DataFormats.CURRENCY_FORMAT),
				new ReportHeaderRow("Total Volume:", getTotalVolume, 1, DataFormats.CURRENCY_FORMAT),
				new ReportHeaderRow("Jobs:", dataSizeMethod, 2, DataFormats.INTEGER_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		
		Method getDivMethod = this.getClass().getMethod("getDiv", (Class<?>[])null);
		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
		
		
		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Division:", getDivMethod, 1, DataFormats.STRING_FORMAT),
				new ReportHeaderRow("From:", getStartDateMethod, 2, DataFormats.DATE_FORMAT),
				new ReportHeaderRow("To:", getEndDateMethod, 3, DataFormats.DATE_FORMAT)
		});
		super.makeHeaderRight(headerRight);
		
		super.setColumnWidths(new ColumnWidth[] {
				new ColumnWidth(2750, 57.0F), 			//Proposed Date
				new ColumnWidth(3750, 45.0F),			// Job Code
				new ColumnWidth(11000, 140.0F),			// Site name
				new ColumnWidth(11000, 140.0F),			// Street 1
				new ColumnWidth(3500, 60.0F),			// City
				new ColumnWidth(1500, 22.5F),			// State
				new ColumnWidth(2500, 46.0F),			// Budget
				new ColumnWidth(2500, 46.0F),			// PPC
				new ColumnWidth(1400, 30.0F),			// Job #
				new ColumnWidth(1400, 27.0F),			// Freq
				new ColumnWidth(1600, 29.0F),			// Status
				new ColumnWidth(null, 42.0F),			// Lead Type
				new ColumnWidth(4000, 45.0F),			// tags
				new ColumnWidth(null, 57.5F),			// volume

		});
	}
	
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		public Date reportDate;
		public Integer jobId;
		public String name;
		public String address1;
		public String city;
		public String state;
		public Double budget;
		public Double pricePerCleaning;
		public Integer jobNbr;
		public JobFrequency jobFrequency;
		public String jobStatus;
		public String columnData;
		public Double volume;
		public String tagList;
		
		public RowData(ResultSet rs, PacDetailReport report) throws SQLException {
			this.reportDate = new Date( rs.getDate("report_date").getTime());
			this.jobId = rs.getInt("job_id");
			this.name = rs.getString("name");
//				this.name = StringUtils.abbreviate(this.name, 25);
			this.address1 = rs.getString("address1");
			this.city = rs.getString("city");
//				this.city = StringUtils.substring(this.city, 0, 8);
			this.state = rs.getString("state");
			this.budget = rs.getBigDecimal("budget").doubleValue();
			this.pricePerCleaning = rs.getBigDecimal("price_per_cleaning").doubleValue();
			report.totalPpc = report.totalPpc + this.pricePerCleaning;
			this.jobNbr = rs.getInt("job_nbr");
			this.jobFrequency = JobFrequency.lookup(rs.getString("job_frequency"));
			this.jobStatus = rs.getString("job_status");
			this.columnData = rs.getString("column_data");
//				this.columnData = StringUtils.substring(this.columnData, 0, 4);
			this.volume = this.pricePerCleaning * Double.valueOf(this.jobFrequency.annualCount()); // PPC * freq.timesPerYear
			report.totalVolume = report.totalVolume + this.volume;
			this.tagList = rs.getString("tag_list");
		}


		public Date getReportDate() {
			return reportDate;
		}
		public Integer getJobId() {
			return jobId;
		}
		public String getName() {
			return name;
		}
		public String getAddress1() {
			return address1;
		}
		public String getCity() {
			return city;
		}
		public String getState() {
			return state;
		}
		public Double getBudget() {
			return budget;
		}
		public Double getPricePerCleaning() {
			return pricePerCleaning;
		}
		public String getFreq() {
			return jobFrequency.abbrev();
		}
		public Integer getJobNbr() {
			return jobNbr;
		}
		public String getJobStatus() {
			return jobStatus;
		}
		public String getColumnData() {
			return columnData;
		}
		public Double getVolume() {
			return volume;
		}
		public String getTagList() {
			return tagList;
		}

	}
	
	protected enum PacDetailReportType {
		PROPOSED("Proposals Listing", "quote.proposal_date", "Proposed Date", "quote.lead_type", "Lead Type"),
		ACTIVATION("Activations Listing", "job.activation_date", "Activation Date", "quote.lead_type", "Lead Type"),
		CANCELLED("Cancellations Listing", "job.cancel_date", "Cancelled Date", "job.cancel_reason", "Cancel Reason");
		
		public final String reportTitle;
		public final String dateName;
		public final String dateHeader;
		public final String fieldName;
		public final String columnHeader;
		
		private PacDetailReportType(String reportTitle, String dateName, String dateHeader, String fieldName, String columnHeader) {
			this.reportTitle=reportTitle;
			this.dateName=dateName;
			this.dateHeader=dateHeader;
			this.fieldName=fieldName;
			this.columnHeader=columnHeader;
		}
	}
}
