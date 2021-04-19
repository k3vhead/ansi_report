package com.ansi.scilla.report.htmlTable;

import org.apache.commons.collections.Predicate;

public class SameColumnIndexPredicate implements Predicate {

	private Integer index;
	public SameColumnIndexPredicate(Integer index) {
		this.index = index;
	}
	
	@Override
	public boolean evaluate(Object arg0) {
		HTMLCell cell = (HTMLCell)arg0;
		return cell.getColumnIndex().equals(index);
	}
	
}
