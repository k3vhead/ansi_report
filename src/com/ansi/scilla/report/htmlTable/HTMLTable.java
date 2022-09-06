package com.ansi.scilla.report.htmlTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.reportBuilder.htmlBuilder.HTMLReportFormatter;

public class HTMLTable extends ApplicationObject {

	private static final long serialVersionUID = 1L;
	private List<HTMLRow> rows = new ArrayList<HTMLRow>();
	public List<HTMLRow> getRows() {
		return rows;
	}
	public void setRows(List<HTMLRow> rows) {
		this.rows = rows;
	}
	public HTMLRow createRow(int rownum) {
		HTMLRow duplicate = IterableUtils.find(rows, new SameRowIndexPredicate(rownum));			
		if ( duplicate != null ) {
			rows.remove(duplicate);
		}
		HTMLRow row = new HTMLRow(rownum);
		rows.add(row);
		Collections.sort(rows);
		return row;
	}
	
	public String makeHTML() throws Exception {
		List<HTMLRow> gapRows = new ArrayList<HTMLRow>();
		// make sure there are no gaps in the range of cells
		for ( int index = 0; index < rows.size(); index++ ) {
			long matches = IterableUtils.countMatches(rows, new SameRowIndexPredicate(index));
			if ( matches == 0L ) {
				gapRows.add(new HTMLRow(index));
			}
		}
		this.rows.addAll(gapRows);
		Collections.sort(rows);
		
		List<String> reportLineList = new ArrayList<String>();
		reportLineList.add("<table class=\"" + HTMLReportFormatter.CSS_TABLE + "\">");
		for ( HTMLRow row : rows ) {
			reportLineList.add(row.makeHTML());
		}
		reportLineList.add("</table>");
		return StringUtils.join(reportLineList, "\n");
	}
}

