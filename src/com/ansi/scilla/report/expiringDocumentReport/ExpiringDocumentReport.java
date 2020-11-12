package com.ansi.scilla.report.expiringDocumentReport;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.document.DocumentType;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportByStartEnd;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;

public class ExpiringDocumentReport extends StandardReport implements ReportByStartEnd {

	private static final long serialVersionUID = 1L;
	public static final String FILENAME = "ExpiringDocument";

	public static final String REPORT_TITLE = "Expiring Document Report";
	private Calendar startDate;
	private Calendar endDate;
	private Calendar runDate;
	
	
	public ExpiringDocumentReport(Connection conn, Calendar startDate, Calendar endDate) throws Exception {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.setTitle(REPORT_TITLE);
		this.runDate = Calendar.getInstance(new AnsiTime());
		makeData(conn);
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
	public Calendar getRunDate() {
		return runDate;
	}
	public void setRunDate(Calendar runDate) {
		this.runDate = runDate;
	}

	private void makeData(Connection conn) throws Exception {
		super.setSubtitle(makeSubTitle());
		super.setHeaderRow(new ColumnHeader[] {
			new ColumnHeader("documentId","Document ID", 1, DataFormats.NUMBER_CENTERED, SummaryType.NONE),
			new ColumnHeader("description", "Description", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
			new ColumnHeader("documentDate", "Document Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
			new ColumnHeader("expirationDate", "Expiration Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
			new ColumnHeader("xrefType", "Type", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
			new ColumnHeader("xrefDisplay", "Reference", 1, DataFormats.STRING_FORMAT, SummaryType.NONE)
		});
		
		Method getRunDateMethod = this.getClass().getMethod("getRunDate", (Class<?>[])null);		
		
		List<ReportHeaderRow> headerLeft = Arrays.asList(new ReportHeaderRow[] {
			new ReportHeaderRow("Created:", getRunDateMethod, 0, DataFormats.DATE_TIME_FORMAT)
		});
		super.makeHeaderLeft(headerLeft);
		

		List<ReportHeaderRow> headerRight = Arrays.asList(new ReportHeaderRow[] {});
		super.makeHeaderRight(headerRight);
		
//		super.setColumnWidths(new Integer[] {
//				ColumnWidth.HEADER_COL1.width(),
//				ColumnWidth.DESCRIPTION.width(),
//				ColumnWidth.HEADER_ANSI.width()/2,
//				ColumnWidth.HEADER_ANSI.width()/2,
//				ColumnWidth.DOCUMENT_TYPE.width(),
//				ColumnWidth.DOCUMENT_REFERENCE.width()
//		});
		super.setColumnWidths(new ColumnWidth[] {
				new ColumnWidth(4250, (Float)null),
				new ColumnWidth(4250, (Float)null),
				new ColumnWidth(5000, (Float)null),
				new ColumnWidth(5000, (Float)null),
				new ColumnWidth(4250, (Float)null),
				new ColumnWidth(4250, (Float)null),
				new ColumnWidth(4250, (Float)null),
		});
		
		
		PreparedStatement ps = conn.prepareStatement(makeSql());
		Calendar sqlStartDate = (Calendar)this.startDate.clone();
		sqlStartDate.add(Calendar.DAY_OF_MONTH, -1);
		Calendar sqlEndDate = (Calendar)this.endDate.clone();
		sqlEndDate.add(Calendar.DAY_OF_MONTH, 1);
		ps.setDate(1, new java.sql.Date(sqlStartDate.getTimeInMillis()));
		ps.setDate(2, new java.sql.Date(sqlEndDate.getTimeInMillis()));
		ResultSet rs = ps.executeQuery();
		while ( rs.next() ) {
			super.addDataRow(new RowData(rs));
		}
		rs.close();
	}

	private String makeSubTitle() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");		
		return sdf.format(this.startDate.getTime()) + " to " + sdf.format(this.getEndDate().getTime());
	}

	protected String makeSql() {
		return makeSelect() + " " + makeFrom() + " " + makeWhere() + " " + makeOrderBy();
	}
	
	
		
	private String makeSelect() {
		String sqlSelectClause = 
				"select "
				+ "\n    document.document_id, "
				+ "\n    document.description, "
				+ "\n    document.document_date, "
				+ "\n    document.expiration_date,"
				+ "\n    document.xref_type, "
				+ "\n    document.xref_id";
		
		List<String> sqlSelect = new ArrayList<String>();
		for ( DocumentType type : DocumentType.values()) {
			sqlSelect.add("when xref_type = '"+type.name()+"' then " + type.xrefDisplay());			
		}
		return sqlSelectClause + ",\ncase\n" + StringUtils.join(sqlSelect, "\n") + "\nend as xref_display";		
	}

	private String makeFrom() {
		String sqlFromClause = "\nfrom document";
		List<String> sqlFrom = new ArrayList<String>();
		for ( DocumentType type : DocumentType.values()) {
			sqlFrom.add("left outer join "+type.xrefTable()+" on document.xref_type='"+type.name()+"' and "+type.xrefTable() + "." + type.xrefKey()+"=document.xref_id");			
		}
		return sqlFromClause + "\n" + StringUtils.join(sqlFrom, "\n");
	}

	private String makeWhere() {
		String whereClause = "\nwhere document.expiration_date>? \nand document.expiration_date<?";
		return whereClause;
	}

	private String makeOrderBy() {		
		return "\norder by document.expiration_date asc";
	}

	
//	public static XSSFWorkbook makeReport(Connection conn, Calendar startDate,Calendar endDate) throws Exception {
//		ExpiringDocumentReport report = new ExpiringDocumentReport(conn, startDate, endDate);
//		return report.makeXLS();
//	}
	
	public static ExpiringDocumentReport buildReport(Connection conn, Calendar startDate,Calendar endDate) throws Exception {	
		return new ExpiringDocumentReport(conn, startDate, endDate);
	}
	
	
	
	public class RowData extends ApplicationObject {

		private static final long serialVersionUID = 1L;
		private Integer documentId;
		private String description;
		private Date documentDate;
		private Date expirationDate;
		private String xrefType;
		private String xrefDisplay;
		
		public RowData(ResultSet rs) throws SQLException {
			this.documentId = rs.getInt("document_id");
			this.description = rs.getString("description");
			this.documentDate = rs.getDate("document_date");
			this.expirationDate = rs.getDate("expiration_date");
			this.xrefType = rs.getString("xref_type");
			this.xrefDisplay = rs.getString("xref_display");
		}

		public Integer getDocumentId() {
			return documentId;
		}

		public void setDocumentId(Integer documentId) {
			this.documentId = documentId;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Date getDocumentDate() {
			return documentDate;
		}

		public void setDocumentDate(Date documentDate) {
			this.documentDate = documentDate;
		}

		public Date getExpirationDate() {
			return expirationDate;
		}

		public void setExpirationDate(Date expirationDate) {
			this.expirationDate = expirationDate;
		}

		public String getXrefType() {
			return xrefType;
		}

		public void setXrefType(String xrefType) {
			this.xrefType = xrefType;
		}

		public String getXrefDisplay() {
			return xrefDisplay;
		}

		public void setXrefDisplay(String xrefDisplay) {
			this.xrefDisplay = xrefDisplay;
		}
		
		
	}

	@Override
	public String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate) {
		return makeFileName(FILENAME, runDate, division, startDate, endDate);
	}

}
