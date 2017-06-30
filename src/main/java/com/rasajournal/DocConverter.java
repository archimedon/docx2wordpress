package com.rasajournal;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.Result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

public class DocConverter {
    
    private final WPTask taskMan;
    private final ConverterConfig config;
    private final DocumentBuilder builder;

    final Path imageDirPath;
    final String cssClassName = "docConvGen";
    final String convTitleTag = "cmsTitle";
    final String xhtmlWrapStart = "<span>";
    final String xhtmlWrapEnd = "</span>";
    public final Path categoriesDir;
    public final Path authorsDir;
    
    public String title;
    public String featuredImage;
    
    public DocConverter (ConverterConfig config, WPTask taskMan) throws ParserConfigurationException, IOException {
	this.config= config; 
	this.taskMan = taskMan;
	this.builder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
	this.imageDirPath = config.getScratchPath().resolve("images");
	Files.createDirectories(imageDirPath);
	this.authorsDir = config.watchPath.resolve("Authors");
	this.categoriesDir = config.watchPath.resolve("Categories");
    }

    public String saveDoc(File docx, ArticleMeta options) throws IOException, ParserConfigurationException, SAXException, XPathException, TransformerFactoryConfigurationError, TransformerException {

	final List<String> response = new LinkedList<String>();
	final String docxName = docx.getName();
	final String baseName = FilenameUtils.getBaseName(docxName);

	final Path docImagePath = this.imageDirPath.resolve(baseName);

	final File docParentPath = docx.getParentFile();
	final Map<String, String> docImages = new HashMap<String, String>();
	final DocumentConverter mammothDc = new DocumentConverter()
		.addStyleMap("r[style-name='Hyperlink'] =>")
		.addStyleMap("r[style-name='Internet Link'] =>")
		.addStyleMap("r[style-name='Q'] => q")
		.addStyleMap("p[style-name='Quotations'] => blockquote:fresh")
		.addStyleMap("p[style-name='Caption'] => center > i.caption:fresh")
		.addStyleMap("r[style-name='Emphasis'] => strong")
		.addStyleMap("p[style-name='Normal'] => p:fresh")
		.addStyleMap("p[style-name='Illustration'] =>") // not handled.
		.addStyleMap("p[style-name='Title'] => cmsTitle:fresh")
		.imageConverter(img -> {
		    final int size = docImages.size();

		    File targetFile = docImagePath .resolve("img-"+ size + "." + img.getContentType().split("/")[1]).toFile();
		    FileUtils.copyInputStreamToFile( img.getInputStream(), targetFile );

		    final String urlStr = taskMan.importImage(targetFile);
		    docImages.put(targetFile.getAbsolutePath(), urlStr);
		    return new ImmutableMap.Builder<String, String>()
			.put("src", urlStr)
			.put("class", cssClassName).build();
		});
	Result<String> result = mammothDc.convertToHtml(docx);
	final File htmlFile = new File(docParentPath, baseName + ".xhtml");

	try {
	    response.add("warnings: \t" + result.getWarnings().toString());

	    final String xhtml = xhtmlWrapStart + result.getValue() + xhtmlWrapEnd;

	    try (InputStream streamIn = new ByteArrayInputStream(xhtml.getBytes("utf-8"))) {

		final Document document = builder.parse(new InputSource(streamIn));
		document.getDocumentElement().normalize();

		final Element titleElem = (Element) document.getElementsByTagName(convTitleTag).item(0);
		this.title = titleElem.getTextContent();
		titleElem.getParentNode().removeChild(titleElem);

		try (ByteArrayOutputStream htmIntermediatelOut = new ByteArrayOutputStream()) {

		    final OutputStream htmlOut = new FileOutputStream(htmlFile);
		    final Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    transformer.setOutputProperty(  javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
		    transformer.transform(new DOMSource(document), new StreamResult(htmIntermediatelOut));

		    final int startWrapTagLen = xhtmlWrapStart.length();
		    final int closeWrapTagLen = xhtmlWrapEnd.length();

		    htmlOut.write(htmIntermediatelOut.toByteArray(), startWrapTagLen, htmIntermediatelOut.size()  - (closeWrapTagLen + startWrapTagLen));
		    htmlOut.close();
		}
	    }

	    final String pid = taskMan.postCreate(htmlFile, title, null, options);
	    response.add("  posted with ID: " + pid);

	    final Optional<File> featuredImg = checkFeaturedImage(docx);

	    if (featuredImg.isPresent()) {
		response.add("Featured Image: " + taskMan.importFeaturedImage(featuredImg.get(), Long.parseLong(pid)));
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	return response.stream().collect(Collectors.joining("\n"));

    }

    private Optional<File> checkFeaturedImage(File docx) throws IOException {
	final String imageTypes="tif,tiff,gif,jpeg,jpg,jif,jfif,jp2,jpx,j2k,j2c,fpx,pcd,png";
	
	File[] extraImages = docx.getParentFile().listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File arg0) {
		return imageTypes.contains(FilenameUtils.getExtension(arg0.getName()));
	    }
	});
	return Optional.ofNullable((extraImages.length > 0 ) ? extraImages[0] : null);
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the featuredImage
     */
    public String getFeaturedImage() {
        return featuredImage;
    }

    /**
     * @param featuredImage the featuredImage to set
     */
    public void setFeaturedImage(String featuredImage) {
        this.featuredImage = featuredImage;
    }

    /**
     * @return the config
     */
    public ConverterConfig getConfig() {
        return config;
    }


    /**
     * @return the mammothDc
     */
//    public DocumentConverter getMammothDc() {
//        return mammothDc;
//    }


    /**
     * @return the imageDirPath
     */
    public Path getImageDirPath() {
        return imageDirPath;
    }


    /**
     * @return the cssClassName
     */
    public String getCssClassName() {
        return cssClassName;
    }


    /**
     * @return the categoriesDir
     */
    public Path getCategoriesDir() {
        return categoriesDir;
    }


    /**
     * @return the authorsDir
     */
    public Path getAuthorsDir() {
        return authorsDir;
    }


    /**
     * @return the taskMan
     */
    public WPTask getTaskMan() {
        return taskMan;
    }
}

