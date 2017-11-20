package com.ansi.scilla.report.reportBuilder;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.common.jobticket.TicketWatermarkManagerCopy;
import com.ansi.scilla.common.jobticket.TicketWatermarkOfficeCopy;
import com.ansi.scilla.common.queries.TicketPrintResult;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFBuilder extends AbstractPDFBuilder {
	private static final long serialVersionUID = 1L;
	
	private final Font fontBillToName = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
	private final Font fontBillToAddress = FontFactory.getFont(FontFactory.HELVETICA, 8);
	private final Font fontHelvBold24 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
	private final Font fontHelv14 = FontFactory.getFont(FontFactory.HELVETICA, 14);
	private final Font fontHelvBold14 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
	private final Font fontHelvItalic14 = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 14);
	private final Font fontHelvItalic12 = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12);
	private final Font fontHelv12 = FontFactory.getFont(FontFactory.HELVETICA, 12);
	private final Font fontHelv11 = FontFactory.getFont(FontFactory.HELVETICA, 11);
	private final Font fontHelv10 = FontFactory.getFont(FontFactory.HELVETICA, 10);
	private final Font fontHelv9 = FontFactory.getFont(FontFactory.HELVETICA, 9);
	private final Font fontHelv85 = FontFactory.getFont(FontFactory.HELVETICA, 8.5F);
	private final Font fontHelv8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
	private final Font fontHelv6 = FontFactory.getFont(FontFactory.HELVETICA, 6);
	private final Font fontHelv5 = FontFactory.getFont(FontFactory.HELVETICA, 5);
	private final Font fontHelv4 = FontFactory.getFont(FontFactory.HELVETICA, 4);
	private final Font fontInvoice = FontFactory.getFont(FontFactory.TIMES_BOLD, 16F);
	private final Font fontInvoiceNumber = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
	private final Font spacerFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 3);
	private final Font fontTicket = FontFactory.getFont(FontFactory.HELVETICA, 10);
	private final Font fontTicketBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
	private final Font fontSplashTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
	private final Font fontSplashText = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
	private final Font fontClearlyWindowsBanner1 = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 24);
	private final Font fontClearlyWindowsBanner2 = FontFactory.getFont(FontFactory.TIMES_ITALIC, 12);
	private static Font fontRomanRed8;
	private static Font fontRomanRed10;
	private static Font fontRomanRedItalic10;
	private static Font fontRomanRed12;
	
	private Position remitTo = new Position(144F,576F,220.5F,756F);
	private final Position returnAddress = new Position(220F,700F,365F,756F);
	private final Position toAddress = new Position(144F,684F,165F,700F);
	private final Position billTo = new Position(180F,635F,375F,690F);
	private final Position invoice = new Position(460F,754F,550F,775F);

	private final float[] ticketTableColumns = new float[] {120F, 360F, 80F};
	private final Integer ticketsPerPage = 5;
	private Calendar printDate;
	
	private final SimpleDateFormat ticketDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private final DecimalFormat dollarFormat = new  DecimalFormat("$#,##0.00");
	private final SimpleDateFormat ticketDateTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
	
	private final float marginLeft = 30F;
	private final float marginRight = 30F;
	private final float marginTop = 36F;
	private final float marginBottom = 36F;
	
	private Calendar runDate;
	private Calendar startDate;
	private Calendar endDate;
	
	private String div;
	private Integer divisionId;
	
	static {
		fontRomanRed8 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 8);
		fontRomanRed8.setColor(BaseColor.RED);
		fontRomanRed10 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10);
		fontRomanRed10.setColor(BaseColor.RED);
		fontRomanRedItalic10 = FontFactory.getFont(FontFactory.TIMES_ITALIC, 10);
		fontRomanRedItalic10.setColor(BaseColor.RED);
		fontRomanRed12 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
		fontRomanRed12.setColor(BaseColor.RED);
	}
	
	public Calendar getStartDate() {
		return startDate;
	}
	
	private void makeDates(Calendar startDate) {
		this.runDate = Calendar.getInstance(new AnsiTime());
		
		Calendar workDate = (Calendar)startDate.clone();
		workDate.set(Calendar.DAY_OF_MONTH, 1);
		workDate.set(Calendar.HOUR_OF_DAY, 0);
		workDate.set(Calendar.MINUTE, 0);
		workDate.set(Calendar.SECOND, 0);
		workDate.set(Calendar.MILLISECOND, 0);		
		this.startDate = (Calendar)workDate.clone();
		
		workDate.set(Calendar.DAY_OF_MONTH, workDate.getMaximum(Calendar.DAY_OF_MONTH));
		this.endDate = (Calendar)workDate.clone();
		
	}
	
	private PDFBuilder(Connection conn,  Integer divisionId, Calendar startDate, StandardReport report) throws Exception {
		super(report);
		this.divisionId = divisionId;
		this.div = makeDivision(conn, divisionId);
		makeDates(startDate);
		//makeData(conn);		
	}
	
	private String makeDivision(Connection conn, Integer divisionId) throws Exception {
		Division division = new Division();
		division.setDivisionId(divisionId);
		division.selectOne(conn);
		return division.getDivisionNbr() + "-" + division.getDivisionCode();
	}
	

	
	private float drawColumnText(PdfContentByte canvas, Rectangle rect, Element p, boolean simulate) throws DocumentException {
		ColumnText ct = new ColumnText(canvas);
		ct.setSimpleColumn(rect);
		ct.addElement(p);
		ct.go(simulate);
		return ct.getYLine();
	}
	private class Position extends ApplicationObject {
		private static final long serialVersionUID = 1L;
		public float llx;  //lower left x
		public float lly;  //lower left y
		public float urx;  //upper right x
		public float ury;  // upper right y
		public Position(float lowerLeftX, float lowerLeftY, float upperRightX, float upperRightyY) {
			super();
			this.llx = lowerLeftX;
			this.lly = lowerLeftY;
			this.urx = upperRightX;
			this.ury = upperRightyY;
		}
		
	}

	public PDFBuilder(StandardReport report) {
		super(report);
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
	
	
	
	
}
