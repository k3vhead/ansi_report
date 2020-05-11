package com.ansi.scilla.report.reportBuilder.common;

/**
 * Default width of report columns<br />
 * Value is 1/256 of a character (per https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/Sheet.html)<br />
 * Naming convention is &lt;DB table&gt;_&lt;field&gt;
 * 
 * @author dclewis
 *
 */
public enum ColumnWidth {
	DATETIME(3750, (Float)null),
	DATE(2750, (Float)null),
	HEADER_COL1(2000, (Float)null),
	HEADER_COL_RIGHT(2000, (Float)null),
	HEADER_ANSI(8600, (Float)null),
	HDR_RIGHT_NON_DISPATCHED(3500, (Float)null),
	
	ADDRESS_NAME(11000, (Float)null),
	ADDRESS_ADDRESS1(11000, (Float)null),
	ADDRESS_CITY(3500, (Float)null),
	ADDRESS_STATE(1500, (Float)null),
	
	CONTACT_NAME(11000, (Float)null),
	
	
	JOB_JOB_NBR(1400, (Float)null),
	JOB_JOB_FREQUENCY(1400, (Float)null),
	JOB_JOB_STATUS(1600, (Float)null), 
	JOB_PPC(2500, (Float)null),
	
	
	// These are custom column widths for specific_reports:
	CRR_JOB_SITE_NAME(ADDRESS_NAME.xlsWidth() - DATE.xlsWidth(), (Float)null),			// CashReceiptsRegisterDetailReport
	IRR_TICKET_ID(CONTACT_NAME.xlsWidth() - DATETIME.xlsWidth() - HEADER_COL1.xlsWidth(), (Float)null),           // InvoiceRegisterReport
	IRR_INVOICE_AMT(ADDRESS_NAME.xlsWidth() - HEADER_COL_RIGHT.xlsWidth(), (Float)null),			// InvoiceRegisterReport
	PAC_SUMMARY_COLUMN(HEADER_ANSI.xlsWidth()/3, (Float)null),      					// PacSummaryReport
	SMRV_JOB_SITE_NAME(ADDRESS_NAME.xlsWidth()-DATETIME.xlsWidth()-DATE.xlsWidth(), (Float)null),   // SmrvDetailReport
	SMRV_ADDRESS1(ADDRESS_ADDRESS1.xlsWidth()-DATE.xlsWidth(), (Float)null),		   	// SmrvDetailReport
	DO_SITE_NAME(ADDRESS_NAME.xlsWidth() - DATETIME.xlsWidth(), (Float)null),			// DispatchedOutstandingTicketReport
	
	
	// This is here because we reference(d) zero at one point
	ZERO_WIDTH(0, 0F),
	;
	
	private final Integer xlsWidth;
	private final Float pdfWidth;
	
	private ColumnWidth(Integer xlsWidth, Float pdfWidth) {
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
