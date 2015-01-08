Finmerge tool
=============
The tool for merging records from several Financisto backup files into single backup file,
that can be restored back in the Financisto. Typycal scenario two or more persons (a family members, for example) would like to share their personal finances. Here are the steps for using the program:
  1. Each person enters data using his/her own mobile device
  2. Each person backup the data and transfer the backup file to a single computer
  3. Use finmerge tool to merge records in the backup files into new single backup file
  4. Each person restores the new backup file on his/her own mobile device
  5. Go to step 1.

Usage
=====
java ru.xibodoh.finmerge.Tool [-logLevel level] command [inputFile] [-out outputFile]  
    level - logging level: SEVERE,WARNING,INFO,FINE,FINER,FINEST,ALL  
    command  
        print - print intput file contents(default)  
        added - print new entities  
        deleted - print deleted entities  
        comm123 - compare two files  
            1 - print unique entities of first file  
            2 - print common entities for both files  
            3 - print unique entities for second file  
        equal - compare two files for equality  
        merge - merge two files  
        metadata - print input file metadata  
        log - print input file changelog  
        inputFile - input file or folder name  
            If folder is given, the most recent backup file in it will be selected.  
        outputFile - output file name, where to write merge result.  
            If omitted, the result will be written in every input folder.  
            If there are no input folders, the result will be written to current folder.
      
References
==========
[Financisto](https://play.google.com/store/apps/details?id=ru.orangesoftware.financisto "Financisto") - personal finance manager

