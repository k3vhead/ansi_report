package com.ansi.scilla.report.reportBuilder;

import com.ansi.scilla.common.ApplicationObject;

public class ReportFieldLayout extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	private FontSize fontSize;
	private FontWeight fontWeight;
	private TextAlign textAlign;
	private Class<?> formatter;
	
	public ReportFieldLayout() {
		super();
	}
	public ReportFieldLayout(FontSize fontSize, FontWeight fontWeight, TextAlign textAlign, Class<?> formatter) {
		this();
		this.fontSize = fontSize;
		this.fontWeight = fontWeight;
		this.textAlign = textAlign;
		this.formatter = formatter;
	}
	public FontSize getFontSize() {
		return fontSize;
	}
	public void setFontSize(FontSize fontSize) {
		this.fontSize = fontSize;
	}
	public FontWeight getFontWeight() {
		return fontWeight;
	}
	public void setFontWeight(FontWeight fontWeight) {
		this.fontWeight = fontWeight;
	}
	public TextAlign getTextAlign() {
		return textAlign;
	}
	public void setTextAlign(TextAlign textAlign) {
		this.textAlign = textAlign;
	}
	public Class<?> getFormatter() {
		return formatter;
	}
	public void setFormatter(Class<?> formatter) {
		this.formatter = formatter;
	}

	public enum FontSize {
		FONTSIZE_LARGE,
		FONTSIZE_STANDARD,
		FONTSIZE_SMALL;
	}
	
	public enum FontWeight {
		FONTWEIGHT_BOLD,
		FONTWEIGHT_STANDARD,
		FONTWEIGHT_LIGHT;
	}
	
	public enum TextAlign {
		TEXTALIGN_LEFT,
		TEXTALIGN_CENTER,
		TEXTALIGN_RIGHT;
	}
}
