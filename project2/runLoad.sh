#!/bin/bash

# Run the drop.sql batch file to drop existing tables
# Inside the drop.sql, you sould check whether the table exists. Drop them ONLY if they exists.
mysql CS144 < drop.sql

# Run the create.sql batch file to create the database and tables
mysql CS144 < create.sql

# Compile and run the parser to generate the appropriate load files
ant
ant run-all

# If the Java code does not handle duplicate removal, do this now
sort -u users.dat -o users.dat
sort -u sellers.dat -o sellers.dat
sort -u bidders.dat -o bidders.dat
sort -u items.dat -o items.dat
sort -u bids.dat -o bids.dat
sort -u categories.dat -o categories.dat
sort -u itemCategory.dat -o itemCategory.dat

# Run the load.sql batch file to load the data
mysql CS144 < load.sql

# Remove .dat files to prevent appending
rm *.dat
ant clean

