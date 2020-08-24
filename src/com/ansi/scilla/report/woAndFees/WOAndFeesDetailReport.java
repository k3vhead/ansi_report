package com.ansi.scilla.report.woAndFees;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.DateFormatter;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByDivStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.ReportStartLoc;
import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSBuilder;

public class WOAndFeesDetailReport extends StandardReport implements ReportByDivStartEnd {

	private static final long serialVersionUID = 1L;

	private final String sql = "select concat(division_code,'-',ticket.ticket_type)" +
			" as div_type, division_code as Div, ticket.ticket_type as 'Fee/WO'\n" + 
			", ticket.ticket_id as Ticket, job_site.name as 'Job Site', " +
			"job.job_id as Job, job_site.address1 as 'Job Address'\n" + 
			", job.job_nbr as 'Job #', \n" + 
			"ticket.invoice_id as Invoice, ticket.invoice_date as 'Inv Date', " +
			"ticket.act_price_per_cleaning as PPC\n" + 
			", ticket.process_notes as Notes\n" + 
			"from ticket\n" + 
			"join division on division.division_id = ticket.act_division_id\n" + 
			"join job on job.job_id = ticket.job_id\n" + 
			"join quote on quote.quote_id = job.quote_id\n" + 
			"join address as job_site on job_site.address_id = job_site_address_id\n" + 
			"where invoice_date >= '2020-01-01' and invoice_date < '2020-04-01'\n" + 
			"and ticket.ticket_type in ('fee','writeoff')\n" + 
			"order by division_nbr, ticket_type, name, job_nbr, invoice_date\n";
			

	
	public static final String REPORT_TITLE = "WO and Fees Detail";
	public static final String FILENAME = "WOandFeesDetail";
//	private final String REPORT_NOTES = "notes go here";
	
	private Calendar startDate;
	private Calendar endDate;
	private Integer lastDivision;
	
	private List<RowData> data;
	
	Logger logger = LogManager.getLogger(this.getClass());
	
	public WOAndFeesDetailReport() {
		super();
		this.setTitle(REPORT_TITLE);
		super.setReportOrientation(ReportOrientation.PORTRAIT);

	}
	/**
	 * Default date range is current month-to-date
	 * @param conn Database exception
	 * @throws Exception something bad happened
	 */
	protected WOAndFeesDetailReport(Connection conn) throws Exception {
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

	protected WOAndFeesDetailReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
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
	
	private List<RowData> makeData(Connection conn, WOAndFeesDetailReport report, Calendar startDate, Calendar endDate) throws Exception {
		
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
				new ColumnHeader("type", "Type", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("ppc","PPC", 1, DataFormats.DECIMAL_FORMAT, SummaryType.SUM),
				
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
				(ColumnWidth)null,
				new ColumnWidth(3950, 45.0F),
				new ColumnWidth(11000, (Float)null),
				new ColumnWidth(11000, (Float)null),
				new ColumnWidth(3250, (Float)null),
				(ColumnWidth)null,
				new ColumnWidth(6000, 57.0F),
		});
		
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
	
	public static WOAndFeesDetailReport buildReport(Connection conn) throws Exception {
		return new WOAndFeesDetailReport(conn);
	}
	public static WOAndFeesDetailReport buildReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		return new WOAndFeesDetailReport(conn, startDate, endDate);
	}


	public class RowData extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		public String div;
		public String type;
		public BigDecimal ppc;
		
	
		public RowData(ResultSet rs, WOAndFeesDetailReport report) throws SQLException {
			this.div = rs.getString("div");
			this.type = rs.getString("job");
			this.ppc = rs.getBigDecimal("service_description");
			
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

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public BigDecimal getServiceDescription() {
			return ppc;
		}

		public void setServiceDescription(BigDecimal ppc) {
			this.ppc = ppc;
		}

		
	}

	
	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

}