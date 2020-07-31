package com.ansi.scilla.report.reportBuilder.common;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.ApplicationObject;

public class CustomCellFormat extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	private String dataFormat;
	private CustomCellAlignment alignment;
	private CustomCellColor background;
	private CustomCellColor foreground;
	private Double fontHeight = 9.0D;
	private Boolean bold = false;
	
	
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
	
	public CustomCellFormat(CustomCellColor foreground, CustomCellColor background, CustomCellAlignment alignment, String dataFormat) {
		this(foreground, background, alignment);
		this.dataFormat = dataFormat;
	}

	public CustomCellFormat(CellStyle cellStyle) {
		this();
		this.alignment = CustomCellAlignment.fromXlsStyle(cellStyle);
		if ( ! StringUtils.isBlank(cellStyle.getDataFormatString()) ) {
			this.dataFormat = cellStyle.getDataFormatString();
		}
		this.background = CustomCellColor.fromIndexedColor(cellStyle.getFillBackgroundColor());
		this.foreground = CustomCellColor.fromHssfColor(cellStyle.getFillForegroundColor());
	}
	
	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
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
		cellStyle.setFont(font);
		if ( ! StringUtils.isBlank(this.dataFormat) ) {
			CreationHelper createHelper = workbook.getCreationHelper();	
			short dataFormat = createHelper.createDataFormat().getFormat(this.dataFormat);
			cellStyle.setDataFormat(dataFormat);
		}
	    return cellStyle;
	}
	
	
	public static CustomCellFormat defaultFormat() {
		CustomCellFormat format = new CustomCellFormat();
		format.setAlignment(CustomCellAlignment.LEFT);
		format.setBackground(CustomCellColor.WHITE);
		format.setForeground(CustomCellColor.BLACK);
		format.setFontHeight(9.0D);
		return format;
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
		LEFT(CellStyle.ALIGN_LEFT), 
		CENTER(CellStyle.ALIGN_CENTER), 
		RIGHT(CellStyle.ALIGN_RIGHT),
		; 
		private short xlsAlignment;
		private CustomCellAlignment(short xlsAlignment) { 
			this.xlsAlignment = xlsAlignment; 
		}		
		public short xlsAlignment() { return this.xlsAlignment; }
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
		AUTOMATIC(IndexedColors.AUTOMATIC, HSSFColor.AUTOMATIC.index),

		BLACK(IndexedColors.BLACK, HSSFColor.BLACK.index),
		BLUE(IndexedColors.BLUE, HSSFColor.BLUE.index),
		BRIGHT_GREEN(IndexedColors.BRIGHT_GREEN, HSSFColor.BRIGHT_GREEN.index),
		CORNFLOWER_BLUE(IndexedColors.CORNFLOWER_BLUE, HSSFColor.CORNFLOWER_BLUE.index),
		DARK_BLUE(IndexedColors.DARK_BLUE, HSSFColor.DARK_BLUE.index),
		DARK_RED(IndexedColors.DARK_RED, HSSFColor.DARK_RED.index),
		DARK_YELLOW(IndexedColors.DARK_YELLOW, HSSFColor.DARK_YELLOW.index),
		GREEN(IndexedColors.GREEN, HSSFColor.GREEN.index),
		GREY_25_PERCENT(IndexedColors.GREY_25_PERCENT, HSSFColor.GREY_25_PERCENT.index),
		GREY_50_PERCENT(IndexedColors.GREY_50_PERCENT, HSSFColor.GREY_50_PERCENT.index),
		ORANGE(IndexedColors.ORANGE, HSSFColor.ORANGE.index),
		PINK(IndexedColors.PINK, HSSFColor.PINK.index),
		RED(IndexedColors.RED, HSSFColor.RED.index),
		TEAL(IndexedColors.TEAL, HSSFColor.TEAL.index),
		TURQUOISE(IndexedColors.TURQUOISE, HSSFColor.TURQUOISE.index),
		VIOLET(IndexedColors.VIOLET, HSSFColor.VIOLET.index),
		WHITE(IndexedColors.WHITE, HSSFColor.WHITE.index),
		YELLOW(IndexedColors.YELLOW, HSSFColor.YELLOW.index),

		
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
		private CustomCellColor(IndexedColors indexedColors, short hssfColor) {
			this.indexedColors = indexedColors;
			this.hssfColor = hssfColor;
		}
		public IndexedColors indexedColors() { return indexedColors; }
		public short hssfColor() { return this.hssfColor; }		
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
