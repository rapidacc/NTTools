package com.ernieyu.feedparser.impl;

import com.ernieyu.feedparser.*;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Feed implementation for RSS 1.0.
 */
class Rss1Feed extends BaseElement implements Feed {
    // XML elements for RSS feeds.
    private static final String CHANNEL = "channel";
    private static final String TITLE = "title";
    private static final String LINK = "link";
    private static final String DESCRIPTION = "description";
    private static final String LANGUAGE = "language";
    private static final String RIGHTS = "rights";
    private static final String DATE = "pubDate";
    private static final String ITEM = "item";

    /**
     * Constructs an Rss1Feed with the specified namespace uri, name and
     * attributes.
     */
    public Rss1Feed(String uri, String name, Attributes attributes) {
        super(uri, name, attributes);
    }

    @Override
    public FeedType getType() {
        return FeedType.RSS_1_0;
    }

    @Override
    public String getTitle() {
        Element channel = getElement(CHANNEL);
        Element title = channel.getElement(TITLE);
        return (title != null) ? title.getContent() : null;
    }

    @Override
    public String getLink() {
        Element channel = getElement(CHANNEL);
        Element link = channel.getElement(LINK);
        return (link != null) ? link.getContent() : null;
    }

    @Override
    public String getDescription() {
        Element channel = getElement(CHANNEL);
        Element descr = channel.getElement(DESCRIPTION);
        return (descr != null) ? descr.getContent() : null;
    }

    @Override
    public String getLanguage() {
        // Use Dublin Core element.
        Element channel = getElement(CHANNEL);
        Element language = channel.getElement(LANGUAGE);
        return (language != null) ? language.getContent() : null;
    }

    @Override
    public String getCopyright() {
        // Use Dublin Core element.
        Element channel = getElement(CHANNEL);
        Element rights = channel.getElement(RIGHTS);
        return (rights != null) ? rights.getContent() : null;
    }

    @Override
    public Date getPubDate() {
        // Use Dublin Core element.
        Element channel = getElement(CHANNEL);
        Element pubDate = channel.getElement(DATE);
        return (pubDate != null) ? FeedUtils.convertRss1Date(pubDate.getContent()) : null;
    }

    @Override
    public List<String> getCategories() {
        return Collections.<String>emptyList();
    }

    @Override
    public List<Item> getItemList() {
        // Get element list for items.
        List<Element> elementList = getElementList(ITEM);
        List<Item> itemList = new ArrayList<Item>();

        // Build item list.
        if (elementList != null) {
            for (Element element : elementList) {
                itemList.add((Item) element);
            }
        }

        return itemList;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
