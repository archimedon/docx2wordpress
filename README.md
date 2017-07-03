# Docx to WordPress

## A simple **Drag & Drop** interface for publishing to WordPress.

A serverside application, customized for our particular CMS needs, specifically, an interface that utilized familiar technologies was required. Thus, this application integrates with existing directory services. Authors need only mount/connect to _their_ respective directories, create the article in Word/LibreOffice etc... and *drop* a folder containing: their docx article, a featured image (if any) and category *_tags_* to their folder.

## Overview

A small application, built for Linux. providing just enough flexibility and functionality to get the job done. Currently running as a cronjob. With a little elbow, it can be converted into a daemon or DirectoryWatchService.

(Depends on [WP-CLI](https://github.com/wp-cli/wp-cli) ...  for talking to Wordpress platform.)

## Publisher/Author Usage

During the setup of Docx2Wordpress, author directories are created (or updated) based on existing WP-authors. A set of files representing the available *categories* are also created. 

![Generated and Watched directories](https://www.screencast.com/t/lxrEbQYMgX5S)

Directory services, in this use-case, are by ACL over webDav. My user has access to all test accounts however, in practice authors only have access their own folders.

## Publishing an article

### Create docx
 - The article may contain inlined images
 - The article may utilize: endnotes, list-styles and the set of common document structures/styles as supported by mammoth
 - In Word/LibreOffice, create a style named *Title*. Use this style to mark the article/post title.

1. Create a folder for the article, `article-dir/`
2. Save the article as a docx file to `article-dir/`
3. Copy article-dir to the author's folder
4. Copy the files representing the desired categories

![Categorize and add featured image](https://www.screencast.com/t/vtMxpnDc7)

# How to build

Connect to Linux shell

## Build

`mvn clean compile package`

## Run

### Setup
`java -cp target/converter.jar com.rasajournal.DocxConvSetup '<WordPressHome>' '<WatchedDirectory>'

This Creates the category files and and author directories and sets file permissions in an attempt to prevent accidental deletion - Note. this depends on the _current_ user's access privilege.

It may be helpful to run setup in a weekly cronjob, depending on the rate at which you add categories or authors.

### Convert

`java -jar target/converter.jar '<WordPressHome>' '<WatchedDirectory>'

- Find *.docx articles
- Sort out the author and applicable categories
- Post to WordPress

Runs in a cron. Articles have to be in before publish times.


