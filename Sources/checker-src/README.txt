Contents:

- Introduction
- System Requirements & Installation
- How to use the application
- Bug Reports and Feedback

1. INTRODUCTION
	Open Source License Checker 3.0 is a risk management tool for analyzing open 
	source software licenses. It is developed in Java, and is platform 
	independent.
	
	Supported Features:
	- opening a single source file, or a source directory from the file system
	- opening compression packages: zip, jar, tar, tar.gz, tgz
	- identifying open source licenses from:
		* Java, Javascript, PHP, Python and C/C++ source files 
		* Linux kernel source support
		* LICENSE.txt and COPYING.txt
	- Indicating the license matching condifence comparing to the original license
	  text.
	- highlighting the matching license text
	- displaying the license conflicts:
		* Local/reference conflicts: source file A cannot import or include source 
		  file B due to license reference restriction. (e.g GLP license source file
		  cannot import or include PHP licensed source file.)
		* Global conflicts: TODO
	- filtering source files
        - print support
	- showing found tags (author name, years, etc)
	- identificating license exceptions
	- identificating forbidden phrases
	- Summary and report on the source files in the package
	- listing compatible licenses within a source file package
	- adding new licenses and forbidden phrases in to the system
	- downloading a preset database from the web
	- idenfify copyright holders within source files
	- exporting license and copyright reports in PDF and RTF formats
	- checkout support for both CVS and SVN
	- language support for english and finnish with the ability to create new language files
	
2. SYSTEM REQUIREMENTS AND INSTALLATION
	The minimun requirement is: JRE version 1.5 or above
	
3. HOW TO USE THE APPLICATION

Quick start:
    java -jar oslc.jar   
           to run the GUI version and
    java -jar oslc.jar [arguments]
           to run the CLI version.

Run the program with the "-h" argument to see the CLI help screen.

When run without arguments, the GUI is started.

Script files for Unix and Windows:
   ./oslccli [arguments]       (Unix, CLI)
   ./oslcgui                   (Unix, GUI)
   oslccli [arguments]         (Windows, CLI)
   oslcgui                     (Windows, GUI)


For example:
   ./oslccli -r test_sources.zip	
	
5. BUGS AND FEEDBACKS
	Bugs and feedback can be reported to us in sourceforge: 
	http://forge.objectweb.org/projects/oslcv3/

	
