package com.rasajournal.entity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ArticleMeta {

    public static enum PostType
    {
	POST("post"),
	PAGE("page"),
	ATTACHMENT("attachment"),
	REVISION("revision"),
	NAVMENUITEM("nav_menu_item"),
	CUSTOMCSS("custom_css");


	private final String cmdVal;

	PostType(String cliarg) {
	    this.cmdVal = cliarg;
	}

	public String type() { return cmdVal; }
    }

    public static enum PostStatus
    {
	PUBLISH("publish"),		// A published post or page
	PENDING("pending"),		// Post is pending review
	DRAFT("draft"),			// A post in draft status
	AUTODRAFT("auto-draft"),	// A newly created post, with no content
	FUTURE("future"),		// A post to publish in the future
	PRIVATE("private"),		// Not visible to users who are not logged in
	INHERIT("inherit"),		// A revision. see get_children.
	TRASH("trash");			// Post is in trash


	private final String cmdVal;

	PostStatus(String cliarg) {
	    this.cmdVal = cliarg;
	}

	public String status() { return cmdVal; }
    }

    public PostType getPostType();
    public PostStatus getPostStatus();

    public ArticleMeta addMeta(String k, String v);

    public Map<String, String> getPostMeta();

    public Optional<List<Long>> getPostCategories();
    public Optional<String> getPostCategoriesString();
    public Map<String, String> coreMeta();
}



