package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
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

	public static final Integer fontHeight = 8;
	public static final Integer reportBannerHeight = 14;
	public static final Integer reportTitleHeight = 12;
	public static final Integer reportSubTitleHeight = 10;
	public static final Integer reportNoteHeight = 8;
	public static final Integer reportHeaderBottomSpace = 9;
	public static final float   tableTotalWidth = 732.000024F;  // when a PdfPTable is set to 100% width on a landscape page, this is the absolute width

//	public short standardHeaderHeight = (short)400;    //this may be a good idea at some point, but we're not doing it now
//	public short standardDetailHeight = (short)400;    //this may be a good idea at some point, but we're not doing it now
	
	public static final BaseFont arial;
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
	public short dataFormatPct;
	public short dataFormatCurrency;
	
	public CellStyle cellStyleColHdrLeft;
	public CellStyle cellStyleColHdrCenter;
	public CellStyle cellStyleStandardLeft;
	public CellStyle cellStyleStandardCenter;
	public CellStyle cellStyleStandardRight;
	public CellStyle cellStyleStandardDecimal;
	public CellStyle cellStyleStandardNumber;
	public CellStyle cellStyleStandardPct;
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
			arial = getArial();
			fontStandardBlack = new Font(arial, fontHeight);
			fontStandardBlackBold = new Font(arial, fontHeight, Font.BOLD);
			fontStandardWhite = new Font(arial, fontHeight);
			fontStandardWhite.setColor(BaseColor.WHITE);
			fontStandardWhiteBold = new Font(arial, fontHeight, Font.BOLD);
			fontStandardWhiteBold.setColor(BaseColor.WHITE);
			fontReportBanner = new Font(arial, reportBannerHeight, Font.BOLD);
			fontReportTitle = new Font(arial, reportTitleHeight, Font.BOLD);
			fontReportSubTitle = new Font(arial, reportSubTitleHeight, Font.BOLD);
			fontReportNote = new Font(arial, reportNoteHeight, Font.BOLD);
			fontSubtotal = new Font(arial, fontHeight, Font.BOLD);
			
			
			
			/** Standard cell alignments, based on data type/format **/
			cellStyles = new HashMap<DataFormats, Integer>();
			
			cellStyles.put(DataFormats.DATE_FORMAT, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.DATE_TIME_FORMAT, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.DETAIL_TIME_FORMAT, Element.ALIGN_LEFT);
			
			cellStyles.put(DataFormats.INTEGER_FORMAT, Element.ALIGN_RIGHT);
			cellStyles.put(DataFormats.NUMBER_FORMAT, Element.ALIGN_RIGHT);
			cellStyles.put(DataFormats.NUMBER_CENTERED, Element.ALIGN_CENTER);
			cellStyles.put(DataFormats.NUMBER_LEFT, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.DECIMAL_FORMAT, Element.ALIGN_RIGHT);

			cellStyles.put(DataFormats.PCT_FORMAT, Element.ALIGN_RIGHT);
			cellStyles.put(DataFormats.CURRENCY_FORMAT, Element.ALIGN_RIGHT);

			cellStyles.put(DataFormats.STRING_FORMAT, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.STRING_CENTERED, Element.ALIGN_CENTER);
			cellStyles.put(DataFormats.STRING_TRUNCATE, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.STRING_ABBREVIATE, Element.ALIGN_LEFT);
			
			cellStyles.put(DataFormats.STRING_WRAP_LEFT, Element.ALIGN_LEFT);
			cellStyles.put(DataFormats.STRING_WRAP_CENTERED, Element.ALIGN_CENTER);
			cellStyles.put(DataFormats.STRING_WRAP_RIGHT, Element.ALIGN_RIGHT);
		} catch (Exception e) {			
			throw new RuntimeException(e);
		}
		
		
	}
	
	
	/**
	 * To create a BaseFont we need the name of a TTF file (there are options but we don't care). When the TTF file
	 * is embedded in a JAR file as a resource, the iText utility can't read it, because there is no distinct file
	 * in the OS file system. So, we dive into the morass. 
	 * 
	 * First, check the standard font installation directory for the run-time operating system. If the TTF file is
	 * not in that directory, check the Java temp io directory. If the TTF file is not in that directory, copy the 
	 * content of the resource file into the temp directory and provide iText with that filename.
	 * 
	 * @return BaseFont (not a Font -- they're different) for Calibri.ttf
	 * @throws DocumentException
	 * @throws IOException
	 */
//	public static BaseFont getCalibri() throws DocumentException, IOException {
//		String fontFile = null;
//		if ( SystemUtils.IS_OS_LINUX ) {
//			File file = new File("/usr/share/fonts/calibri.ttf");
//			if ( file.exists() && file.canRead() ) {
//				fontFile = "/usr/share/fonts/calibri.ttf";
//			}
//		} else if ( SystemUtils.IS_OS_WINDOWS ) {
//			File file = new File("C:\\Windows\\fonts\\calibri.ttf");
//			if ( file.exists() && file.canRead() ) {
//				fontFile = "C:\\Windows\\fonts\\calibri.ttf";
//			}
//		}
//
//		if ( fontFile == null ) {
//			File tempDir = SystemUtils.getJavaIoTmpDir();
//			if ( ! tempDir.exists() ) {
//				tempDir.mkdirs();
//			}
//			File ttfFile = new File(tempDir.getName() + File.separator + "calibri.ttf");
//			if ( ! ttfFile.exists() ) {
//				InputStream is = PDFReportFormatter.class.getClassLoader().getResourceAsStream("resources/calibri.ttf");
//				byte[] data = IOUtils.toByteArray(is);
//				FileUtils.writeByteArrayToFile(ttfFile, data);
//			}
//			fontFile = ttfFile.getAbsolutePath();
//		}
//				
//		BaseFont calibri = BaseFont.createFont(fontFile, BaseFont.WINANSI, true);	
//		return calibri;
//	}

	public static BaseFont getArial() throws DocumentException, IOException {
		String fontFile = null;
		if ( SystemUtils.IS_OS_LINUX ) {
			File file = new File("/usr/share/fonts/arial.ttf");
			if ( file.exists() && file.canRead() ) {
				fontFile = "/usr/share/fonts/arial.ttf";
			}
		} else if ( SystemUtils.IS_OS_WINDOWS ) {
			File file = new File("C:\\Windows\\fonts\\arial.ttf");
			if ( file.exists() && file.canRead() ) {
				fontFile = "C:\\Windows\\fonts\\arial.ttf";
			}
		}

		if ( fontFile == null ) {
			File tempDir = SystemUtils.getJavaIoTmpDir();
			if ( ! tempDir.exists() ) {
				tempDir.mkdirs();
			}
			File ttfFile = new File(tempDir.getName() + File.separator + "arial.ttf");
			if ( ! ttfFile.exists() ) {
				InputStream is = PDFReportFormatter.class.getClassLoader().getResourceAsStream("arial/calibri.ttf");
				byte[] data = IOUtils.toByteArray(is);
				FileUtils.writeByteArrayToFile(ttfFile, data);
			}
			fontFile = ttfFile.getAbsolutePath();
		}
				
		BaseFont arial = BaseFont.createFont(fontFile, BaseFont.WINANSI, true);	
		return arial;
	}
	
	
	
}
