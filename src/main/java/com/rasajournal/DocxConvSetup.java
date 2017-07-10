package com.rasajournal;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasajournal.entity.Author;
import com.rasajournal.entity.Category;

/**
 * Create or update the operational space: Author folders and available Categories.
 */
public class DocxConvSetup {

    static final String authorDataFile = "author.json";
    static final String categoryDataFile = "category.json";
    public static final String completedDataFilen = ".completed.json";

    public final Path categoriesDir;
    public final Path authorsDir;
    public final Path hotDir;
    public final ObjectMapper objectMapper;
    public final ConverterConfig config;
    public final WPTask taskMan;

    private final Set<PosixFilePermission> perms = PosixFilePermissions.fromString("r-xr-xr--");


    public DocxConvSetup(String platformDirPath, String watchDirPath) throws IOException {
	this.objectMapper = new ObjectMapper();
	this.config = new ConverterConfig(platformDirPath, watchDirPath);
	this.taskMan = new WPTask(new File(platformDirPath));
	this.hotDir =  Paths.get(watchDirPath).toRealPath();
	this.authorsDir = Files.createDirectories(Paths.get(watchDirPath, "Authors"));
	this.categoriesDir = Files.createDirectories(Paths.get(watchDirPath, "Categories"));
    }

    public static void main(String[] args) throws IOException {

//        OptionParser parser = new OptionParser( "fc:q::" );
//
//        OptionSet options = parser.parse( "-f", "-c", "foo", "-q" );
//
//        assertTrue( options.has( "f" ) );
//
//        assertTrue( options.has( "c" ) );
//        assertTrue( options.hasArgument( "c" ) );
//        assertEquals( "foo", options.valueOf( "c" ) );


	final DocxConvSetup dcs = new DocxConvSetup( args[0], args[1]  ) ;
	Files.setPosixFilePermissions(dcs.categoriesDir, PosixFilePermissions.fromString("rwxr-xr--"));
	dcs.initAuthors();
	dcs.initCategories();
	Files.setPosixFilePermissions(dcs.categoriesDir, PosixFilePermissions.fromString("r-xr-xr--"));
//	dcs.mkReadOnlyDir(dcs.categoriesDir);
    }

    public List<Author> getAuthors() throws IOException {
	final String authorJson = taskMan.getUsers();
	return objectMapper.readValue(authorJson, new TypeReference<List<Author>>(){});
    }

    public Map<Long, Category> getCategories() {
	String categoriesJson;
	try {
	    categoriesJson = taskMan.getCategories();
	    List<Category> cats = objectMapper.readValue(categoriesJson, new TypeReference<List<Category>>() {});
	    final Map<Long, Category> m = new HashMap<Long, Category>();
	    cats.forEach(cat -> {
		m.put(cat.getTermId(), cat);
	    });
	    return m;
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public void mkReadOnlyDir(Path aDir) throws IOException {
	try (DirectoryStream<Path> stream = Files.newDirectoryStream(aDir, entry -> Files.isDirectory(entry))) {
	    for (Path dir : stream)  {
		try (DirectoryStream<Path> innerStream = Files.newDirectoryStream(dir, entry -> Files.isDirectory(entry))) {
		    for (Path innerDir : innerStream) {
			mkReadOnlyDir(innerDir);
			Files.setPosixFilePermissions(innerDir, perms);
		    }
		}
		Files.setPosixFilePermissions(dir, perms);
	    }
	}
    }

    private void initCategories() {

	final Map<Long, Category>  catmap = getCategories();
	final List<Category> sortedCats = new LinkedList<Category>(catmap.values());

	sortedCats.sort((x, y) -> {
		int ret = x.parent.compareTo(y.parent);
		return ret == 0 ? x.termId.compareTo(y.termId) : ret;
	});

	sortedCats.forEach( category -> {

	    String filename = category.getParent().longValue() > 0
		    	? "[ " + catmap.get(category.getParent()) + " : "  +  category.getName() + " ] " +  categoryDataFile
		    	: "[ "  +  category.getName() + " ] " +  categoryDataFile;

	    Path tmp = categoriesDir.resolve(filename);

	    try {
		objectMapper.writeValue(tmp.toFile() , category);
		Files.setPosixFilePermissions(tmp, PosixFilePermissions.fromString("rwxrwxrwx"));
	    } catch (Exception e) {
		e.printStackTrace();
	    };
	});
    }

    private String makeCategoryDataFileName(String name) {
	return "[" + name + "]-" +  categoryDataFile;
    }

    private void initAuthors() {
    	try {
	    getAuthors().forEach( author -> {
	        try {
                	    	this.objectMapper.writeValue(
                	    		Files.createDirectories(this.authorsDir.resolve(author.getDisplayName())).resolve(authorDataFile).toFile(), author
                	    	);
	        } catch (Exception e) { e.printStackTrace(); }
	    });
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public Path getCategoriesDir() {
	return categoriesDir;
    }

    public Path getAuthorsDir() {
	return authorsDir;
    }
}

