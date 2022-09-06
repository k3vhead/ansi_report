package com.ansi.scilla.report.htmlTable;

import org.apache.commons.collections4.Predicate;

public class SameColumnIndexPredicate implements Predicate<HTMLCell> {

	private Integer index;
	public SameColumnIndexPredicate(Integer index) {
		this.index = index;
	}
	
	@Override
	public boolean evaluate(HTMLCell cell) {
		return cell.getColumnIndex().equals(index);
	}
	
}
