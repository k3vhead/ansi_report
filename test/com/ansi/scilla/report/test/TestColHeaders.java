package com.ansi.scilla.report.test;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.ColumnWidth;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportFormatter;

public class TestColHeaders {

	
	
	
	public void go() {
		Rpt report = new Rpt();
		
		float[] totalWidth = null;
		
		if ( report.getColumnWidths() != null ) {
			float[] columnWidths = new float[report.getColumnWidths().length]; // working area for column widths
			
			
			float definedWidth = 0.0F;
			float definedColumnCount = 0.0F;
			for ( int i = 0; i < report.getColumnWidths().length; i++ ) {
				if ( report.getColumnWidths()[i] != null && report.getColumnWidths()[i].pdfWidth() != null ) {
					columnWidths[i] = report.getColumnWidths()[i].pdfWidth();
					definedWidth = definedWidth + report.getColumnWidths()[i].pdfWidth();
					definedColumnCount = definedColumnCount + 1;
				}
			}
			float defaultWidth = (PDFReportFormatter.tableTotalWidth - definedWidth)/(columnWidths.length - definedColumnCount);
			for ( int i = 0; i < columnWidths.length; i++ ) {
				if ( columnWidths[i] == 0.0F ) {
					columnWidths[i] = defaultWidth;
				}
			}
			
			ColumnHeader[] headers = report.getHeaderRow();
			totalWidth = new float[headers.length];
			int idxC = 0;  // index into columnWidth
			for ( int idxH = 0; idxH < headers.length; idxH ++ ) {
				float width = 0.0F;
				for ( int cCount=0; cCount < headers[idxH].getColspan(); cCount++ ) {
					System.out.println(idxH + "\t" + idxC);
					width = width + columnWidths[idxC];
					idxC++;
				}
				totalWidth[idxH] = width;
			}
			
			float finalWidth = 0.0F;
			for (int i = 0; i< columnWidths.length; i++ ) {
				System.out.println(i + "\t" + columnWidths[i]);
				finalWidth = finalWidth + columnWidths[i];
			}
			System.out.println(finalWidth);
			System.out.println("******");
			float finaltotalWidth = 0.0F;
			for (int i = 0; i< totalWidth.length; i++ ) {
				System.out.println(i + "\t" + totalWidth[i]);
				finaltotalWidth = finaltotalWidth + totalWidth[i];
			}
			System.out.println(finaltotalWidth);
		}

		
		
		
	}
	
	
	public static void main(String[] args) {
		new TestColHeaders().go();
	}

	
	public class Rpt extends ApplicationObject {
		private static final long serialVersionUID = 1L;

		private ColumnHeader[] headerRow = new ColumnHeader[] {
				new ColumnHeader("billToName","Client Name", 3, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("jobId", "Job Code", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("ticketId", "Ticket", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("invoiceDate", "Invoice Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("invoiceId", "Invoice", 1, DataFormats.NUMBER_FORMAT, SummaryType.NONE),
				new ColumnHeader("divisionDisplay", "Div", 1, DataFormats.STRING_CENTERED, SummaryType.NONE),
				new ColumnHeader("paymentNote","Payment Notes", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("paymentDate", "Payment Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("checkNbr", "Check Number", 1, DataFormats.STRING_FORMAT, SummaryType.NONE),
				new ColumnHeader("checkDate", "Check Date", 1, DataFormats.DATE_FORMAT, SummaryType.NONE),
				new ColumnHeader("amount","PPC\nPaid", 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "divisionDisplay"),
				new ColumnHeader("taxAmt","Taxes\nPaid", 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "divisionDisplay"),
				new ColumnHeader("total","Total\nPaid", 1, DataFormats.CURRENCY_FORMAT, SummaryType.SUM, "divisionDisplay"),
				new ColumnHeader("jobSiteName","Site Name", 2, DataFormats.STRING_FORMAT, SummaryType.NONE),
		};
		
		private ColumnWidth[] columnWidths = new ColumnWidth[] {
				(ColumnWidth)null,
				ColumnWidth.DATETIME,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				(ColumnWidth)null,
				ColumnWidth.CRR_JOB_SITE_NAME,
				(ColumnWidth)null,
		};

		public ColumnHeader[] getHeaderRow() {
			return headerRow;
		}

		public ColumnWidth[] getColumnWidths() {
			return columnWidths;
		}
		
	}
}
