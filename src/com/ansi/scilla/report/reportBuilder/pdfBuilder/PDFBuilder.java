package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.StandardSummaryReport;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.NoPreviousValue;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
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
	
	
	private Document buildReport(Document document, PdfWriter pdfWriter) throws Exception {
		if ( this.report instanceof StandardSummaryReport ) {
			logger.log(Level.DEBUG, "Making standard summary report header");
			pdfWriter.setPageEvent(new StandardSummaryPDFReportHeader((StandardSummaryReport)report));
		} else {
			logger.log(Level.DEBUG, "Making standard report header");
			pdfWriter.setPageEvent(new StandardPDFReportHeader((StandardReport)report));
		}
		
		//TODO : Margin top based on number of rows in header (incl. notes)
//		document.setMargins(left, right, top, bottom);
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
		return document;

	}

	
	public static void build(StandardReport report, Document document, PdfWriter pdfWriter) throws Exception {
		PDFBuilder builder = new PDFBuilder(report);		
		builder.buildReport(document, pdfWriter);
	}
	
	public static void build(StandardSummaryReport report, Document document, PdfWriter pdfWriter) throws Exception {
		PDFBuilder builder = new PDFBuilder(report);		
		builder.buildReport(document, pdfWriter);
	}

	
	
	/**
	 * Create a new spreadsheet with one tab filled with the input report
	 * 
	 * @param report
	 * @return
	 * @throws Exception
	 */
//	public static Document build(StandardReport report) throws Exception {
////		Document workbook = new Document(PageSize.LETTER, marginLeft, marginRight, marginTop, marginBottom);
//		PDFBuilder builder = new PDFBuilder(report);
//		Document workbook = builder.buildReport();
//		return workbook;
//	}

	
	/**
	 * Add a report as a block of cells to an existing spreadsheet tab
	 * 
	 * @param report
	 * @param sheet
	 * @param reportStartLoc
	 * @throws Exception
	 */
	public static void build(StandardReport report, XSSFSheet sheet, ReportStartLoc reportStartLoc) throws Exception {
//		XLSBuilder builder = new XLSBuilder(report);
//		builder.setReportStartLoc(reportStartLoc);
//		builder.makeFormatters(sheet.getWorkbook());
//		builder.buildReport(sheet);
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
/*
	

	
	private float drawColumnText(PdfContentByte canvas, Rectangle rect, Element p, boolean simulate) throws DocumentException {
		ColumnText ct = new ColumnText(canvas);
		ct.setSimpleColumn(rect);
		ct.addElement(p);
		ct.go(simulate);
		return ct.getYLine();
	}
	
	

	
	
	private void makePDFHeader() throws Exception{
		StringBuffer buffer = new StringBuffer();
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		CreationHelper createHelper = workbook.getCreationHelper();
		sheet.getPrintSetup().setLandscape(true);
		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.LETTER_PAPERSIZE);
		sheet.getPrintSetup().setFitWidth((short)1);
		
		short dataFormatDate = createHelper.createDataFormat().getFormat("mm/dd/yyyy");
	    short dataFormatDateTime = createHelper.createDataFormat().getFormat("mm/dd/yyyy hh:mm:ss");
	    short dataFormatDecimal = createHelper.createDataFormat().getFormat("#,##0.00");
	    short dataFormatInteger = createHelper.createDataFormat().getFormat("#,##0");
	    Integer fontHeight = 9;
	    XSSFFont fontDefaultFont = workbook.createFont();
		fontDefaultFont.setFontHeight(fontHeight);
		
		CellStyle cellStyleRunDate = workbook.createCellStyle();
	    cellStyleRunDate.setDataFormat(dataFormatDateTime);
	    cellStyleRunDate.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleRunDate.setFont(fontDefaultFont);
		
	    CellStyle cellStyleHeaderDivision = workbook.createCellStyle();
	    cellStyleHeaderDivision.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleHeaderDivision.setDataFormat(dataFormatInteger);
	    cellStyleHeaderDivision.setFont(fontDefaultFont);
	    
		//Date today = new Date();
		int rowNum = 0;
		int colNum = 0;
		XSSFRow row = null;
		XSSFCell cell = null;
		
	    row = sheet.createRow(rowNum);
	    row.setHeight((short)400);
//	    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,1,2));
//	    sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,3,7));
	    cell = row.createCell(colNum);	
//	    cell.setCellStyle(cellStyleHeaderLabel);
	    //cell.setCellStyle(cellStyleCreatedLabel);	    
	    cell.setCellValue("Created:");
	    colNum++;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleRunDate);
	    cell.setCellValue(this.runDate);
	    colNum++;
	    colNum++;  // make up for merged rows
	    cell = row.createCell(colNum);
	    //cell.setCellStyle(cellStyleAnsi);
	    cell.setCellValue("American National Skyline, Inc.");
	    colNum = 9;
	    cell = row.createCell(colNum);
	    //cell.setCellStyle(cellStyleCreatedLabel);
	    cell.setCellValue("Division:");
	    colNum++;
	    cell = row.createCell(colNum);
	    cell.setCellStyle(cellStyleHeaderDivision);
	    cell.setCellValue(this.div);
		

	}
	
	
	public ByteArrayOutputStream makePDF(Connection conn, List<TicketPrintResult> ticketList) throws Exception {
		
		Document document = new Document(PageSize.LETTER, marginLeft, marginRight, marginTop, marginBottom);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
		pdfWriter.setPageEvent(new TicketWatermarkManagerCopy());
		pdfWriter.setPageEvent(new TicketWatermarkOfficeCopy());

		document.open();
			
		PdfContentByte cb = pdfWriter.getDirectContent();
		for(TicketPrintResult ticket : ticketList){
			//Position(LLX, LLY, URX, URY);
			//remitTo = new Position(50F,715F,130F,766F);
			remitTo = new Position(15F,730F,70F,780F);
			Rectangle rect = new Rectangle(remitTo.llx, remitTo.lly, remitTo.urx, remitTo.ury);
			//rect.setBorder(Rectangle.BOX);
			rect.setBorderWidth(0.5f);
			//rect.setBorderColor(BaseColor.RED);
			rect.setBackgroundColor(BaseColor.LIGHT_GRAY);
			cb.rectangle(rect);
			Paragraph paragraph = new Paragraph();
			paragraph.setLeading(0F, 1.1F);
//			paragraph.setIndentationLeft(154.8F);
			paragraph.setAlignment(Paragraph.ALIGN_LEFT);
			paragraph.setIndentationLeft(3);
			paragraph.add(new Chunk("Direct Labor:", fontHelv8));
			paragraph.add(new Chunk("\n"));
			paragraph.add(new Chunk("P.P.C.:", fontHelv8));
			paragraph.add(new Chunk("\n"));
			paragraph.add(new Chunk("Pay Method:", fontHelv8));
			paragraph.add(new Chunk("\n"));
			paragraph.add(new Chunk("Site Contact:", fontHelv8));
			paragraph.add(new Chunk("\n"));
			paragraph.add(new Chunk("Job Contact:", fontHelv8));
			drawColumnText(cb, rect, paragraph, false);
			
		}
//		Paragraph paragraph = new Paragraph(new Chunk("Stuff goes here"));
//		document.add(paragraph);
		try {
			document.close();
		} catch ( Exception e ) {
			System.err.println(e.getMessage());
		}
		
		return baos;
	}
	
	
*/	
	
}
