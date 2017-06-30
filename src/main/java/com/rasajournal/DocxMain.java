package com.rasajournal;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.zwobble.mammoth.DocumentConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DocxMain {

    final ConverterConfig config;
    
    final WPTask taskMan;
    final DocConverter docconv;
    final ObjectMapper objectMapper;
    
    final BiPredicate<Path, BasicFileAttributes> docMatcher = (path, attr) -> String.valueOf(path).contains(".docx") ;
    
    final BiPredicate<Path, BasicFileAttributes> authorMatcher = (path, attr) -> String.valueOf(path).equals(DocxConvSetup.authorDataFile);
    final BiPredicate<Path, BasicFileAttributes> categoryMatcher = (path, attr) -> String.valueOf(path).endsWith(DocxConvSetup.categoryDataFile);


    public DocxMain(ConverterConfig config, WPTask taskMan) throws Exception {
	this.config = config;
	this.taskMan = taskMan;
	this.docconv = new DocConverter(config, taskMan);
	this.objectMapper = new ObjectMapper();
    }

    public void run() throws Throwable {
	
	try (Stream<Path> docxDirStream = Files.find(docconv.getAuthorsDir(), 4, (path, attr) -> String.valueOf(path).endsWith(".docx"))) {

	    List<String> result = docxDirStream.map(docxArticlePath -> convertToSend(docxArticlePath)).collect(Collectors.toList());

	} catch (Exception ex) {
	    throw ex.getCause();
	}
    }

    private  String convertToSend(Path docx) {
	Path documentDir = docx.getParent();
	try {
	    // Get the files author
	    final Path authorDir = documentDir.getParent().equals( docconv.getAuthorsDir()) ? documentDir : documentDir.getParent();
	    File authorFile = authorDir.resolve(DocxConvSetup.authorDataFile).toRealPath().toFile();
	    Author author = loadJsonClass(authorFile, Author.class);

	    // Get the files categories
	    final List<Category> categories = new LinkedList<>();
//	    final List<Path> catDirs = new LinkedList<Path>();

	    try (Stream<Path> catFileStream = Files.find(documentDir, 3, (path, attr) ->  String.valueOf(path).endsWith(DocxConvSetup.categoryDataFile) )) {

		catFileStream.forEach( (catFilePath) -> {
		    System.out.println("catFilePath: "  +String.valueOf(catFilePath));
		    categories.add(loadJsonClass(catFilePath.toFile(), Category.class));
//		    catFilePath.toFile().deleteOnExit();
//		    if (! ( catDirs.contains(catFilePath.getParent())) ) {
//			catDirs.add(catFilePath.getParent());
//		    }
		});
	    }

//	    catDirs.forEach(catDir -> {
//		catDir.toFile().deleteOnExit();
////		removeLoadedCategory(catDir);
//	    });
	    // Augment Metadata
	    ArticleMeta articleMeta = new ArticleMetaImpl(author, categories);

	    return docconv.saveDoc(docx.toFile(), articleMeta);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    private void removeLoadedCategory(Path rootPath) {
	if (Files.exists(rootPath)) {
	    try {
		Files.walk(rootPath)
		    .sorted(Comparator.reverseOrder())
		    .map(Path::toFile)
		    .forEach(File::delete);
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    private <T> T loadJsonClass(File file,  Class<T> clazz) {
	T obj = null;
	try {
	    obj = objectMapper.readValue(file, clazz);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return obj;
    }
    
    public static void main(String... args) throws Throwable {
	String platformDirPath = args[0];
	String watchDirPath = args[1];
	
	System.err.println("Watching directory: " + watchDirPath );
	System.err.println("Posting to: " + platformDirPath );
	
	final ConverterConfig config = new ConverterConfig(platformDirPath, watchDirPath);
	final WPTask taskMan= new WPTask(config.getPlatformPath().toFile());

	try {
	    DocxMain convApp = new DocxMain(config, taskMan);
	    convApp.run();
	    
	    
	} catch (Exception ex) {
	    // I/O error encounted during the iteration, the cause is an
	    // IOException
	    throw ex.getCause();
	}
    }
    
}
