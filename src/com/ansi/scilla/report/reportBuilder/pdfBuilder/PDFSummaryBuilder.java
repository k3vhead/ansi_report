package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.io.ByteArrayOutputStream;

import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.reportType.StandardReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFSummaryBuilder extends AbstractPDFBuilder {

	private static final long serialVersionUID = 1L;

	protected PDFSummaryBuilder(StandardSummaryReport report) {
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

		//TODO : Footer / page count
		PdfContentByte pdfContentByte = pdfWriter.getDirectContent();
		
		TableSize companyTableSize = addCompanyTable(pdfContentByte, topMargin);
		TableSize regionTableSize = addRegionTable(pdfContentByte, companyTableSize.getHeight());
		addDivisionTable(pdfContentByte, topMargin, Math.max(regionTableSize.getWidth(), companyTableSize.getWidth()));
		document.close();

		return baos;	
	}
	
	private TableSize addCompanyTable(PdfContentByte cb, Float topPosition) throws DocumentException, Exception {
		StandardSummaryReport report = (StandardSummaryReport)this.report;
		StandardReport subReport = report.getCompanySummary();
		PdfPTable subTable = makeSubReport(subReport);
		Float llx = PDFReportFormatter.marginLeft;
		Float lly = 0F;
		Float urx = llx + subTable.getTotalWidth();
		Float ury = PDFReportFormatter.shortSideSize - topPosition;
		return addTable(cb, subTable, llx, lly, urx, ury );
	}

	private TableSize addRegionTable(PdfContentByte cb, Float topPosition) throws DocumentException, Exception {
		StandardSummaryReport report = (StandardSummaryReport)this.report;
		StandardReport subReport = report.getRegionSummary();
		PdfPTable subTable = makeSubReport(subReport);
		Float llx = PDFReportFormatter.marginLeft;
		Float lly = 0F;
		Float urx = llx + subTable.getTotalWidth();
		Float ury = topPosition - 20F;
		return addTable(cb, subTable, llx, lly, urx, ury );		
	}

	private TableSize addDivisionTable(PdfContentByte cb, Float topPosition, Float leftPosition) throws DocumentException, Exception {
		StandardSummaryReport report = (StandardSummaryReport)this.report;
		StandardReport subReport = report.getDivisionSummary();
		PdfPTable subTable = makeSubReport(subReport);
		Float llx = leftPosition + PDFReportFormatter.marginLeft + 60F;
		Float lly = 0F;
		Float urx = llx + subTable.getTotalWidth();
		Float ury = PDFReportFormatter.shortSideSize - topPosition;
		return addTable(cb, subTable, llx, lly, urx, ury );		
	}

	
	private PdfPTable makeSubReport(StandardReport subReport) throws DocumentException, Exception {
		super.initializeSummaries(subReport.getHeaderRow());

		PdfPTable dataTable = new PdfPTable(4);
		dataTable.setTotalWidth(new float[] {100F, 75F, 75F, 75F});
		dataTable.setLockedWidth(true);
	
		PdfPCell bannerCell = new AnsiPCell();
		bannerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		bannerCell.setColspan(4);
		bannerCell.setPhrase(new Phrase(new Chunk(subReport.getBanner(), PDFReportFormatter.fontReportBanner)));
		dataTable.addCell(bannerCell);
		
		PdfPCell titleCell = new AnsiPCell();
		titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		titleCell.setColspan(4);
		titleCell.setPhrase(new Phrase(new Chunk(subReport.getTitle(), PDFReportFormatter.fontReportTitle)));
		dataTable.addCell(titleCell);
		
		PdfPCell subTitleCell = new AnsiPCell();
		subTitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		subTitleCell.setColspan(4);
		subTitleCell.setPhrase(new Phrase(new Chunk(subReport.getSubtitle(), PDFReportFormatter.fontReportSubTitle)));
		dataTable.addCell(subTitleCell);
		
		PDFReportBuilderUtils.makeColumnHeader(subReport, dataTable);
		makeDetails(subReport, dataTable);
		makeSummary(subReport, dataTable);	
		
		return dataTable;
		
	}
	
	
	private void makeDetails(StandardReport report, PdfPTable dataTable) throws Exception  {		
		for ( Object dataRow : report.getDataRows() ) {
//			makeSubtotal(report, dataRow, dataTable);
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];				
				Object value = makeDisplayData(columnHeader, dataRow);
				String display = makeFormattedDisplayData(columnHeader.getFormatter(), value);
				PdfPCell cell = new AnsiPCell(new Chunk(display, PDFReportFormatter.fontStandardBlack));	
				/* If you're looking here because you got key error, you need to add a dataformat to the cell styles in PDFReportFormatter */
				cell.setHorizontalAlignment(PDFReportFormatter.cellStyles.get(columnHeader.getFormatter()));
				dataTable.addCell(cell);
				super.doSummaries(columnHeader, value);				
			}
		}
	}



	/**
	 * 
	 * @param pdfContentByte
	 * @param subReport
	 * @param llx
	 * @param lly
	 * @param urx
	 * @param ury
	 * @return  size of the added table
	 * @throws DocumentException
	 */
	private TableSize addTable(PdfContentByte pdfContentByte, PdfPTable subReport, Float llx, Float lly, Float urx, Float ury) throws DocumentException {
		Rectangle rect = new Rectangle(llx, lly, urx, ury);
		drawColumnText(pdfContentByte, rect, subReport, false);
		return new TableSize(subReport.getTotalHeight(), subReport.getTotalWidth());
	}

	public static ByteArrayOutputStream build(StandardSummaryReport report) throws Exception {
		return new PDFSummaryBuilder(report).buildReport();
	}

}
