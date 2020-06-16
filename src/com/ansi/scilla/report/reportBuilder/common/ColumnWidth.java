package com.ansi.scilla.report.reportBuilder.common;

import com.ansi.scilla.common.ApplicationObject;

/**
 * Default width of report columns<br />
 * XLS Value is 1/256 of a character (per https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/Sheet.html)<br />
 * Naming convention is &lt;DB table&gt;_&lt;field&gt;
 * 
 * @author dclewis
 *
 */
public class ColumnWidth extends ApplicationObject {
	private static final long serialVersionUID = 1L;
	private final Integer xlsWidth;
	private final Float pdfWidth;

	public ColumnWidth(Integer xlsWidth, Float pdfWidth) {
		super();
		this.xlsWidth = xlsWidth;
		this.pdfWidth = pdfWidth;
	}

	public Integer xlsWidth() {
		return xlsWidth;
	}
	public Float pdfWidth() {
		return pdfWidth;
	}



/*
public enum ColumnWidthX {
//	DATETIME(3750, 45.0F),  
//	DATE(2750, 57.0F),
	HEADER_COL1(2000, (Float)null),		new ColumnWidth(2000, (Float)null)
	HEADER_COL_RIGHT(2000, (Float)null),	new ColumnWidth(2000, (Float)null)
	HEADER_ANSI(8600, (Float)null),		new ColumnWidth(8600, (Float)null)
	HDR_RIGHT_NON_DISPATCHED		new ColumnWidth(3500, (Float)null),	
	
	ADDRESS_NAME		new ColumnWidth(11000, 200.0F),
	ADDRESS_ADDRESS1		new ColumnWidth(11000, 200.0F),
	ADDRESS_CITY			new ColumnWidth(3500, (Float)null),
	ADDRESS_STATE			new ColumnWidth(1500, (Float)null),
	
	CONTACT_NAME			new ColumnWidth(11000, (Float)null),
	
	
	JOB_JOB_NBR				new ColumnWidth(1400, 30.0F),
	JOB_JOB_FREQUENCY			new ColumnWidth(1400, (Float)null),
	JOB_JOB_STATUS			new ColumnWidth(1600, (Float)null), 
	JOB_PPC					new ColumnWidth(2500, 46.0F),
	JOB_JOB_ID				new ColumnWidth(2500, 32.0F),
	
	TICKET_STATUS			new ColumnWidth(2750, 55.0F), 			//TicketStatusReport
	TICKET_INVOICED			new ColumnWidth(2500, 42.0F),			// TicketStatusReport
	TICKET_NBR				new ColumnWidth(2500, 32.0F),				// TicketStatusReport

	
	// These are custom column widths for specific_reports:
	CRR_JOB_SITE_NAME(ADDRESS_NAME.xlsWidth() - DATE.xlsWidth(), ADDRESS_NAME.pdfWidth() - DATE.pdfWidth()),	new ColumnWidth(7250, 155.0F)		// CashReceiptsRegisterDetailReport
	IRR_TICKET_ID(CONTACT_NAME.xlsWidth() - DATETIME.xlsWidth() - HEADER_COL1.xlsWidth(), (Float)null),         new ColumnWidth(5250, (Float)null)  // InvoiceRegisterReport
	IRR_INVOICE_AMT(ADDRESS_NAME.xlsWidth() - HEADER_COL_RIGHT.xlsWidth(), (Float)null),						new ColumnWidth(9000, (Float)null)	// InvoiceRegisterReport
	PAC_SUMMARY_COLUMN(HEADER_ANSI.xlsWidth()/3, (Float)null),      											new ColumnWidth(2866, (Float)null)	// PacSummaryReport
	SMRV_JOB_SITE_NAME(ADDRESS_NAME.xlsWidth()-DATETIME.xlsWidth()-DATE.xlsWidth(), (Float)null),   			new ColumnWidth(4500, (Float)null)	// SmrvDetailReport
	SMRV_ADDRESS1(ADDRESS_ADDRESS1.xlsWidth()-DATE.xlsWidth(), (Float)null),		   							new ColumnWidth(8250, (Float)null)	// SmrvDetailReport
	DO_SITE_NAME(ADDRESS_NAME.xlsWidth() - DATETIME.xlsWidth(), (Float)null),									new ColumnWidth(7250, 155.0F)		// DispatchedOutstandingTicketReport
	
	
	// This is here because we reference(d) zero at one point
	ZERO_WIDTH				new ColumnWidth(0, 0F),
	;
	
	private final Integer xlsWidth;
	private final Float pdfWidth;
	
	private ColumnWidthX(Integer xlsWidth, Float pdfWidth) {
		this.xlsWidth = xlsWidth;
		this.pdfWidth = pdfWidth;
	}
	
	public Integer xlsWidth() {
		return this.xlsWidth;
	}
	
	public Float pdfWidth() {
		return this.pdfWidth;
	}
}
*/
}
