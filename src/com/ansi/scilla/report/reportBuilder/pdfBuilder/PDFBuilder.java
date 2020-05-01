package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.NoPreviousValue;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.thewebthing.commons.lang.StringUtils;

public class PDFBuilder extends AbstractPDFBuilder {
	private static final long serialVersionUID = 1L;
	private Logger logger = LogManager.getLogger(PDFBuilder.class);
	
	public PDFBuilder(StandardReport report) {
		super(report);
	}
	
	public PDFBuilder(StandardSummaryReport report) {
		super(report);
	}
	
	
	private ByteArrayOutputStream buildReport() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		PDFReportHeader header = new PDFReportHeader(report);
		float topMargin = header.getHeaderTable().getTotalHeight() + (PDFReportFormatter.shortSideSize - PDFReportFormatter.headerDefaultPositionY) + 4.0F;
		Document document = new Document(PageSize.LETTER.rotate(), PDFReportFormatter.marginLeft, PDFReportFormatter.marginRight, topMargin, PDFReportFormatter.marginBottom);
		PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
		document.open();
		pdfWriter.setPageEvent(header);

		//TODO : Column Widths
		//TODO : Footer / page count
		PdfPTable dataTable = new PdfPTable(((StandardReport)report).getHeaderRow().length);
		dataTable.setHeaderRows(1);	// set column headers to repeat on each page
		dataTable.setWidthPercentage(100F);
		PDFReportBuilderUtils.makeColumnHeader((StandardReport)report, dataTable);		
		makeDetails((StandardReport)report, dataTable);
		makeFinalSubtotal(dataTable);
		makeSummary(dataTable);	
		document.add(dataTable);
		document.close();

		return baos;

	}

	

	private void makeDetails(StandardReport report, PdfPTable dataTable) throws Exception {
		
		for ( Object dataRow : report.getDataRows() ) {
			makeSubtotal(report, dataRow, dataTable);
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];				
				Object value = makeDisplayData(columnHeader, dataRow);
				String display = makeFormattedDisplayData(columnHeader.getFormatter(), value);
				Phrase content = new Phrase(new Chunk(display, PDFReportFormatter.fontStandardBlack));
				PdfPCell cell = new PdfPCell(content);				
				/* If you're looking here because you got key error, you need to add a dataformat to the cell styles in PDFReportFormatter */
				cell.setHorizontalAlignment(PDFReportFormatter.cellStyles.get(columnHeader.getFormatter()));
				dataTable.addCell(cell);
				super.doSummaries(columnHeader, value);
				
			}
		}
//		for ( int i = 0; i < report.getHeaderRow().length+4; i++ ) { // removed for performance issues 13 mins/column in CRR Detail
//			sheet.autoSizeColumn(i);
//		}
	}
	
	
	private void makeSubtotal(StandardReport report, Object row, PdfPTable dataTable) throws Exception {
		List<String> fieldsToDisplay = new ArrayList<String>();
		List<String> fieldsThatChanged = new ArrayList<String>();
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];	
			String fieldName = columnHeader.getFieldName();
			if (this.previousValues.containsKey(columnHeader.getFieldName())) {
				// we need to check for changed values because this field is a trigger for a subtotal
				Object previousValue = this.previousValues.get(fieldName);
				Object newValue = makeDisplayData(columnHeader,row);
				if ( ! previousValue.equals(new NoPreviousValue()) && ! previousValue.equals(newValue)) {
					// we have a value change, so add a subtotal row
					fieldsThatChanged.add(fieldName);
				}
				this.previousValues.put(fieldName,  newValue);
			}			
		}
		
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			String fieldName = columnHeader.getFieldName();
			if ( fieldsThatChanged.contains(columnHeader.getSubTotalTrigger())) {
				fieldsToDisplay.add(fieldName);
			}
		}
		
		if ( ! fieldsToDisplay.isEmpty() ) {
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];
				String fieldName = columnHeader.getFieldName();
				if ( fieldsToDisplay.contains(fieldName)) {
					String subtotal = super.makeSubtotalData(columnHeader);
					PdfPCell cell = new PdfPCell(new Phrase(new Chunk(subtotal, PDFReportFormatter.fontSubtotal)));
					cell.setHorizontalAlignment(PDFReportFormatter.cellStyles.get(columnHeader.getFormatter()));
					dataTable.addCell(cell);					
				} else {
					dataTable.addCell(new PdfPCell(new Phrase("")));
				}
			}
		}
	}
	
	
	private void makeFinalSubtotal(PdfPTable dataTable) throws Exception {
		StandardReport report = (StandardReport)this.report;
		List<PdfPCell> subtotalRow = new ArrayList<PdfPCell>();
		boolean addASub = false;
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];	
			PdfPCell cell = new PdfPCell();
			if ( StringUtils.isBlank(columnHeader.getSubTotalTrigger())) {
				cell.setPhrase(new Phrase(""));
			} else {
				// we're doing a subtotal for this field				
				addASub = true;
				String subtotal = super.makeSubtotalData(columnHeader);
				cell.setPhrase(new Phrase(new Chunk(subtotal, PDFReportFormatter.fontSubtotal)));
				cell.setHorizontalAlignment(PDFReportFormatter.cellStyles.get(columnHeader.getFormatter()));
			}
			subtotalRow.add(cell);
					
		}
		
		if ( addASub ) {
			for ( PdfPCell cell : subtotalRow ) {
				dataTable.addCell(cell);
			}
		}
	}

	
	
	private void makeSummary(PdfPTable dataTable) throws Exception {
		StandardReport report = (StandardReport)this.report;
		boolean addASummary = false;		
		List<PdfPCell> summaryRow = new ArrayList<PdfPCell>();
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			PdfPCell cell = new PdfPCell();			
			if ( columnHeader.getSummaryType().equals(SummaryType.NONE)) {
				cell.setPhrase(new Phrase(""));
			} else {
				addASummary = true;
				String subtotal = makeSummaryData(columnHeader);
				cell.setPhrase(new Phrase(new Chunk(subtotal, PDFReportFormatter.fontSubtotal)));
				cell.setHorizontalAlignment(PDFReportFormatter.cellStyles.get(columnHeader.getFormatter()));
			}
			summaryRow.add(cell);
		}
		
		if ( addASummary ) {
			for ( PdfPCell cell : summaryRow ) {
				dataTable.addCell(cell);
			}
		}
	}

	public static ByteArrayOutputStream build(StandardReport report) throws Exception {
		return new PDFBuilder(report).buildReport();
	}
	
	
}
