package com.ansi.scilla.report.test;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.common.utils.PropertyNames;



public class TesterUtils {
	
	
	public TesterUtils() {
		makeLoggers();
	}
	
	public static void makeLoggers() {
		for ( String loggerName : new String[] {"com.thewebthing","org.apache"} ) {
			makeLogger(loggerName, Level.INFO);
		}
		String ansiLoggerName = AppUtils.getProperty(PropertyNames.LOG_NAME);
		makeLogger(ansiLoggerName, Level.DEBUG);
	}

	public static void makeLogger(String loggerName, Level level) {
		Logger logger = Logger.getLogger(loggerName);
		PatternLayout layout = new PatternLayout(AppUtils.getProperty(PropertyNames.LOG_PATTERN));
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.addAppender(consoleAppender);
		consoleAppender.activateOptions();
		logger.setLevel(level);
		
	}
	
	

}
