package com.ansi.scilla.report.sixMonthRollingVolume;

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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.jobticket.JobFrequency;
import com.ansi.scilla.common.jobticket.JobStatus;
import com.ansi.scilla.common.jobticket.TicketDateGenerator;
import com.ansi.scilla.common.jobticket.TicketStatus;
import com.ansi.scilla.common.jobticket.TicketType;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.DataFormats;
import com.ansi.scilla.report.reportBuilder.DateFormatter;
import com.ansi.scilla.report.reportBuilder.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.SummaryType;

public class SmrvDetailReport extends StandardReport {

	private static final long serialVersionUID = 1L;

	final static String sql = "select job_site.name as job_site_name "
			+ "\n\t, job_site.zip "
			+ "\n\t, job_site.address1 "
			+ "\n\t, job.job_id "
			+ "\n\t, job.job_nbr "
			+ "\n\t, job.job_frequency "
			+ "\n\t, job.price_per_cleaning "
			+ "\n\t, job.start_date "
			+ "\n\t, max(t.process_date) as last_run "
			+ "\nfrom job "
			+ "\ninner join quote on quote.quote_id = job.quote_id "
			+ "\ninner join address as job_site on job_site.address_id = quote.job_site_address_id "
			+ "\ninner join division on division.division_id = job.division_id "
			+ "\nleft outer join ticket t on ticket_status in (?,?,?) and ticket_type in (?,?) and t.job_id = job.job_id "
			+ "\nwhere division.division_id = ? "
			+ "\n\tand job.job_status = ?	 "
			+ "\ngroup by job_site.name "
			+ "\n\t, job_site.zip "
			+ "\n\t, job_site.address1 "
			+ "\n\t, job.job_id "
			+ "\n\t, job.job_nbr "
			+ "\n\t, job.job_frequency "
			+ "\n\t, job.price_per_cleaning "
			+ "\n\t, job.start_date "
			+ "\norder by job_site_name, job_site.address1, job_nbr";	
	
	public static final String REPORT_TITLE = "Six Month Rolling Volume";
//	private final String REPORT_NOTES = "notes go here";
	
	private String div;
	private String startMonth;
	private String endMonth;
	private String headerRange;
	private Calendar startDate;
	private List<RowData> data;
	private int jobCount = 0;   // total row count
	private int contractCount = 0;   // distinct job sites
	private Double[] monthlyTotal = new Double[] {0.0D,0.0D,0.0D,0.0D,0.0D,0.0D};

	public static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S");
	private Logger logger;
	
	public SmrvDetailReport() {
		super();
		this.logger = LogManager.getLogger(this.getClass());
		this.setTitle(REPORT_TITLE);
		this.data = new ArrayList<RowData>();
	}
	
	/**
	 * Default Start Date is current Day 1 current month
	 * @param conn Database Connection
	 * @param reportType Which report is to be generated
	 * @param divisionId Division Filter
	 * @throws Exception Something bad happened
	 */
	public SmrvDetailReport(Connection conn, Integer divisionId) throws Exception {
		
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		startDate = (Calendar)Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);
		
		logger.log(Level.DEBUG, "\t" + "1 startDate:" + startDate.getTime() + "\t" + "Division:" + divisionId );
		this.data = makeData(conn, divisionId, startDate);
		String subtitle = "month" + " through " + "month";
		logger.log(Level.DEBUG, "\t" + "1 startDate:" + startDate.getTime() + "\t" + "Division:" + divisionId + "\t" + "Data:" + data );
		makeReport(div, startDate, data, subtitle);

	}

	/**
	 * Create a 6-Month Rolling Volume report object for a single 6-month period
	 * @param conn Database Connection
	 * @param divisionId Division Filter
	 * @param month Month of the year (1-12)
	 * @param year 4-digit year (eg 2017, not 17)
	 * @return Report Object
	 * @throws Exception something bad happened
	 */
	public SmrvDetailReport(Connection conn, Integer divisionId, Integer month, Integer year) throws Exception {
		this.div = makeDivision(conn, divisionId);
		Integer startMonth = month - 1;  // because java calendars start with 0
		Calendar startDate = new GregorianCalendar(year, startMonth, 1);
		this.startDate = startDate;
//		logger.log(Level.DEBUG, "\t" + "2 startDate:" + startDate.getTime() + "\t" + "Division:" + divisionId );
		this.data = makeData(conn, divisionId, startDate);
//		logger.log(Level.DEBUG, "\t" + "2 startDate:" + startDate.getTime() + "\t" + "Division:" + divisionId + "\t" + "Data:" + data );
		String subtitle = "month" + " through " + "month";
		makeReport(div, startDate, data, subtitle);
	}
	
	public SmrvDetailReport(Connection conn, Integer divisionId, Calendar startDate) throws Exception {
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		this.div = makeDivision(conn, divisionId);
		this.startDate = startDate;
		logger.log(Level.DEBUG, "\t" + "3 startDate:" + startDate.getTime() + "\t" + "Division:" + divisionId );
		this.data = makeData(conn, divisionId, startDate);
		logger.log(Level.DEBUG, "\t" + "3 startDate:" + startDate.getTime() + "\t" + "Division:" + divisionId + "\t" + "Data:" + data );
		String subtitle = "month" + " through " + "month";
		makeReport(div, startDate, data, subtitle);
	}
	
	
	public String getDiv() {
		return div;
	}

	public String getHeaderRange() {
		return headerRange;
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

	public Integer makeDataSize() {
		return this.data.size();
	}
	
	public Integer getJobCount() {
		return this.jobCount;
	}
	
	public Integer getContractCount() {
		return this.contractCount;
	}
	
	private String makeDivision(Connection conn, Integer divisionId) throws Exception {
		Division division = new Division();
		division.setDivisionId(divisionId);
		division.selectOne(conn);
		return division.getDivisionNbr() + "-" + division.getDivisionCode();
	}

	private List<RowData> makeData(Connection conn, Integer divisionId, Calendar startDate) throws Exception {
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);

		SimpleDateFormat row0DateFormatter = new SimpleDateFormat("MMMM");
		this.startMonth = row0DateFormatter.format(this.startDate.getTime());
		Calendar endDate = (Calendar)startDate.clone();
		endDate.add(Calendar.MONTH, 5);
		this.endMonth = row0DateFormatter.format(endDate.getTime());
		this.headerRange = this.startMonth + " through " + this.endMonth;
		endDate.add(Calendar.MONTH, 1);

		Integer queryMonth = startDate.get(Calendar.MONTH) + 1; // add 1 because January is 0;
		Integer queryYear = startDate.get(Calendar.YEAR);
		Double[] jobMonthlyTotal = new Double[] {0.0D,0.0D,0.0D,0.0D,0.0D,0.0D};
		Integer[] jobQueryMonth = new Integer[] {0,0,0,0,0,0};
		Integer[] jobQueryYear = new Integer[] {0,0,0,0,0,0};
		Calendar jobStartDate = Calendar.getInstance(new AnsiTime());
//		logger.log(Level.DEBUG, "\t" + "startDate:" + startDate.getTime() + "\t" + "endDate:" + endDate.getTime());
		
		PreparedStatement ps = conn.prepareStatement(sql);
		
		int n = 1;		
		ps.setString(n, TicketStatus.COMPLETED.code());
		n++;
		ps.setString(n, TicketStatus.INVOICED.code());
		n++;
		ps.setString(n, TicketStatus.PAID.code());
		n++;
		
		ps.setString(n, TicketType.JOB.code());
		n++;
		ps.setString(n, TicketType.RUN.code());
		n++;
		
		for ( int i = 0; i < 6; i++ ) {
			jobQueryMonth[i] = queryMonth;
			jobQueryYear[i] = queryYear;
//			logger.log(Level.DEBUG, "\t"+ i + "\t" + "jobQueryYear:" + jobQueryYear[i] + "\t" + "jobQueryMonth:" + jobQueryMonth[i]);
			queryMonth++;
			if ( queryMonth > 12 ) {
				queryMonth = 1;
				queryYear++;
			}
		}
		ps.setInt(n, divisionId);
		n++;
		ps.setString(n, JobStatus.ACTIVE.code());
		n++;
		
		ResultSet rs = ps.executeQuery();
		
		List<String> jobSiteNameList = new ArrayList<String>();
		List<RowData> data = new ArrayList<RowData>();
		while ( rs.next() ) {
			RowData dataRow = new RowData(rs);
			data.add(dataRow);
//			logger.log(Level.DEBUG, "\t" + "jobId:" + dataRow.jobId + "\t" + "jobFrequency:" + dataRow.jobFrequency);
			this.jobCount++;
			if (! jobSiteNameList.contains(dataRow.jobSiteName)) {
				jobSiteNameList.add(dataRow.jobSiteName);
			}
			jobStartDate.setTime(dataRow.jobStartDate);
			
			List<Calendar> ticketDates = TicketDateGenerator.generateTicketDates(dataRow.jobFrequency, jobStartDate, endDate);
			Integer ticketMonth = 0; 
			Integer ticketYear = 0;
			for ( Calendar ticketDate : ticketDates ) {
//				logger.log(Level.DEBUG, "\tticketDate:"+ ticketDate.getTime());
				ticketMonth = ticketDate.get(Calendar.MONTH) + 1; // add 1 because January is 0;
				ticketYear = ticketDate.get(Calendar.YEAR);
//				logger.log(Level.DEBUG, "\t" + "\t" + "ticketYear:" + ticketYear + "\t" + "ticketMonth:" + ticketMonth);
//				logger.log(Level.DEBUG, "\t" + "\t" + "PPC:" + dataRow.jobPpc );
				for ( int i = 0; i < 6; i++ ) {
//					logger.log(Level.DEBUG, "\t"+ i + "\t" + "jobQueryYear:" + jobQueryYear[i] + "\t" + "jobQueryMonth:" + jobQueryMonth[i]);
					if ( ticketMonth.equals(jobQueryMonth[i]) && ticketYear.equals(jobQueryYear[i]) ) {
						this.monthlyTotal[i] = dataRow.jobPpc == null ? this.monthlyTotal[i] : this.monthlyTotal[i] + dataRow.jobPpc.doubleValue();
						dataRow.jobMonthlyTotal[i] = dataRow.jobPpc == null ? dataRow.jobMonthlyTotal[i] : dataRow.jobMonthlyTotal[i] + dataRow.jobPpc.doubleValue();
//						logger.log(Level.DEBUG, "\tmonthlyTotal[i]:"+ this.monthlyTotal[i]);
					}
				}
				dataRow.ppcm01 = new BigDecimal( dataRow.jobMonthlyTotal[0]);
				dataRow.ppcm02 = new BigDecimal( dataRow.jobMonthlyTotal[1]);
				dataRow.ppcm03 = new BigDecimal( dataRow.jobMonthlyTotal[2]);
				dataRow.ppcq01 = BigDecimal.ZERO;
				dataRow.ppcq01.add(dataRow.ppcm01);
				dataRow.ppcq01.add(dataRow.ppcm02);
				dataRow.ppcq01.add(dataRow.ppcm03);
				dataRow.ppcm04 = new BigDecimal( dataRow.jobMonthlyTotal[3]);
				dataRow.ppcm05 = new BigDecimal( dataRow.jobMonthlyTotal[4]);
				dataRow.ppcm06 = new BigDecimal( dataRow.jobMonthlyTotal[5]);
				dataRow.ppcq02 = BigDecimal.ZERO;
				dataRow.ppcq02.add(dataRow.ppcm04);
				dataRow.ppcq02.add(dataRow.ppcm05);
				dataRow.ppcq02.add(dataRow.ppcm06);
				dataRow.ppch01 = BigDecimal.ZERO;
				dataRow.ppch01.add(dataRow.ppcq01);
				dataRow.ppch01.add(dataRow.ppcq02);

			}
		}
		this.contractCount = jobSiteNameList.size();
		rs.close();
		return data;
	}


	@SuppressWarnings("unchecked")	
	private void makeReport(String div, Calendar startDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(this.REPORT_TITLE);	
		super.setSubtitle(this.headerRange);
//		super.setHeaderNotes(REPORT_NOTES);
		
		SimpleDateFormat columnDateHeaderFormat = new SimpleDateFormat("MM/yyyy");
		List<String> dateHeaderList = new ArrayList<String>();
		for ( int i = 0; i < 6; i++ ) {
			Calendar columnDate = (Calendar)startDate.clone();
			columnDate.add(Calendar.MONTH, i);
			String columnDateHeader = columnDateHeaderFormat.format(columnDate.getTime());
			dateHeaderList.add(columnDateHeader);
		}
		
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("jobSiteName", "Building Name", 2, DataFormats.STRING_FORMAT, SummaryType.COUNT_DISTINCT),
				new ColumnHeader("zip", "Zip", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("address1", "Street 1", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobNbr", "Job#", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("lastRun", "Last Run", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobId", "Job", 1, DataFormats.NUMBER_FORMAT, SummaryType.COUNT),
				new ColumnHeader("freq","Freq", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("ppcm01",dateHeaderList.get(0), 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM),
				new ColumnHeader("ppcm02",dateHeaderList.get(1), 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM),
				new ColumnHeader("ppcm03",dateHeaderList.get(2), 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM),
				new ColumnHeader("ppcm04",dateHeaderList.get(3), 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM),
				new ColumnHeader("ppcm05",dateHeaderList.get(4), 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM),
				new ColumnHeader("ppcm06",dateHeaderList.get(5), 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM),
//				new ColumnHeader("ppch01","Totals", 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM),
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		Method getJobCountMethod = this.getClass().getMethod("getJobCount", (Class<?>[])null);
		Method getContractCountMethod = this.getClass().getMethod("getContractCount", (Class<?>[])null);
		Method getHeaderRangeMethod = this.getClass().getMethod("getHeaderRange", (Class<?>[])null);
//		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
//		Method dataSizeMethod = this.getClass().getMethod("makeDataSize", (Class<?>[])null);
//		Method getTotalPpc = this.getClass().getMethod("getTotalPpc", (Class<?>[])null);
//		Method getTotalVolume = this.getClass().getMethod("getTotalVolume", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
//				new ReportHeaderRow("Total PPC:", getStartDateMethod, 0, DataFormats.DATE_FORMAT),
//				new ReportHeaderRow("Total Volume:", getStartDateMethod, 1, DataFormats.DATE_FORMAT),
//				new ReportHeaderRow("Jobs:", getStartDateMethod, 2, DataFormats.DATE_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		
		Method getDivMethod = this.getClass().getMethod("getDiv", (Class<?>[])null);
//		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
//		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
		
		
		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Division:", getDivMethod, 1, DataFormats.STRING_FORMAT),
				new ReportHeaderRow("Jobs:", getJobCountMethod, 2, DataFormats.INTEGER_FORMAT),
				new ReportHeaderRow("Contracts:", getContractCountMethod, 3, DataFormats.INTEGER_FORMAT)
		});
		super.makeHeaderRight(headerRight);

		super.setColumnWidths(new Integer[] {
				ColumnWidth.ADDRESS_NAME.width()-ColumnWidth.DATETIME.width()-ColumnWidth.DATE.width(),
				ColumnWidth.DATETIME.width(),
				(Integer)null,
				ColumnWidth.ADDRESS_ADDRESS1.width()-ColumnWidth.DATE.width(),
				ColumnWidth.JOB_JOB_NBR.width(),
				ColumnWidth.DATE.width(),
				(Integer)null,
				ColumnWidth.JOB_JOB_FREQUENCY.width(),
				(Integer)null,
				(Integer)null,
				(Integer)null,
				(Integer)null,
				(Integer)null,
				(Integer)null,
//				(Integer)null,
		});
}
	
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		
		private String jobSiteName;
		private String zip;
		private String address1;
		private Integer jobId;
		private Integer jobNbr;
		private JobFrequency jobFrequency;
		private BigDecimal jobPpc;
		private Date jobStartDate;
		private Date lastRun;
		private BigDecimal ppcm01;
		private BigDecimal ppcm02;
		private BigDecimal ppcm03;
		private BigDecimal ppcq01;
		private BigDecimal ppcm04;
		private BigDecimal ppcm05;
		private BigDecimal ppcm06;
		private BigDecimal ppcq02;
		private BigDecimal ppch01;
		private Double[] jobMonthlyTotal = new Double[] {0.0D,0.0D,0.0D,0.0D,0.0D,0.0D};

		
		
		public RowData(ResultSet rs) throws SQLException {
			this.jobSiteName = rs.getString("job_site_name");
			this.zip = rs.getString("zip");
			this.address1 = rs.getString("address1");
			this.jobId = rs.getInt("job_id");
			this.jobNbr = rs.getInt("job_nbr");
			this.jobFrequency = JobFrequency.get(rs.getString("job_frequency"));
			this.jobPpc = rs.getBigDecimal("price_per_cleaning");
			java.sql.Date lastRunDate = rs.getDate("last_run");
			if ( lastRunDate != null ) {
				this.lastRun = new Date(lastRunDate.getTime());
			}
			java.sql.Date jobStartDate = rs.getDate("start_date");
			if ( jobStartDate != null ) {
				this.jobStartDate = new Date(jobStartDate.getTime());
			}
		}


		public String getJobSiteName() {
			return jobSiteName;
		}
		public String getZip() {
			return zip;
		}
		public String getAddress1() {
			return address1;
		}
		public Integer getJobNbr() {
			return jobNbr;
		}
		public Integer getJobId() {
			return jobId;
		}


		public JobFrequency getJobFrequency() {
			return jobFrequency;
		}
		public String getFreq() {
			return jobFrequency.abbrev();
		}
		public BigDecimal getJobPpc() {
			return jobPpc;
		}
		public Date getJobStartDate() {
			return jobStartDate;
		}


		public Date getLastRun() {
			return lastRun;
		}
		public BigDecimal getPpcm01() {
			return ppcm01;
		}
		public BigDecimal getPpcm02() {
			return ppcm02;
		}
		public BigDecimal getPpcm03() {
			return ppcm03;
		}
		public BigDecimal getPpcq01() {
			return ppcq01;
		}
		public BigDecimal getPpcm04() {
			return ppcm04;
		}
		public BigDecimal getPpcm05() {
			return ppcm05;
		}
		public BigDecimal getPpcm06() {
			return ppcm06;
		}
		public BigDecimal getPpcq02() {
			return ppcq02;
		}
		public BigDecimal getPpch01() {
			return ppch01;
		}
	}
	
	
}
