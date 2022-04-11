package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import com.ansi.scilla.common.ApplicationObject;

public class Position extends ApplicationObject {
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
