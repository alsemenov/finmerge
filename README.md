Finmerge tool
=============
The tool for merging records from several Financisto backup files into single backup file,
that can be restored back in the Financisto. Typycal scenario two or more persons (a family members, for example) would like to share their personal finances. Here are the steps for using the program:
  1. Each person enters data using his/her own mobile device and Financisto
  2. Each person backup the data and transfer the backup file to a single computer
  3. Use finmerge tool to merge records in the backup files into new single backup file
  4. Each person restores the new backup file on his/her own mobile device
  5. Go to step 1.

Usage
=====
```
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
```  

How it works
============
  Basically merging files produces the file, which is union of all unque records of all source files.  
  
  For each data entity unique finger print is calculated. The finger print is based on concatenation of entity fields values, that make the entity unique. For example, for project finger print is its title, for category fingerprint is its path in the category tree, for account - title+currency. Merging two (or more) files is actually just adding entities from one file to another. Before addition each source entity is checked by its finger print, whether it is already present in the destination. If entity already exist, it is not added.  
  
  Merging is good, but it is **not a synchronization**. If some record is deleted in one file it still exists in other files and will be included into output. If some record is edited, its old version still exists in other files so the output will contain both versions new and old. To solve this problem Finmerge compares each source file with previous merge result (if possible), finds deleted records and makes sure, that deleted records are not included into output. The situation when the same entity is edited in several sources is still not resolved, the output will contain all new versions of the entity. I recommend to agree that each entity can be editied only by single person (for example, by person who entered the entity).
  
  It is recommended to keep backups of each person in a separate folder. Then Finmerge will be able to find previous merge result and handle deleted records correctly.
  

Current limitations
===================
  * Java v1.7 is required. The project can be recompiled with lower version, but some tests may fail.
  * Splitted transactions are not tested/supported (because I do not use them :-) )
  * Budget, locations should work well, but not tested
  * Conflict of editing the same entity by different persons is not resolved technically. It should be resolved by agreement between persons.
  * I had to exclude test data from publishing, because it is my real financial data. All tests are commented.

Support
=======
Please, report bugs, issues, feature requests into [Issues](https://github.com/alsemenov/finmerge/issues)

References
==========
[Financisto](https://play.google.com/store/apps/details?id=ru.orangesoftware.financisto) - personal finance manager

