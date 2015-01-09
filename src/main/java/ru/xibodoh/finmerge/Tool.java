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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.xibodoh.finmerge.financisto.BackupFile;
import ru.xibodoh.finmerge.financisto.Entity;

public class Tool {
	private static final Logger logger = Logger.getLogger(App.class.getName());

	
	private static final String COPYRIGHT = "Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh). All rights reserved.";
	
	private static final String DONATE = "If you like the application, please, donate to Sberbank account 6761 9600 0391 992469";
	private static final String DONATE_TRANSLIT = "Esli Vam ponravilos' prilozhenie, pozhalujsta, podderzhite razrabotchika, nomer karty v Sberbanke: 6761 9600 0391 992469";
	private static final String DONATE_RUS = "Если Вам понравилось приложение, пожалуйста, поддержите разработчика, номер карты в Сбербанке: 6761 9600 0391 992469";
	
	private static final String USAGE = "Usage: java "+Tool.class.getName()+" [-logLevel level] command [inputFile] [-out outputFile]\n"+
			"\tlevel - logging level: SEVERE,WARNING,INFO,FINE,FINER,FINEST,ALL\n"+
			"\tcommand\n"+
			"\t\tprint - print intput file contents(default)\n"+
			"\t\tadded - print new entities\n"+
			"\t\tdeleted - print deleted entities\n"+
			"\t\tcomm123 - compare two files\n" +
			"\t\t\t1 - print unique entities of first file\n" +
			"\t\t\t2 - print common entities for both files\n" +
			"\t\t\t3 - print unique entities for second file\n" +
			"\t\tequal - compare two files for equality\n" +
			"\t\tmerge - merge two files\n" +
			"\t\tmetadata - print input file metadata\n"+
			"\t\tlog - print input file changelog\n"+
			"\t inputFile - input file or folder name\n"+
			"\t\tIf folder is given, the most recent backup file in it will be selected.\n"+
			"\t outputFile - output file name, where to write merge result.\n"+
			"\t\tIf omitted, the result will be written in every input folder.\n"+
			"\t\tIf there are no input folders, the result will be written to current folder.";
			
	private final static String[] COMMANDS = {"print", "added", "deleted", "comm", "equal", "merge", "metadata", "log"};
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
    	String logLevel = null;
    	List<File> inputFiles = new ArrayList<File>();
    	String outputName = null;
    	String command = null;
    	List<String> commands = Arrays.asList(COMMANDS);
    	for(int i=0; i<args.length; i++){
    		String arg = args[i];
    		if ("-logLevel".equals(arg) && i+1<args.length){
    			logLevel = args[++i];
//    		} else if ("-in".equals(arg) && i+1<args.length){
//    			inputFiles.add(new File(args[++i]));    			
    		} else if ("-out".equals(arg) && i+1<args.length) {
    			outputName = args[++i];
    		} else if (command==null && (commands.indexOf(arg)>=0 || arg.startsWith("comm"))){
    			command = arg;    			
    		} else {
    			inputFiles.add(new File(arg));
    		}
    	}
    	
    	Log.configure(logLevel);
    	logger.info(COPYRIGHT);
    	if (inputFiles.isEmpty()){
    		logger.info(USAGE);
    		logger.info(DONATE_TRANSLIT);
    		return;
    	}
    	if (command==null){
    		command = "print";
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
					
					if ("print".equals(command)){
						print(backupFile);
					} else if ("added".equals(command)){
						print(backupFile.added());
					} else if ("deleted".equals(command)){
						print(backupFile.deleted());
					} else if (command.startsWith("comm")) {
						if (result==null){
							result = backupFile;
						} else {
							if (command.indexOf('1')>=0){
								print(result.unique(backupFile));
							}
							if (command.indexOf('2')>=0){
								print(result.common(backupFile));
							}
							if (command.indexOf('3')>=0){
								print(backupFile.unique(result));
							}
							result = null;
						}
					} else if ("equal".equals(command)){
						if (result==null){
							result = backupFile;
						} else {
							System.out.println(result.unique(backupFile).isEmpty() && backupFile.unique(result).isEmpty());
							result = null;
						}
					} else if ("metadata".equals(command)){
						System.out.println("package name: "+backupFile.getPackageName());
						System.out.println("version name: "+backupFile.getVersionName());
						System.out.println("version code: "+backupFile.getVersionCode());
						System.out.println("database versions: "+backupFile.getDatabaseVersion());
						System.out.println("file name: "+backupFile.getMetaData().getFileName());
						System.out.println("parents: "+Arrays.toString(backupFile.getMetaData().getParents()));
					} else if ("log".equals(command)){
						printLog(backupFile, "", true);
					} else if ("merge".equals(command)){					
						if (result==null){
							result = backupFile;
						} else {
							result.merge(backupFile);
						}
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
    		if (outputFolders.isEmpty()){
    			outputFolders.add(new File("."));
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
    	}
		logger.info(DONATE_TRANSLIT);
//    	else {
//    		logger.info("The result is empty");
//    	}    	    	
	}
	
	
	private static void print(Iterable<Entity> collection){
		for (Entity entity: collection){
			System.out.println(entity);
		}
	}
	
	private static void printLog(BackupFile backupFile, String prefix, boolean isTail) {
		System.out.println(prefix + (isTail ? "└── " : "├── ") + backupFile.getMetaData().getFileName());
		String[] parents = backupFile.getMetaData().getParents();
		for (int i = 0; i < parents.length; i++) {
			try {
				// TODO find file in input folders
				BackupFile parent = new BackupFile(new File(parents[i]));
				printLog(parent, prefix + (isTail ? "    " : "│   "), i==parents.length-1);
			} catch (Exception e){
				System.out.println(prefix + (isTail ? "    " : "│   ")+(i==parents.length-1 ? "└── " : "├── ") + parents[i] + " (failed to read)");
			}
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
