package com.rasajournal;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.zwobble.mammoth.DocumentConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasajournal.entity.ArticleMeta;
import com.rasajournal.entity.ArticleMetaImpl;
import com.rasajournal.entity.Author;
import com.rasajournal.entity.Category;

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
	String res = null;
	
	
	Path documentDir = docx.getParent();
	try {

	    // Get the files author
	    final Path authorDir = documentDir.getParent().equals( docconv.getAuthorsDir()) ? documentDir : documentDir.getParent();
	    File authorFile = authorDir.resolve(DocxConvSetup.authorDataFile).toRealPath().toFile();
	    Author author = loadJsonClass(authorFile, Author.class);

	    // Get the files categories
	    final List<Category> categories = new LinkedList<>();

	    try (Stream<Path> catFileStream = Files.find(documentDir, 3, (path, attr) ->  String.valueOf(path).endsWith(DocxConvSetup.categoryDataFile) )) {

		catFileStream.forEach( (catFilePath) -> {
		    categories.add(loadJsonClass(catFilePath.toFile(), Category.class));
		});
	    }
	   
	    final Map<String,Object> result = new HashMap<String, Object>(
		    // Augment Metadata
		    docconv.saveDoc(docx.toFile(), new ArticleMetaImpl(author, categories))
	    );
	    final String baseName = FilenameUtils.getBaseName(docx.getFileName().toString());
	    res = this.objectMapper.writeValueAsString(result);

	    Files.write(documentDir.resolve(DocxConvSetup.completedDataFilen), res.getBytes());
	    Files.write(documentDir.resolve(baseName + ".html"), getConfirmation(result));
	    Files.move(docx, documentDir.resolve(baseName + ".docx.posted"));
	    
	    Files.delete(documentDir.resolve(baseName + ".xhtml"));
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return res;
    }

    private byte[] getConfirmation(Map<String, Object> result) {
	return ("<html><body><a href='" + result.get("ArticleURL") + "'>"  + result.get("ArticleTitle") + "</a> <br/>"  + result.get("ArticleURL") + "</body></html>").getBytes();
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
