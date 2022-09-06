package com.ansi.scilla.report.htmlTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.reportBuilder.htmlBuilder.HTMLReportFormatter;

public class HTMLRow extends ApplicationObject implements Comparable<HTMLRow> {

	private static final long serialVersionUID = 1L;
	private Integer rowNum;
	private List<HTMLCell> cells = new ArrayList<HTMLCell>();
	
	public HTMLRow(Integer rowNum) {
		this.rowNum = rowNum;
	}
	
	public String makeHTML() throws Exception {
		List<HTMLCell> gapCells = new ArrayList<HTMLCell>();
		// make sure there are no gaps in the range of cells
		for ( int index = 0; index < cells.size(); index++ ) {
			long matches = IterableUtils.countMatches(cells, new SameColumnIndexPredicate(index));
			if ( matches == 0L ) {
				gapCells.add(new HTMLCell(index));
			}
		}
		this.cells.addAll(gapCells);
		Collections.sort(cells);			
		StringBuffer buffer = new StringBuffer();
		buffer.append("<tr class=\"" + HTMLReportFormatter.CSS_DATA_ROW + "\">\n");
		for ( HTMLCell cell : cells ) {
			buffer.append(cell.makeHTML());
		}
		buffer.append("</tr>\n");
		return buffer.toString();
	}

	public HTMLCell createCell(Integer columnIndex) {
		HTMLCell duplicate = IterableUtils.find(cells, new SameColumnIndexPredicate(columnIndex));			
		if ( duplicate != null ) {
			cells.remove(duplicate);
		}

		HTMLCell cell = new HTMLCell(columnIndex);
		cells.add(cell);
		Collections.sort(cells);
		return cell;
	}

	public List<HTMLCell> getCells() {
		return cells;
	}
	public void setCells(List<HTMLCell> cells) {
		this.cells = cells;
	}

	public Integer getRowNum() {
		return rowNum;
	}

	public void setRowNum(Integer rowNum) {
		this.rowNum = rowNum;
	}

	@Override
	public int compareTo(HTMLRow o) {
		return this.getRowNum().compareTo(o.rowNum);
	}
	
}