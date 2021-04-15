package com.ansi.scilla.report.monthlyServiceTaxReport;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.Midnight;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.utils.ObjectTransformer;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.ReportStartLoc;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;

public class MonthlyServiceTaxReport extends StandardReport implements ReportByStartEnd {
	
	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "Monthly Service Tax";
	
	public static final  String REPORT_TITLE = "Monthly Service Tax Report";
	
	protected static final String sql = "select division_group.name as company, job_site.state, job_site.county, tax_rate.location, tax_rate.rate, sum(ticket_payment.tax_amt) as taxes\n" + 
			"from ticket\n" + 
			"join ticket_payment on ticket.ticket_id = ticket_payment.ticket_id\n" + 
			"join payment on payment.payment_id = ticket_payment.payment_id and payment.payment_date >= ? and payment.payment_date < ?\n" + 
			"join division on division.division_id = ticket.act_division_id\n" + 
			"join job on job.job_id = ticket.job_id\n" + 
			"join quote on quote.quote_id = job.quote_id\n" + 
			"join address job_site on job_site.address_id = quote.job_site_address_id\n" + 
			"join tax_rate on tax_rate.tax_rate_id = ticket.act_tax_rate_id\n" + 
			"join division_group on division_group.group_id = division.group_id and group_type = 'COMPANY'\n" + 
			"group by division_group.name, state, county, tax_rate.location, tax_rate.rate\n" + 
			"having sum(ticket_payment.tax_amt) <> 0.00\n" +
			"order by division_group.name asc, job_site.state asc, job_site.county asc, tax_rate.location asc";
	
	protected MonthlyServiceTaxReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
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
	
	private Calendar startDate;
	private Calendar endDate;
	private Integer lastDivision;
	
	private List<RowData> data;
	
	Logger logger = LogManager.getLogger(this.getClass());
	
	public MonthlyServiceTaxReport() {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);

	}
	
	protected MonthlyServiceTaxReport(Connection conn) throws Exception {
		this();
		logger.log(Level.DEBUG, "constructor1");
		DateFormatter dateFormatter = (DateFormatter)DataFormats.DATE_FORMAT.formatter();
		startDate = (Calendar)Midnight.getInstance(new AnsiTime());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.MONTH, -1);
		
		endDate = (Calendar)Calendar.getInstance(new AnsiTime());
		
		this.data = makeData(conn, this, startDate, endDate);

		String startTitle = dateFormatter.format(startDate.getTime());
		String endTitle = dateFormatter.format(endDate.getTime());
		String subtitle = startTitle + " through " + endTitle;
		makeReport(startDate, endDate, data, subtitle);
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

	public Integer getLastDivision() {
		return lastDivision;
	}

	public void setLastDivision(Integer lastDivision) {
		this.lastDivision = lastDivision;
	}

	public Integer makeDataSize() {
		return this.data.size();
	}
	
	private List<RowData> makeData(Connection conn, MonthlyServiceTaxReport report, Calendar startDate, Calendar endDate) throws Exception {
		
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		
		List<RowData> data = new ArrayList<RowData>();
		RowData newRow;
		while ( rs.next() ) {
			newRow = new RowData(rs);
			data.add(newRow);
		}
		rs.close();
		
		return data;
	}


	private void makeReport(Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		
		ColumnHeader taxes = new ColumnHeader("taxes", "Sum of Taxes", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM);
		taxes.setSubTotalTrigger("state");
		
		
		super.setHeaderRow(new ColumnHeader[] {

				new ColumnHeader("company", "Company", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("state", "State", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("county", "County", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("location","Location", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("rate", "Rate", 1, DataFormats.PCT_FORMAT, SummaryType.NONE),
				taxes
				
		});
		
		List<Object> oData = (List<Object>)CollectionUtils.collect(data, new ObjectTransformer());
		super.setDataRows(oData);
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT),
		});
		super.makeHeaderLeft(headerLeft);
		
		Method getStartDateMethod = this.getClass().getMethod("getStartDate", (Class<?>[])null);
		Method getEndDateMethod = this.getClass().getMethod("getEndDate", (Class<?>[])null);
		
		
		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {
				new ReportHeaderRow("From:", getStartDateMethod, 2, DataFormats.DATE_FORMAT),
				new ReportHeaderRow("To:", getEndDateMethod, 3, DataFormats.DATE_FORMAT)
		});
		super.makeHeaderRight(headerRight);
		
		super.setColumnWidths(new ColumnWidth[] {
				new ColumnWidth(3500, 70.0F),
				new ColumnWidth(4000, 30.0F),
				new ColumnWidth(3000, 70.0F),
				new ColumnWidth(10000, (Float)null),
				new ColumnWidth(2000, 30.0F),
				new ColumnWidth(2250, 50.0F),
		});
		

	}
	
	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		XSSFSheet sheet = workbook.createSheet();
		XLSBuilder.build(this, sheet, new ReportStartLoc(0, 0));
	}
	
	public static MonthlyServiceTaxReport buildReport(Connection conn) throws Exception {
		return new MonthlyServiceTaxReport(conn);
	}
	


	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		public String company;
		public String state;
		public String county;
		public String location;
		public Double rate;
		public Double taxes;
	
		public RowData(ResultSet rs) throws SQLException {
			super();
			this.company = rs.getString("company");
			this.state = rs.getString("state");
			this.county = rs.getString("county");
			this.location = rs.getString("location");
			this.rate = rs.getDouble("rate");
			this.taxes = rs.getDouble("taxes");
			
		}

		
		public Double getTaxes() {
			return taxes;
		}

		

		public String getCompany() {
			return company;
		}

		
		public String getState() {
			return state;
		}

		
		public String getCounty() {
			return county;
		}

		

		public String getLocation() {
			return location;
		}

		

		public Double getRate() {
			return rate;
		}

		

		
		
	}
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}
	

	public static MonthlyServiceTaxReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new MonthlyServiceTaxReport(conn, startDate, endDate);
	}
	
}
