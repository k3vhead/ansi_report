package com.ansi.scilla.report.sixMonthRollingVolumeSummary;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.jobticket.JobFrequency;
import com.ansi.scilla.common.jobticket.JobStatus;
import com.ansi.scilla.common.jobticket.TicketStatus;
import com.ansi.scilla.common.jobticket.TicketType;
import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.XLSBuilder;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivMonthYear;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivision;

public class SixMonthRollingVolumeSummary extends StandardReport implements ReportByDivMonthYear, ReportByDivision {

	/**
	 * @author Joshua Lewis
	 */
	private static final long serialVersionUID = 1L;
	public static final String REPORT_TITLE = "Six Month Rolling Volume Summary";
	public static final String FILENAME = "SMRV Summary";
	
	private final String sql = "";
	
	private Double marginTop = XLSBuilder.marginTopDefault;
	private Double marginBottom = XLSBuilder.marginBottomDefault;
	private Double marginLeft = XLSBuilder.marginLeftDefault;
	private Double marginRight = XLSBuilder.marginRightDefault;

	private final String[] colHeaders = new String[] {"Building Name", "Zipcode", "Street 1", "Job #", "Last Run", "Job ID", "Freq"};
	
	private String div;
	private Calendar startDate;
	private List<Object> dataRows;
	private int jobCount = 0;   // total row count
	private int contractCount = 0;   // distinct job sites
	private Double[] monthlyTotal = new Double[] {0.0D,0.0D,0.0D,0.0D,0.0D,0.0D};

	public static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S");
	private Logger logger;
	
	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	
	
	private void makeData(PreparedStatement ps, Integer divisionId, Calendar startDate) throws Exception {
		Integer queryMonth = startDate.get(Calendar.MONTH) + 1; // add 1 because January is 0;
		Integer queryYear = startDate.get(Calendar.YEAR);
		
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
			ps.setInt(n, queryYear);
			n++;
			ps.setInt(n, queryMonth);
			n++;
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
		while ( rs.next() ) {
			DataRow dataRow = new DataRow(rs);
			addDataRow(dataRow);
			this.jobCount++;
			if (! jobSiteNameList.contains(dataRow.jobSiteName)) {
				jobSiteNameList.add(dataRow.jobSiteName);
			}
			
			this.monthlyTotal[0] = dataRow.ppcm01 == null ? this.monthlyTotal[0] : this.monthlyTotal[0] + dataRow.ppcm01.doubleValue();
			this.monthlyTotal[1] = dataRow.ppcm02 == null ? this.monthlyTotal[1] : this.monthlyTotal[1] + dataRow.ppcm02.doubleValue();
			this.monthlyTotal[2] = dataRow.ppcm03 == null ? this.monthlyTotal[2] : this.monthlyTotal[2] + dataRow.ppcm03.doubleValue();
			this.monthlyTotal[3] = dataRow.ppcm04 == null ? this.monthlyTotal[3] : this.monthlyTotal[3] + dataRow.ppcm04.doubleValue();
			this.monthlyTotal[4] = dataRow.ppcm05 == null ? this.monthlyTotal[4] : this.monthlyTotal[4] + dataRow.ppcm05.doubleValue();
			this.monthlyTotal[5] = dataRow.ppcm06 == null ? this.monthlyTotal[5] : this.monthlyTotal[5] + dataRow.ppcm06.doubleValue();
			
		}
		this.contractCount = jobSiteNameList.size();
		rs.close();
	}
	
	public class DataRow extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		
		private String jobSiteName;
		private String zip;
		private String address1;
		private Integer jobId;
		private Integer jobNbr;
		private JobFrequency jobFrequency;
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
		
		
		public DataRow(ResultSet rs) throws SQLException {
			this.jobSiteName = rs.getString("job_site_name");
			this.zip = rs.getString("zip");
			this.address1 = rs.getString("address1");
			this.jobId = rs.getInt("job_id");
			this.jobNbr = rs.getInt("job_nbr");
			this.jobFrequency = JobFrequency.get(rs.getString("job_frequency"));
			java.sql.Date lastRunDate = rs.getDate("last_run");
			if ( lastRunDate != null ) {
				this.lastRun = new Date(lastRunDate.getTime());
			}
			this.ppcm01 = rs.getBigDecimal("ppcm01");
			this.ppcm02 = rs.getBigDecimal("ppcm02");
			this.ppcm03 = rs.getBigDecimal("ppcm03");
			this.ppcq01 = rs.getBigDecimal("ppcq01");
			this.ppcm04 = rs.getBigDecimal("ppcm04");
			this.ppcm05 = rs.getBigDecimal("ppcm05");
			this.ppcm06 = rs.getBigDecimal("ppcm06");
			this.ppcq02 = rs.getBigDecimal("ppcq02");
			this.ppch01 = rs.getBigDecimal("ppch01");
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
