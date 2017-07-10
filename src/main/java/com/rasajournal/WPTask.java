package com.rasajournal;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.rasajournal.entity.ArticleMeta;
import com.rasajournal.entity.ArticleMetaImpl;
import com.rasajournal.entity.Author;
import com.rasajournal.entity.Category;

public class WPTask {

    private final File platformBase;

    public WPTask(File platformBase) {
	this.platformBase = platformBase;
    }

    /**
     * Takes a file representing an image, imports it to Wordpress and returns the URL to the image
     * @param content
     * @return
     * @throws IOException
     */
    public String importImage(File content) throws IOException {
	final String cmdTmplt = "wp post get $(wp media import '%s' --porcelain) --field=guid";
	return runIt( String.format(cmdTmplt ,  content.getAbsolutePath()));
    }

    public String importFeaturedImage(String imagePath, Long articleID) throws IOException {
	final String cmdTmplt = "wp post get $(wp media import '%s' --post_id=%s --featured_image --porcelain) --field=guid";
	return runIt( String.format(cmdTmplt, imagePath, articleID));
    }

    public String importFeaturedImage(File imagePath, Long articleID) throws IOException {
	final String cmdTmplt = "wp post get $(wp media import '%s' --post_id=%s --featured_image --porcelain) --field=guid";
	return runIt(String.format(cmdTmplt, imagePath, articleID));
    }

    public String getUsers() throws IOException {
	return runIt("wp user list --fields=ID,display_name,user_email --format=json");
    }

    public String getCategories() throws IOException {
	return runIt("wp term list category --fields=term_id,name,parent --format=json");
    }

    private String runIt(String cmd) throws IOException {
	final String str = IOUtils.toString( new ProcessBuilder( new ImmutableList.Builder<String>().add("sh").add("-c").add(cmd).build())
	.directory(platformBase).start().getInputStream(), Charsets.UTF_8);
	return (str != null) ? str.trim() : str;
    }

    private String shellScape(String str) {
	return str.contains(" ") ? "'" + str + "'" : str;
    }

    /**
     * @param id
     * @return The articles URL
     * @throws IOException
     */
    public String getPostURL(String id) throws IOException {
	return runIt(String.format("wp post get %s --field=guid", id ));
    }

    /**
     * @param id
     * @return The articles URL
     * @throws IOException
     */
    public String getPostURL(Long id) throws IOException {
	return getPostURL(id.toString());
    }
    
    /**
     *
     * @param content The a File of the article to be posted
     * @param title  The title
     * @param pubdate
     * @param options
     * @return The ID of the article
     * @throws IOException
     */
    public String postCreate(File content, String title, Date pubdate, ArticleMeta options) throws IOException {

	final String res = runIt(
		String.format("wp post create %s --post_title='%s' %s --porcelain",
			shellScape(content.getAbsolutePath()),
			title,
			options.coreMeta().entrySet()
				.stream()
				.map(entry -> "--" + entry.getKey() + "=" + shellScape(entry.getValue()))
				.reduce("", (a,b) -> a + " " + b)
		)
	);
	postMeta(Long.parseLong(res), options.getPostMeta());
	return res;
    }

    public void postMeta(final Long aid, Map<String, String> metaMap) throws IOException {
	final String cmdTmplt = "wp post meta add %d %s %s";
	metaMap.entrySet().forEach(entry -> {
	    try {
		runIt(String.format(cmdTmplt, aid, shellScape(entry.getKey()), shellScape(entry.getValue())));
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	});
    }

    public static void main(String... args) {

	File baseDir = new File(args[0]);
	File imagePath= new File(args[1]);
	File articlePath= new File(args[2]);
	String title = "TESST TITTLES";

	WPTask task = new WPTask(baseDir);

	try {
	    System.out.println("OUTPUT: " + task.importImage(imagePath));

	    Author auth = new Author();
	    auth.setId("2");
	    auth.setDisplayName("ragga muff");
	    long termId = 1;
	    String name = "Uncategorized";
	    long parent = 0;
	    Category cat = new Category(termId, parent, name);

	    ArticleMeta opts = new ArticleMetaImpl(auth, new ImmutableList.Builder<Category>().add(cat).build());

	    String articleId = task.postCreate(articlePath, title, new Date() , opts);

	    System.out.println("OUTPUT: " + articleId);

	    task.importFeaturedImage(imagePath, Long.parseLong(articleId));

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}

