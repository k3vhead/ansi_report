package com.ansi.scilla.report.pac;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.jobticket.JobFrequency;
import com.ansi.scilla.common.jobticket.JobStatus;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.DataFormats;
import com.ansi.scilla.report.reportBuilder.DateFormatter;
import com.ansi.scilla.report.reportBuilder.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.SummaryType;

public class PacSummaryReport extends StandardReport {

	private static final long serialVersionUID = 1L;

//	private final String someSQL = "select "
//			+ " 'Proposed' as job_status, "
//			+ " count(*) as job_count, "
//			+ " isnull(sum(job.price_per_cleaning),'0.00') as price_per_cleaning, "
//			+ JobFrequency.makeVolumeCalculationSQL("job.job_frequency", "job.price_per_cleaning", "volume")
//			+ " from job "
//			+ " join quote on quote.quote_id = job.quote_id "
//			+ " join division on division.division_id = job.division_id "
//
//			+ " where job.division_id = ? "
//			+ " and quote.proposal_date >= ? "
//			+ " and quote.proposal_date <= ? "
//			+ " and job.job_status in ('" + JobStatus.PROPOSED.code() +"','" + JobStatus.ACTIVE.code() + "','"+ JobStatus.CANCELED.code()+"')";
	
	
	private final String proposedSql = "select "
			+ " 'Proposed' as job_status, "
			+ " count(*) as job_count, "
			+ " isnull(sum(job.price_per_cleaning),'0.00') as price_per_cleaning, "
			+ JobFrequency.makeVolumeCalculationSQL("job.job_frequency", "job.price_per_cleaning", "volume")

			+ " from job "
			+ " join quote on quote.quote_id = job.quote_id "
			+ " join division on division.division_id = job.division_id "

			+ " where job.division_id = ? "
			+ " and quote.proposal_date >= ? "
			+ " and quote.proposal_date <= ? "
			+ " and job.job_status in ('" + JobStatus.PROPOSED.code() +"','" + JobStatus.ACTIVE.code() + "','"+ JobStatus.CANCELED.code()+"')";
	
	private final String activatedSql = "select "
			+ " 'Activated' as job_status, "
			+ " count(*) as job_count, "
			+ " isnull(sum(job.price_per_cleaning),'0.00') as price_per_cleaning, "
			+ JobFrequency.makeVolumeCalculationSQL("job.job_frequency", "job.price_per_cleaning", "volume")

			+ " from job "
			+ " join quote on quote.quote_id = job.quote_id "
			+ " join division on division.division_id = job.division_id "

			+ " where job.division_id = ? "
			+ " and job.activation_date >= ? "
			+ " and job.activation_date <= ? "
			+ " and job.job_status in ('" + JobStatus.PROPOSED.code() +"','" + JobStatus.ACTIVE.code() + "','"+ JobStatus.CANCELED.code()+"')";
	
	private final String canceledSql = "select "
			+ " 'Canceled' as job_status, "
			+ " count(*) as job_count, "
			+ " isnull(sum(job.price_per_cleaning),'0.00') as price_per_cleaning, "
			+ JobFrequency.makeVolumeCalculationSQL("job.job_frequency", "job.price_per_cleaning", "volume")

			+ " from job "
			+ " join quote on quote.quote_id = job.quote_id "
			+ " join division on division.division_id = job.division_id "

			+ " where job.division_id = ? "
			+ " and job.cancel_date >= ? "
			+ " and job.cancel_date <= ? "
			+ " and job.job_status in ('" + JobStatus.PROPOSED.code() +"','" + JobStatus.ACTIVE.code() + "','"+ JobStatus.CANCELED.code()+"')";
	
	private final String netSql = "select top 1 "
			+ " 'Net' as job_status, "
			+ " '0' as job_count, "
			+ " '0.00' as price_per_cleaning, "
			+ " '0.00' as Volume "

			+ " from job "
			+ " join quote on quote.quote_id = job.quote_id "
			+ " join division on division.division_id = job.division_id "

			+ " where job.division_id = ? "
			+ " and job.cancel_date >= ? "
			+ " and job.cancel_date <= ? "
			+ " and job.job_status in ('" + JobStatus.PROPOSED.code() +"','" + JobStatus.ACTIVE.code() + "','"+ JobStatus.CANCELED.code()+"')";
	
	public static final  String REPORT_TITLE = "PAC Summary";
//	private final String REPORT_NOTES = "notes go here";
	
	private String div;
	private Calendar startDate;
	private Calendar endDate;
	private Double totalPpc;
	private Double totalVolume;
	private List<RowData> data;
	
	protected PacSummaryReport() {
		super();
		this.setTitle(REPORT_TITLE);
	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database connection
	 * @param divisionId Division filter
	 * @throws Exception something bad happened
	 */
	public PacSummaryReport(Connection conn,  Integer divisionId) throws Exception {
		super();
		this.div = makeDivision(conn, divisionId);
		
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		startDate = Calendar.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate = Calendar.getInstance(new AnsiTime());
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		
		this.totalPpc = 0.00;
		this.totalVolume = 0.00;
		this.data = makeData(conn, this, divisionId, startDate, endDate);

		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(div, startDate, endDate, data, subtitle);
	}

	public PacSummaryReport(Connection conn,  Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		super();
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

	public Integer makeDataSize() {
		return this.data.size();
	}
	
	private String makeDivision(Connection conn, Integer divisionId) throws Exception {
		Division division = new Division();
		division.setDivisionId(divisionId);
		division.selectOne(conn);
		return division.getDivisionNbr() + "-" + division.getDivisionCode();
	}

	private List<RowData> makeData(Connection conn, PacSummaryReport report, Integer divisionId, Calendar startDate, Calendar endDate) throws Exception {
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		
		List<RowData> data = new ArrayList<RowData>();

		PreparedStatement ps;
		ResultSet rs;
		
		ps = conn.prepareStatement(proposedSql);
		ps.setInt(1, divisionId);
		ps.setDate(2, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(3, new java.sql.Date(endDate.getTimeInMillis()));
		rs = ps.executeQuery();
		
		while ( rs.next() ) {
			data.add(new RowData(rs, report));
		}
		rs.close();
		
		ps = conn.prepareStatement(activatedSql);
		ps.setInt(1, divisionId);
		ps.setDate(2, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(3, new java.sql.Date(endDate.getTimeInMillis()));
		rs = ps.executeQuery();
		
		while ( rs.next() ) {
			data.add(new RowData(rs, report));
		}
		rs.close();
		
		ps = conn.prepareStatement(canceledSql);
		ps.setInt(1, divisionId);
		ps.setDate(2, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(3, new java.sql.Date(endDate.getTimeInMillis()));
		rs = ps.executeQuery();
		
		while ( rs.next() ) {
			data.add(new RowData(rs, report));
		}
		rs.close();
		
		ps = conn.prepareStatement(netSql);
		ps.setInt(1, divisionId);
		ps.setDate(2, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(3, new java.sql.Date(endDate.getTimeInMillis()));
		rs = ps.executeQuery();
		
		while ( rs.next() ) {
			data.add(new RowData(rs, report));
		}
		rs.close();
		
		return data;
	}

	public Double getTotalPpc() {
		return this.totalPpc;
	}
	
	public Double getTotalVolume() {
		return this.totalVolume;
	}
	
	@SuppressWarnings("unchecked")	
	private void makeReport(String div, Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		
		super.setHeaderRow(new ColumnHeader[] {
				new ColumnHeader("jobStatus", "", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobCount", "Jobs", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("pricePerCleaning","PPC", 1, DataFormats.CURRENCY_FORMAT, SummaryType.NONE),
				new ColumnHeader("volume","Volume", 1, DataFormats.CURRENCY_FORMAT, SummaryType.NONE),
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		Method dataSizeMethod = this.getClass().getMethod("makeDataSize", (Class<?>[])null);
		Method getTotalPpc = this.getClass().getMethod("getTotalPpc", (Class<?>[])null);
		Method getTotalVolume = this.getClass().getMethod("getTotalVolume", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT)
//				, new ReportHeaderRow("Total PPC:", getTotalPpc, 0, DataFormats.CURRENCY_FORMAT)
//				, new ReportHeaderRow("Total Volume:", getTotalVolume, 1, DataFormats.CURRENCY_FORMAT)
//				, new ReportHeaderRow("Jobs:", dataSizeMethod, 2, DataFormats.INTEGER_FORMAT)
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
	}
	
	
	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		public String jobStatus;
		public Integer jobCount;
		public Double pricePerCleaning;
		public Double volume;
		
		public RowData(ResultSet rs, PacSummaryReport report) throws SQLException {
			this.jobStatus = rs.getString("job_status");
			this.jobCount = rs.getInt("job_count");
			if (this.jobStatus.equals("Net")) {
				this.pricePerCleaning = report.totalPpc;
				this.volume = report.totalVolume;
			} else {
				this.pricePerCleaning = rs.getBigDecimal("price_per_cleaning").doubleValue();
				this.volume = rs.getBigDecimal("volume").doubleValue();
			}
			if (this.jobStatus.equals("Activated")) {
				report.totalPpc = report.totalPpc + this.pricePerCleaning;
				report.totalVolume = report.totalVolume + this.volume;
			}
			if (this.jobStatus.equals("Canceled")) {
				report.totalPpc = report.totalPpc - this.pricePerCleaning;
				report.totalVolume = report.totalVolume - this.volume;
			}
		}

		public String getJobStatus() {
			return jobStatus;
		}
		public Integer getJobCount() {
			return jobCount;
		}
		public Double getPricePerCleaning() {
			return pricePerCleaning;
		}
		public Double getVolume() {
			return volume;
		}

	}
}
