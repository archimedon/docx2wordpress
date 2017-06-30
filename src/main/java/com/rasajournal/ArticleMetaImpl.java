package com.rasajournal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArticleMetaImpl implements ArticleMeta {

    private PostType postType = ArticleMeta.PostType.POST;
    private PostStatus postStatus = ArticleMeta.PostStatus.PUBLISH;
    
    private final Author author;  
    private final List<Category> categories;  
    private final Map<String, String> postMeta;
    /*
     * post_author' => $user_id,
        'post_content' => '',
        'post_content_filtered' => '',
        'post_title' => '',
        'post_excerpt' => '',
        'post_status' => 'draft',
        'post_type' => 'post',
        'comment_status' => '',
        'ping_status' => '',
        'post_password' => '',
        'to_ping' =>  '',
        'pinged' => '',
        'post_parent' => 0,
        'menu_order' => 0,
        'guid' => '',
        'import_id' => 0,
        'context' => '',
     */

    public ArticleMetaImpl(Author author,  List<Category> categories) {
	this.author = author;
	this.categories = categories;
	this.postMeta =  new HashMap<String, String>();

//	m.put("_edit_last", "2");
	postMeta.put("cb_bg_image_post_setting", "1");
	postMeta.put("cb_review_checkbox", "0");
	postMeta.put("cb_cs1", "0");
	postMeta.put("cb_cs2", "0");
	postMeta.put("cb_cs3", "0");
	postMeta.put("cb_cs4", "0");
	postMeta.put("cb_cs5", "0");
	postMeta.put("cb_cs6", "0");
	postMeta.put("cb_video_post_select", "1");
	postMeta.put("cb_featured_image_style_override", "off");
	postMeta.put("cb_featured_image_style", "full-width");
	postMeta.put("cb_featured_image_title_style", "cb-fis-tl-default");
	postMeta.put("cb_featured_image_st_title_style", "cb-fis-tl-st-default");
	postMeta.put("cb_post_fis_header", "on");
	postMeta.put("cb_featured_post_menu", "off");
	postMeta.put("cb_featured_post", "off");
	postMeta.put("cb_featured_cat_post", "off");
	postMeta.put("_cb_dropcap", "off");
	postMeta.put("_cb_first_dropcap", "cb_dropcap_s");
	postMeta.put("cb_full_width_post", "nosidebar-narrow");
	postMeta.put("cb_post_sidebar", "on");
	postMeta.put("cb_post_custom_sidebar_type", "cb_unique_sidebar");
    }

    public Map<String, String> coreMeta() {
	Map<String, String> m = 	new HashMap<String, String>();

	m.put("post_category", getPostCategoriesString().orElse("1"));
	m.put("post_type", this.getPostType().type());
	m.put("post_status", this.getPostStatus().status());
	m.put("user", author.getId());
	return m;
    }



    /**
     * @return the postType
     */
    @Override public PostType getPostType() {
        return postType;
    }


    /**
     * @param postType the postType to set
     */
    public ArticleMeta setPostType(PostType postType) {
        this.postType = postType;
        return this;
    }


    /**
     * @return the postStatus
     */
    @Override public PostStatus getPostStatus() {
        return postStatus;
    }


    /**
     * @param postStatus the postStatus to set
     */
    public ArticleMeta setPostStatus(PostStatus postStatus) {
        this.postStatus = postStatus;
        return this;
    }


    /**
     * @return the author
     */
    public Author getAuthor() {
        return author;
    }


    @Override
    public Optional<List<Long>> getPostCategories() {
	return Optional.ofNullable(categories.isEmpty()
		? null
		: categories.stream().map(cat -> cat.getTermId()).collect(Collectors.toList())
	);
    }

    public Optional<String> getPostCategoriesString() {
	String str = categories.isEmpty() ? null : categories.stream().map(cat -> cat.getTermId().toString()).collect(Collectors.joining(","));
	System.err.println("getPostCategoriesString: " + str);
	return Optional.ofNullable(
		str
	);
    }
    
    public Map<String, String> getPostMeta() {
	return postMeta;
    }

    @Override
    public ArticleMeta addMeta(String k, String v) {
	postMeta.put(k,v);
	return this;
    }
    
}
