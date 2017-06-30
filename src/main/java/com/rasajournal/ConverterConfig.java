package com.rasajournal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class ConverterConfig {

    public static final String authorDataFile = "author.json";
    public static final String categoryFileSuffix = "category.json";

    public static String scratchDirPath = "/tmp/docx/rasa";
    public final Path watchPath;
    public final Path platformPath;
    public final Path scratchPath;
    public final String timeStampString;
    
    public ConverterConfig(String platformDirPath, String watchDirPath) throws IOException {
	
	this.timeStampString = new Date().getTime() + "";
	
	for (Path requiredDir : new Path[] {
		this.watchPath = Paths.get(watchDirPath),
		this.platformPath = Paths.get(platformDirPath),
		this.scratchPath = Paths.get(scratchDirPath, timeStampString) }
	) {
	    Files.createDirectories(requiredDir);
	}
	System.err.println("New ConverterConfig scratchPath: " + scratchPath );
	
	
    }
    
    public static String getScratchDirPath() {
        return scratchDirPath;
    }

    public static void setScratchDirPath(String scratchDirPath) {
        ConverterConfig.scratchDirPath = scratchDirPath;
    }

    public Path getWatchPath() {
        return watchPath;
    }

    public Path getPlatformPath() {
        return platformPath;
    }

    public Path getScratchPath() {
        return scratchPath;
    }


}
