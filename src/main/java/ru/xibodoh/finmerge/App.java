/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.xibodoh.finmerge.financisto.BackupFile;

public class App {

	private static final Logger logger = Logger.getLogger(App.class.getName());
	
	private static final String USAGE = "Usage: java "+App.class.getName()+" [-logLevel level] {-in inputFile} [-out outputFile]\n"+
				"\t level - logging level: SEVERE,WARNING,INFO,FINE,FINER,FINEST,ALL\n"+
				"\t inputFile - input file or folder name\n"+
				"\t\tIf folder is given, the most recent backup file in it will be selected.\n"+
				"\t outputFile - output file name, where to write merge result.\n"+
				"\t\tIf omitted, the result will be written in every input folder.\n"+
				"\t\tIf there are no input folders, the result will be written to current folder.";
	
	public static void main( String[] args ) {
    	String logLevel = null;
    	List<File> inputFiles = new ArrayList<File>();
    	String outputName = null;
    	for(int i=0; i<args.length; i++){
    		String arg = args[i];
    		if ("-logLevel".equals(arg) && i+1<args.length){
    			logLevel = args[++i];
    		} else if ("-in".equals(arg) && i+1<args.length){
    			inputFiles.add(new File(args[++i]));    			
    		} else if ("-out".equals(arg) && i+1<args.length) {
    			outputName = args[++i];
    		}
    	}
    	
    	Log.configure(logLevel);
    	if (inputFiles.isEmpty()){
    		logger.info(USAGE);
    		return;
    	}
    	
    	List<File> outputFolders = new ArrayList<File>();
    	if (outputName!=null) {
    		outputFolders.add(new File(outputName).getParentFile());
    	}
    	BackupFile result = null;
    	String minInputName = null;
    	for (File file: inputFiles){
    		if (file.isDirectory()){
    			if (outputName==null){
    				outputFolders.add(file);
    			}
    			file = findMostRecentBackupFile(file);
    		}
    		
    		if (file.isFile()){
    			if (minInputName==null || minInputName.compareTo(file.getName())>0){
    				minInputName = file.getName();
    			}
    			try {
					BackupFile backupFile = new BackupFile(file);
					logger.log(Level.INFO, "Loaded file {0}", file.getAbsolutePath());
					if (result==null){
						result = backupFile;
					} else {
						result.merge(backupFile);
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, "Failed to read file {0}: {1}", new Object[]{file.getAbsolutePath(), e.getMessage()});					
				}    			
    		} 
    	}
    	
    	if (result!=null){    		
    		if (outputName==null){
    			outputName = "merged_"+minInputName;
    		}
    		for (File folder: outputFolders) {
    			File file = new File(folder, outputName);
	    		try {    			
					result.save(file);
					logger.log(Level.INFO, "Saved merge result to file {0}", file.getAbsolutePath());
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Failed to write file {0}", file.getAbsolutePath());
				}
    		}
    	} else {
    		logger.info("The result is empty");
    	}
    }

	private static boolean isBackupFileName(String name){
		char c;
		int length = name.length();
		int i = 0;
		if (length!=26){
			return false;
		}
		c = name.charAt(i);
		while(i<8 && c>='0' && c<='9'){
			i++;
			c = name.charAt(i);
		}
		if (i<8 || c!='_'){
			return false;
		}
		i++;
		c = name.charAt(i);
		while(i<15 && c>='0' && c<='9'){
			i++;
			c = name.charAt(i);
		}
		if (i<15 || c!='_'){
			return false;
		}
		i++;
		c = name.charAt(i);
		while(i<19 && c>='0' && c<='9'){
			i++;
			c = name.charAt(i);
		}
		if (i<19 || c!='.'){
			return false;
		}
		
		while (i<length-1 && c==".backup".charAt(i-19)){
			i++;
			c = name.charAt(i);
		}
		return c==".backup".charAt(i-19);
	}
	
	private static File findMostRecentBackupFile(File file) {
		final String[] result = new String[1];
		file.listFiles(new FilenameFilter() {			
			public boolean accept(File dir, String name) {				
				boolean accepted = isBackupFileName(name); 
				if (accepted && (result[0] == null || name.compareToIgnoreCase(result[0])>0)){
					result[0] = name;
				}
				return accepted;
			}
		});
		if (result[0]!=null){
			return new File(file, result[0]);
		}
		return file;
	}
}
