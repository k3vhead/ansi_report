package com.ansi.scilla.report.reportBuilder.common;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportFormatter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;

/**
 * A generic table cell formatting object that we can use to create a formatter for XLS, PDF or HTML.
 * 
 * @author dclewis
 *
 */
public class CustomCellFormat extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	private String xlsDataFormat;
	private String htmlDataFormat;
	private CustomCellAlignment alignment;
	private CustomCellColor background;
	private CustomCellColor foreground;
	private Double fontHeight = 8.0D;
	private Boolean bold = false;	
	private Boolean borderTop = false;
	private Boolean borderRight = false;
	private Boolean borderBottom = false;
	private Boolean borderLeft = false;
	private Boolean underline = false;
	
	public CustomCellFormat() {
		super();
	}

	public CustomCellFormat(CustomCellAlignment alignment ) {
		this();
		this.alignment = alignment;
	}
	
	public CustomCellFormat(CustomCellColor foreground, CustomCellColor background, CustomCellAlignment alignment) {
		this(alignment);
		this.background = background;
		this.foreground = foreground;
	}
	
	public CustomCellFormat(CustomCellColor foreground, CustomCellColor background, CustomCellAlignment alignment, String xlsDataFormat, String htmlDataFormat) {
		this(foreground, background, alignment);
		this.xlsDataFormat = xlsDataFormat;
		this.htmlDataFormat = htmlDataFormat;
	}

	public CustomCellFormat(CellStyle cellStyle) {
		this();
		this.alignment = CustomCellAlignment.fromXlsStyle(cellStyle);
		if ( ! StringUtils.isBlank(cellStyle.getDataFormatString()) ) {
			this.xlsDataFormat = cellStyle.getDataFormatString();
		}
		this.background = CustomCellColor.fromIndexedColor(cellStyle.getFillBackgroundColor());
		this.foreground = CustomCellColor.fromHssfColor(cellStyle.getFillForegroundColor());
	}
	
	public String getXlsDataFormat() {
		return xlsDataFormat;
	}

	public void setXlsDataFormat(String xlsDataFormat) {
		this.xlsDataFormat = xlsDataFormat;
	}

	public String getHtmlDataFormat() {
		return htmlDataFormat;
	}

	public void setHtmlDataFormat(String htmlDataFormat) {
		this.htmlDataFormat = htmlDataFormat;
	}

	public CustomCellAlignment getAlignment() {
		return alignment;
	}

	public void setAlignment(CustomCellAlignment alignment) {
		this.alignment = alignment;
	}

	public CustomCellColor getBackground() {
		return background;
	}

	public void setBackground(CustomCellColor background) {
		this.background = background;
	}

	public CustomCellColor getForeground() {
		return foreground;
	}

	public void setForeground(CustomCellColor foreground) {
		this.foreground = foreground;
	}

	public Double getFontHeight() {
		return fontHeight;
	}

	public void setFontHeight(Double fontHeight) {
		this.fontHeight = fontHeight;
	}

	public Boolean getBold() {
		return bold;
	}

	public void setBold(Boolean bold) {
		this.bold = bold;
	}

	public Boolean getBorderTop() {
		return borderTop;
	}

	public void setBorderTop(Boolean borderTop) {
		this.borderTop = borderTop;
	}

	public Boolean getBorderRight() {
		return borderRight;
	}

	public void setBorderRight(Boolean borderRight) {
		this.borderRight = borderRight;
	}

	public Boolean getBorderBottom() {
		return borderBottom;
	}

	public void setBorderBottom(Boolean borderBottom) {
		this.borderBottom = borderBottom;
	}

	public Boolean getBorderLeft() {
		return borderLeft;
	}

	public void setBorderLeft(Boolean borderLeft) {
		this.borderLeft = borderLeft;
	}

	public Boolean getUnderline() {
		return underline;
	}

	public void setUnderline(Boolean underline) {
		this.underline = underline;
	}

	public void setBorder(Boolean border) {
		this.borderTop = border;
		this.borderRight = border;
		this.borderBottom = border;
		this.borderLeft = border;
	}
	
	public CellStyle makeXlsStyle(XSSFWorkbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		if ( this.background == null ) {
			cellStyle.setFillBackgroundColor(CustomCellColor.AUTOMATIC.indexedColors().getIndex());
		} else {
//			cellStyle.setFillBackgroundColor(background.indexedColors().getIndex());
			cellStyle.setFillForegroundColor(background.indexedColors().getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		}
		if ( this.alignment == null ) {
			cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
		} else {
			cellStyle.setAlignment(alignment.xlsAlignment());
		}
		XSSFFont font = workbook.createFont();
		if ( foreground == null ) {
			font.setColor(HSSFColor.BLACK.index);
		} else {
			font.setColor(foreground.hssfColor());
		}
		if ( fontHeight == null ) {
			font.setFontHeight(9);
		} else {
			font.setFontHeight(fontHeight);
		}
		if ( this.bold ) {
			font.setBold(true);
		}
		if ( this.underline ) {
			font.setUnderline(FontUnderline.SINGLE);
		}
		cellStyle.setFont(font);
		if ( this.borderTop ) {
			cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		}
		if ( this.borderRight ) {
			cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		}
		if ( this.borderBottom ) {
			cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		}
		if ( this.borderLeft ) {
			cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		}

		if ( ! StringUtils.isBlank(this.xlsDataFormat) ) {
			CreationHelper createHelper = workbook.getCreationHelper();	
			short dataFormat = createHelper.createDataFormat().getFormat(this.xlsDataFormat);
			cellStyle.setDataFormat(dataFormat);
		}
	    return cellStyle;
	}
	
	public String makeHtmlTextStyle() {
		StringBuffer cellStyle = new StringBuffer();
	
		if ( foreground == null ) {
			cellStyle.append("color:" + CustomCellColor.BLACK.htmlColor() + ";");
		} else {
			cellStyle.append("color:" + foreground.htmlColor() + ";");
		}
		if ( fontHeight != null ) {
			Double htmlFontHeight = fontHeight * 1.77D;
			cellStyle.append("font-size:" + htmlFontHeight.intValue() + ";");
		}
		if ( this.bold ) {
			cellStyle.append("font-weight:bold;");
		}
		if ( this.underline ) {
			cellStyle.append("text-decoration:underline;");
		}
	
	    return cellStyle.toString();
	}

	public String makeHtmlCellStyle() {
		StringBuffer cellStyle = new StringBuffer();
		
		if ( this.background == null ) {
			cellStyle.append("background-color:transparent;");
		} else {
			cellStyle.append("background-color:" + background.htmlColor() + ";");
		}
		if ( this.alignment == null ) {
			cellStyle.append("text-align:left;");
		} else {
			cellStyle.append("text-align:" + alignment.htmlAlignment() + ";");
		}		
		if ( this.borderTop ) {
			cellStyle.append("border-top:1px solid #000000;");
		}
		if ( this.borderRight ) {
			cellStyle.append("border-right:1px solid #000000;");
		}
		if ( this.borderBottom ) {
			cellStyle.append("border-bottom:1px solid #000000;");
		}
		if ( this.borderLeft ) {
			cellStyle.append("border-left:1px solid #000000;");
		}
	
		
	    return cellStyle.toString();
	}

	public String formatValueAsText(Object value) throws Exception {
		String formattedValue = null;
		Logger logger = LogManager.getLogger(this.getClass());
		if ( value instanceof String ) {
			formattedValue = (String)value;
//			logger.log(Level.DEBUG, "String: " + value + "\t" + value.getClass().getName() + "\t" + formattedValue);
		} else if ( value instanceof Date ) {
			SimpleDateFormat sdf = new SimpleDateFormat(this.htmlDataFormat);
			formattedValue = sdf.format((Date)value);
//			logger.log(Level.DEBUG, "Date: " + value + "\t" + value.getClass().getName() + "\t" + formattedValue);
		} else if ( value instanceof Calendar ) {
			SimpleDateFormat sdf = new SimpleDateFormat(this.htmlDataFormat);
			formattedValue = sdf.format(((Calendar)value).getTime());
//			logger.log(Level.DEBUG, "Calendar: " + value + "\t" + value.getClass().getName() + "\t" + formattedValue);
		} else if ( value instanceof Integer ) {
			DecimalFormat df = new DecimalFormat(this.htmlDataFormat);
			formattedValue = df.format((Integer)value);
//			logger.log(Level.DEBUG, "Integer: " + value + "\t" + value.getClass().getName() + "\t" + formattedValue);
		} else if ( value instanceof Double ) {
			DecimalFormat df = new DecimalFormat(this.htmlDataFormat);
			formattedValue = df.format((Double)value);
//			logger.log(Level.DEBUG, "Double: " + value + "\t" + value.getClass().getName() + "\t" + formattedValue);
		} else if ( value instanceof Float ) {
			DecimalFormat df = new DecimalFormat(this.htmlDataFormat);
			formattedValue = df.format((Float)value);
//			logger.log(Level.DEBUG, "Float: " + value + "\t" + value.getClass().getName() + "\t" + formattedValue);
		} else if ( value instanceof Long ) {
			DecimalFormat df = new DecimalFormat(this.htmlDataFormat);
			formattedValue = df.format((Long)value);
//			logger.log(Level.DEBUG, "Long: " + value + "\t" + value.getClass().getName() + "\t" + formattedValue);
		} else {
			/* If you're here because you got a "Need formatter" exception, you need to add another else */
			logger.log(Level.INFO, "Uncoded html formatter for class " + value.getClass().getName());
			throw new Exception("Need a formatter for " + value.getClass().getName());
		}
		return formattedValue;
	}

	// TODO: Add make pdf style
	
	
	public static CustomCellFormat defaultFormat() {
		CustomCellFormat format = new CustomCellFormat();
		format.setAlignment(CustomCellAlignment.LEFT);
		format.setBackground(CustomCellColor.WHITE);
		format.setForeground(CustomCellColor.BLACK);
		format.setFontHeight(9.0D);
		return format;
	}

	public PdfPCell formatPdfCell(PdfPCell pdfCell) {
		pdfCell.setVerticalAlignment(Element.ALIGN_TOP);
		pdfCell.setIndent(0F);
		pdfCell.setPaddingTop(4F);
		pdfCell.setPaddingBottom(4F);
		pdfCell.setHorizontalAlignment(alignment.pdfAlignment());
		if ( borderTop || borderBottom || borderLeft || borderRight ) {
			if ( borderTop ) {
				pdfCell.setBorderColorTop(BaseColor.BLACK);
				pdfCell.setBorderWidthTop(1F);
			}
			if ( borderBottom ) {
				pdfCell.setBorderColorBottom(BaseColor.BLACK);
				pdfCell.setBorderWidthBottom(1F);
			}
			if ( borderLeft ) {
				pdfCell.setBorderColorLeft(BaseColor.BLACK);
				pdfCell.setBorderWidthLeft(1F);
			}
			if ( borderRight ) {
				pdfCell.setBorderColorRight(BaseColor.BLACK);
				pdfCell.setBorderWidthRight(1F);
			}
		} else {
			pdfCell.setBorder(Rectangle.NO_BORDER);			
		}
		if ( background != null ) {
			if ( background.equals(CustomCellColor.AUTOMATIC) ) {
				pdfCell.setBackgroundColor(BaseColor.WHITE);
			} else {
				pdfCell.setBackgroundColor(background.pdfColor());
			}
		}
	
		return pdfCell;
	}

	public Chunk makePdfDisplay(String display) throws DocumentException, IOException {
		BaseFont calibri = PDFReportFormatter.getArial();		
		Font myFont = this.bold ? new Font(calibri, fontHeight.floatValue(), Font.BOLD) : new Font(calibri, fontHeight.floatValue());
		if ( foreground != null ) {
			if ( foreground.equals(CustomCellColor.AUTOMATIC ) ) {
				myFont.setColor(BaseColor.BLACK);
			} else {
				myFont.setColor(foreground.pdfColor());
			}
		}
		Chunk chunk = new Chunk(display, myFont);
		if ( underline ) {
			chunk.setUnderline(0.1f, -2f);
		}
		return chunk;
	}

	@Override
	public CustomCellFormat clone() throws CloneNotSupportedException {
		CustomCellFormat format = new CustomCellFormat();
		try {
			BeanUtils.copyProperties(format, this);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Error in copying properties");
		}
		return format;
	}


	public enum CustomCellAlignment { 
		LEFT(CellStyle.ALIGN_LEFT, "left", Element.ALIGN_LEFT), 
		CENTER(CellStyle.ALIGN_CENTER, "center", Element.ALIGN_CENTER), 
		RIGHT(CellStyle.ALIGN_RIGHT, "right", Element.ALIGN_RIGHT),
		; 
		private short xlsAlignment;
		private String htmlAlignment;
		private int pdfAlignment;
		
		private CustomCellAlignment(short xlsAlignment, String htmlAlignment, int pdfAlignment) { 
			this.xlsAlignment = xlsAlignment; 
			this.htmlAlignment = htmlAlignment;
			this.pdfAlignment = pdfAlignment;
		}		
		
		public short  xlsAlignment()  { return this.xlsAlignment;  }
		public String htmlAlignment() { return this.htmlAlignment; }
		public int    pdfAlignment()  { return this.pdfAlignment;  }
		
		public static CustomCellAlignment fromXlsStyle(CellStyle cellStyle) {
			CustomCellAlignment alignment = null;
			switch ( cellStyle.getAlignment() ) {
				case CellStyle.ALIGN_LEFT: 
					alignment = LEFT;
					break;
				case CellStyle.ALIGN_RIGHT:
					alignment = RIGHT;
					break;
				case CellStyle.ALIGN_CENTER:
					alignment = CENTER;
					break;
				default:
					alignment = LEFT;
					break;
			}
			return alignment;				
		}
	}
	
	public enum CustomCellColor {
		AUTOMATIC(IndexedColors.AUTOMATIC, HSSFColor.AUTOMATIC.index, "transparent"),

		BLACK(IndexedColors.BLACK, HSSFColor.BLACK.index, "#000000"),
		BLUE(IndexedColors.BLUE, HSSFColor.BLUE.index, "#0000FF"),
		BRIGHT_GREEN(IndexedColors.BRIGHT_GREEN, HSSFColor.BRIGHT_GREEN.index, "#00FF00"),
		CORNFLOWER_BLUE(IndexedColors.CORNFLOWER_BLUE, HSSFColor.CORNFLOWER_BLUE.index, "#6495ED"),
		DARK_BLUE(IndexedColors.DARK_BLUE, HSSFColor.DARK_BLUE.index, "00008B"),
		DARK_RED(IndexedColors.DARK_RED, HSSFColor.DARK_RED.index, "#8B0000"),
		DARK_YELLOW(IndexedColors.DARK_YELLOW, HSSFColor.DARK_YELLOW.index, "#FFCC00"),
		GREEN(IndexedColors.GREEN, HSSFColor.GREEN.index, "#008000"),
		GREY_25_PERCENT(IndexedColors.GREY_25_PERCENT, HSSFColor.GREY_25_PERCENT.index, "#778899"),
		GREY_50_PERCENT(IndexedColors.GREY_50_PERCENT, HSSFColor.GREY_50_PERCENT.index, "#808080"),
		ORANGE(IndexedColors.ORANGE, HSSFColor.ORANGE.index, "#FF8C00"),
		PINK(IndexedColors.PINK, HSSFColor.PINK.index, "#FFC0CB"),
		RED(IndexedColors.RED, HSSFColor.RED.index, "#FF0000"),
		TEAL(IndexedColors.TEAL, HSSFColor.TEAL.index, "#008080"),
		TURQUOISE(IndexedColors.TURQUOISE, HSSFColor.TURQUOISE.index, "#40E0D0"),
		VIOLET(IndexedColors.VIOLET, HSSFColor.VIOLET.index, "#EE82EE"),
		WHITE(IndexedColors.WHITE, HSSFColor.WHITE.index, "#FFFFFF"),
		YELLOW(IndexedColors.YELLOW, HSSFColor.YELLOW.index, "#FFFF00"),

		
//		MAROON(IndexedColors.MAROON, HSSFColor.YELLOW.index),
//		LEMON_CHIFFON(IndexedColors.LEMON_CHIFFON, HSSFColor.YELLOW.index),
//		ORCHID(IndexedColors.ORCHID, HSSFColor.YELLOW.index),
//		CORAL(IndexedColors.CORAL, HSSFColor.YELLOW.index),
//		ROYAL_BLUE(IndexedColors.ROYAL_BLUE, HSSFColor.YELLOW.index),
//		LIGHT_CORNFLOWER_BLUE(IndexedColors.LIGHT_CORNFLOWER_BLUE, HSSFColor.YELLOW.index),
//		SKY_BLUE(IndexedColors.SKY_BLUE, HSSFColor.YELLOW.index),
//		LIGHT_TURQUOISE(IndexedColors.LIGHT_TURQUOISE, HSSFColor.YELLOW.index),
//		LIGHT_GREEN(IndexedColors.LIGHT_GREEN, HSSFColor.YELLOW.index),
//		LIGHT_YELLOW(IndexedColors.LIGHT_YELLOW, HSSFColor.YELLOW.index),
//		PALE_BLUE(IndexedColors.PALE_BLUE, HSSFColor.YELLOW.index),
//		ROSE(IndexedColors.ROSE, HSSFColor.YELLOW.index),
//		LAVENDER(IndexedColors.LAVENDER, HSSFColor.YELLOW.index),
//		TAN(IndexedColors.TAN, HSSFColor.YELLOW.index),
//		LIGHT_BLUE(IndexedColors.LIGHT_BLUE, HSSFColor.YELLOW.index),
//		AQUA(IndexedColors.AQUA, HSSFColor.YELLOW.index),
//		LIME(IndexedColors.LIME, HSSFColor.YELLOW.index),
//		GOLD(IndexedColors.GOLD, HSSFColor.YELLOW.index),
//		LIGHT_ORANGE(IndexedColors.LIGHT_ORANGE, HSSFColor.YELLOW.index),
//		BLUE_GREY(IndexedColors.BLUE_GREY, HSSFColor.YELLOW.index),
//		GREY_40_PERCENT(IndexedColors.GREY_40_PERCENT, HSSFColor.YELLOW.index),
//		DARK_TEAL(IndexedColors.DARK_TEAL, HSSFColor.YELLOW.index),
//		SEA_GREEN(IndexedColors.SEA_GREEN, HSSFColor.YELLOW.index),
//		DARK_GREEN(IndexedColors.DARK_GREEN, HSSFColor.YELLOW.index),
//		OLIVE_GREEN(IndexedColors.OLIVE_GREEN, HSSFColor.YELLOW.index),
//		BROWN(IndexedColors.BROWN, HSSFColor.YELLOW.index),
//		PLUM(IndexedColors.PLUM, HSSFColor.YELLOW.index),
//		INDIGO(IndexedColors.INDIGO, HSSFColor.YELLOW.index),
//		GREY_80_PERCENT(IndexedColors.GREY_80_PERCENT, HSSFColor.YELLOW.index),
		;
		
		private IndexedColors indexedColors;
		private short hssfColor;
		private String htmlColor;
		private CustomCellColor(IndexedColors indexedColors, short hssfColor, String htmlColor) {
			this.indexedColors = indexedColors;
			this.hssfColor = hssfColor;
			this.htmlColor = htmlColor;
		}
		

		public IndexedColors indexedColors() { return indexedColors;  }
		public short         hssfColor()     { return this.hssfColor; }	
		public String        htmlColor()     { return this.htmlColor; }
		
		public BaseColor pdfColor() { 
			BaseColor color = null;
			try {
				color = new BaseColor(
					Integer.valueOf( htmlColor().substring(1,3),16),
					Integer.valueOf( htmlColor().substring(3,5),16),
					Integer.valueOf( htmlColor().substring(5,7),16)
				);
			} catch ( Exception e) {
				color = BaseColor.WHITE;
			}
			return color;
		}
		
		public static CustomCellColor fromIndexedColor(short indexedColor) {
			CustomCellColor customCellColor = null;
			for ( CustomCellColor color : CustomCellColor.values() ) {
				if ( color.indexedColors().index == indexedColor ) {
					customCellColor = color;
				}
			}
			return customCellColor;
		}
		public static CustomCellColor fromHssfColor(short hssfColor) {
			CustomCellColor customCellColor = null;
			for ( CustomCellColor color : CustomCellColor.values() ) {
				if ( color.hssfColor() == hssfColor ) {
					customCellColor = color;
				}
			}
			return customCellColor;
		}
	}
}
