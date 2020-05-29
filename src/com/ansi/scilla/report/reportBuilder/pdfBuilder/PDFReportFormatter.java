package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.CellStyle;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;

public class PDFReportFormatter extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	public static final Integer fontHeight = 9;
	public static final Integer reportBannerHeight = 14;
	public static final Integer reportTitleHeight = 12;
	public static final Integer reportSubTitleHeight = 10;
	public static final Integer reportNoteHeight = 8;
	public static final Integer reportHeaderBottomSpace = 9;
	public static final float   tableTotalWidth = 732.000024F;  // when a PdfPTable is set to 100% width on a landscape page, this is the absolute width

//	public short standardHeaderHeight = (short)400;    //this may be a good idea at some point, but we're not doing it now
//	public short standardDetailHeight = (short)400;    //this may be a good idea at some point, but we're not doing it now
	
	public static final BaseFont calibri;
	public static final Font fontStandardBlack;
	public static final Font fontStandardBlackBold;
	public static final Font fontStandardWhite;
	public static final Font fontStandardWhiteBold;
	public static final Font fontReportBanner;
	public static final Font fontReportTitle;
	public static final Font fontReportSubTitle;
	public static final Font fontReportNote;
	public static final Font fontSubtotal;
	
	public static final HashMap<DataFormats, Integer> cellStyles;
	public static final SimpleDateFormat dataFormatDate = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat dataFormatDateTime = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	
	public static final float marginLeft = 30F;
	public static final float marginRight = 30F;
	public static final float marginTop = 75F;
	public static final float marginBottom = 36F;
	public static final float marginTopDatadump = 35F;
	
	public static final float headerDefaultPositionX = 34F;
	public static final float headerDefaultPositionY = 612F;
	
	// Dimensions of a 8 1/2 x 11 paper
	public static final float shortSideSize = PageSize.LETTER.getWidth();
	public static final float longSideSize = PageSize.LETTER.getHeight();
	
	
	
//	public short dataFormatDate;
//	public short dataFormatDateTime;
	public short dataFormatDecimal;
	public short dataFormatInteger;
	public short dataFormatNumber;
	public short dataFormatCurrency;
	
	public CellStyle cellStyleColHdrLeft;
	public CellStyle cellStyleColHdrCenter;
	public CellStyle cellStyleStandardLeft;
	public CellStyle cellStyleStandardCenter;
	public CellStyle cellStyleStandardRight;
	public CellStyle cellStyleStandardDecimal;
	public CellStyle cellStyleStandardNumber;
	public CellStyle cellStyleStandardCurrency;
	public CellStyle cellStyleStandardInteger;
	public CellStyle cellStyleNumberCenter;
	public CellStyle cellStyleDateCenter;
	public CellStyle cellStyleDateLeft;
	public CellStyle cellStyleDateTimeLeft;
	public CellStyle cellStyleReportBanner;
	public CellStyle cellStyleReportTitle;
	public CellStyle cellStyleReportSubTitle;
	public CellStyle cellStyleReportHeaderLabelCenter;
	public CellStyle cellStyleReportHeaderLabelLeft;
	public CellStyle cellStyleReportHeaderLabelRight;
	public CellStyle cellStyleReportNote;
	
	
	public CellStyle cellStyleSubtotalDecimal;


	
	static {
		// find ttf files in /usr/share/fonts (linux) or C:\Windows\fonts (windows 10)
		
		;
		try {
			String calibriTTF = PDFReportFormatter.class.getClassLoader().getResource("resources/calibri.ttf").getFile();
			calibri = BaseFont.createFont(calibriTTF, BaseFont.WINANSI, true);
			fontStandardBlack = new Font(calibri, fontHeight);
			fontStandardBlackBold = new Font(calibri, fontHeight, Font.BOLD);
			fontStandardWhite = new Font(calibri, fontHeight);
			fontStandardWhite.setColor(BaseColor.WHITE);
			fontStandardWhiteBold = new Font(calibri, fontHeight, Font.BOLD);
			fontStandardWhiteBold.setColor(BaseColor.WHITE);
			fontReportBanner = new Font(calibri, reportBannerHeight, Font.BOLD);
			fontReportTitle = new Font(calibri, reportTitleHeight, Font.BOLD);
			fontReportSubTitle = new Font(calibri, reportSubTitleHeight, Font.BOLD);
			fontReportNote = new Font(calibri, reportNoteHeight, Font.BOLD);
			fontSubtotal = new Font(calibri, fontHeight, Font.BOLD);
			
			
			
			/** Standard cell alignments, based on data type/format **/
			cellStyles = new HashMap<DataFormats, Integer>();
			
			cellStyles.put(DataFormats.DATE_FORMAT, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.DATE_TIME_FORMAT, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.DETAIL_TIME_FORMAT, Element.ALIGN_LEFT);
			
			cellStyles.put(DataFormats.INTEGER_FORMAT, Element.ALIGN_RIGHT);
			cellStyles.put(DataFormats.NUMBER_FORMAT, Element.ALIGN_RIGHT);
			cellStyles.put(DataFormats.NUMBER_CENTERED, Element.ALIGN_CENTER);
			cellStyles.put(DataFormats.DECIMAL_FORMAT, Element.ALIGN_RIGHT);

			cellStyles.put(DataFormats.CURRENCY_FORMAT, Element.ALIGN_RIGHT);

			cellStyles.put(DataFormats.STRING_FORMAT, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.STRING_CENTERED, Element.ALIGN_CENTER);
			cellStyles.put(DataFormats.STRING_TRUNCATE, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.STRING_ABBREVIATE, Element.ALIGN_LEFT);
		} catch (Exception e) {			
			throw new RuntimeException(e);
		}
		
		
	}
	
	
	public static BaseFont getCalibri() throws DocumentException, IOException {
		String calibriTTF = PDFReportFormatter.class.getClassLoader().getResource("resources/calibri.ttf").getFile();
		BaseFont calibri = BaseFont.createFont(calibriTTF, BaseFont.WINANSI, true);
		return calibri;
	}
	
	
}
