/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {
	public static void configure(String level){		
		Level l = null;
		try{
			l = Level.parse(level);
		} catch (Exception e){
			l = Level.INFO;
		}
		configure(l);
	}
	
	public static void configure(Level level){
		Logger logger = Logger.getLogger(Log.class.getPackage().getName());
		logger.setUseParentHandlers(false);
		logger.setLevel(level);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		consoleHandler.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				return formatMessage(record)+"\n";
			}
		});
		logger.addHandler(consoleHandler);
	}
	
}
