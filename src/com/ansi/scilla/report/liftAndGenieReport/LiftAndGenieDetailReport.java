package com.ansi.scilla.report.liftAndGenieReport;

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
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.ReportStartLoc;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;

public class LiftAndGenieDetailReport extends StandardReport implements ReportByDivStartEnd {

	private static final long serialVersionUID = 1L;

	private final String sql = "select concat(division_nbr,'-',division_code) as div\n" + 
			", ticket.ticket_id as job\n" + 
			", job.service_description as 'service_description'\n" + 
			", job.equipment as 'job_equipment'\n" + 
			", act_dl_amt as 'direct_labor'\n" + 
			", process_date as 'completed_date'\n" + 
			", bill_to.name as 'client_name'\n" + 
			"\n" + 
			"from division\n" + 
			"	join ticket on division.division_id = act_division_id\n" + 
			"	join job on job.job_id = ticket.job_id\n" + 
			"	join quote on quote.quote_id = job.quote_id\n" + 
			"	join address bill_to on bill_to.address_id = bill_to_address_id\n" + 
			"where (service_description like '%lift%'\n" + 
			"	or service_description like '%genie%'\n" + 
			"	or equipment like '%lift%'\n" + 
			"	or equipment like '%genie%')\n" + 
			"	and ticket_status in ('c','i','p')\n" + 
			"	and ticket_type in ('run','job')\n" + 
			"	and process_date >= ? and process_date < ?\n" +  
			"order by division_nbr, bill_to.name, ticket.ticket_id" ;
			

	
	public static final String REPORT_TITLE = "Lift And Genie Detail";
	public static final String FILENAME = "LiftAndGenieDetail";
//	private final String REPORT_NOTES = "notes go here";
	
	private Calendar startDate;
	private Calendar endDate;
	private Integer lastDivision;
	
	private List<RowData> data;
	
	Logger logger = LogManager.getLogger(this.getClass());
	
	public LiftAndGenieDetailReport() {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);

	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database exception
	 * @throws Exception something bad happened
	 */
	protected LiftAndGenieDetailReport(Connection conn) throws Exception {
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

	protected LiftAndGenieDetailReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
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
	
	private List<RowData> makeData(Connection conn, LiftAndGenieDetailReport report, Calendar startDate, Calendar endDate) throws Exception {
		
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
			newRow = new RowData(rs, report);
			data.add(newRow);
		}
		rs.close();
		
		return data;
	}

	@SuppressWarnings("unchecked")	
	private void makeReport(Calendar startDate, Calendar endDate, List<RowData> data, String subtitle) throws NoSuchMethodException, SecurityException {

		super.setTitle(REPORT_TITLE);	
		super.setSubtitle(subtitle);
//		super.setHeaderNotes(REPORT_NOTES);
		
		super.setHeaderRow(new ColumnHeader[] {

				new ColumnHeader("div", "Div", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("job", "Job", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("serviceDescription","Service Description", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobEquipment", "Job Equipment", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("directLabor", "Direct Labor", 1, DataFormats.DECIMAL_FORMAT, SummaryType.NONE),
				new ColumnHeader("completedDate", "Completed Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("clientName","Client Name", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),

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
		
//		super.setColumnWidths(new Integer[] {
//				(Integer)null,
//				ColumnWidth.DATETIME.width(),
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				(Integer)null,
//				Math.max(0, ColumnWidth.ADDRESS_NAME.width() - ColumnWidth.DATE.width()),
//		});

	}
	
	public void makeXLS(XSSFWorkbook workbook) throws Exception {
		XSSFSheet sheet = workbook.createSheet();
		XLSBuilder.build(this, sheet, new ReportStartLoc(0, 0));
	}
	
	public static LiftAndGenieDetailReport buildReport(Connection conn) throws Exception {
		return new LiftAndGenieDetailReport(conn);
	}
	public static LiftAndGenieDetailReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new LiftAndGenieDetailReport(conn, startDate, endDate);
	}


	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		public String div;
		public Integer job;
		public String serviceDescription;
		public String jobEquipment;
		public Double directLabor;
		public Date completedDate;
		public String clientName;
	
		public RowData(ResultSet rs, LiftAndGenieDetailReport report) throws SQLException {
			this.div = rs.getString("div");
			this.job = rs.getInt("job");
			this.serviceDescription = rs.getString("service_description");
			this.jobEquipment = rs.getString("job_equipment");
			this.directLabor = rs.getDouble("direct_labor");
			this.completedDate = rs.getDate("completed_date");
			this.clientName = rs.getString("client_name");

		}

		public RowData() {
			super();
		}

		public String getDiv() {
			return div;
		}

		public void setDiv(String div) {
			this.div = div;
		}

		public Integer getJob() {
			return job;
		}

		public void setJob(Integer job) {
			this.job = job;
		}

		public String getServiceDescription() {
			return serviceDescription;
		}

		public void setServiceDescription(String serviceDescription) {
			this.serviceDescription = serviceDescription;
		}

		public String getJobEquipment() {
			return jobEquipment;
		}

		public void setJobEquipment(String jobEquipment) {
			this.jobEquipment = jobEquipment;
		}

		public Double getDirectLabor() {
			return directLabor;
		}

		public void setDirectLabor(Double directLabor) {
			this.directLabor = directLabor;
		}

		public Date getCompletedDate() {
			return completedDate;
		}

		public void setCompletedDate(Date completedDate) {
			this.completedDate = completedDate;
		}

		public String getClientName() {
			return clientName;
		}

		public void setClientName(String clientName) {
			this.clientName = clientName;
		}

		
	}

	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

}