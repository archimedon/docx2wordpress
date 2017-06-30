= Docx to WordPress

A utility that ties together Mammoth converter and WP-CLI to perform a simple DAV based Content management system.

== Build
`mvn clean compile package`

== Run

=== Setup
`java -cp target/converter.jar com.rasajournal.DocxConvSetup '<WordPressHome>' '<WatchedDirectory>'

Create categories and author directories.
WatchedDirectory will contain 'WatchedDirectory/Authors' and 'WatchedDirectory/Categories'.

=== Convert
`java -jar target/converter.jar '<WordPressHome>' '<WatchedDirectory>'

Find *.docx articles. Sorts out the author and applicable categories before posting to WordPress
