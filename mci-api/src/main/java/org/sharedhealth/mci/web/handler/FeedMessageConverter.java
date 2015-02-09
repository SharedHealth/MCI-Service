package org.sharedhealth.mci.web.handler;

import com.sun.syndication.feed.atom.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sharedhealth.mci.web.mapper.Feed;
import org.sharedhealth.mci.web.mapper.FeedEntry;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.sharedhealth.mci.utils.DateUtil.ISO_DATE_TIME_TILL_MILLIS_FORMAT1;

public class FeedMessageConverter extends AbstractHttpMessageConverter<Feed> {
    private static final String ATOM_MEDIA_TYPE = "application/atom+xml";
    private static final String LINK_TYPE_SELF = "self";
    private static final String LINK_TYPE_VIA = "via";
    private static final String ATOMFEED_MEDIA_TYPE = "application/vnd.atomfeed+xml";
    public static final String APPLICATION_XML = "application/xml";

    public FeedMessageConverter() {
        super(MediaType.APPLICATION_ATOM_XML);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Feed.class.equals(clazz);
    }

    @Override
    protected Feed readInternal(Class<? extends Feed> clazz, HttpInputMessage inputMessage) throws IOException,
            HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(Feed feed, HttpOutputMessage outputMessage) throws IOException,
            HttpMessageNotWritableException {
        String wireFeedEncoding = "UTF-8";
        com.sun.syndication.feed.atom.Feed atomFeed = mapToWireFeed(feed);
        MediaType contentType = outputMessage.getHeaders().getContentType();
        if (contentType != null) {
            Charset wireFeedCharset = Charset.forName(wireFeedEncoding);
            contentType = new MediaType(contentType.getType(), contentType.getSubtype(), wireFeedCharset);
            outputMessage.getHeaders().setContentType(contentType);
        }

        WireFeedOutput feedOutput = new WireFeedOutput();

        try {
            Writer writer = new OutputStreamWriter(outputMessage.getBody(), wireFeedEncoding);
            feedOutput.output(atomFeed, writer);
        } catch (FeedException ex) {
            throw new HttpMessageNotWritableException("Could not write WiredFeed: " + ex.getMessage(), ex);
        }
    }

    private com.sun.syndication.feed.atom.Feed mapToWireFeed(Feed feed) {
        com.sun.syndication.feed.atom.Feed atomFeed = new com.sun.syndication.feed.atom.Feed();
        atomFeed.setFeedType("atom_1.0");
        atomFeed.setId(UUID.randomUUID().toString());
        atomFeed.setGenerator(getGenerator());
        atomFeed.setAuthors(mapAuthors(feed));
        atomFeed.setAlternateLinks(mapLinks(feed));
        atomFeed.setTitle(feed.getTitle());
        atomFeed.setEntries(mapEntries(feed));
        atomFeed.setUpdated(getEarliestPostedEntry(feed));
        return atomFeed;
    }

    private Date getEarliestPostedEntry(Feed feed) {
        try {
            if (feed.getEntries().isEmpty())
                return null;
            return new SimpleDateFormat(ISO_DATE_TIME_TILL_MILLIS_FORMAT1).parse(feed.getEntries().get(0)
                    .getPublishedDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Generator getGenerator() {
        Generator generator = new Generator();
        generator.setUrl("https://github.com/ICT4H/atomfeed");
        generator.setValue("Atomfeed");
        return generator;
    }

    private List<Link> mapLinks(Feed feed) {
        List<Link> links = generatePagingLinks(feed.getPrevUrl(), feed.getNextUrl());
        links.add(getSelfLink(feed.getFeedUrl()));
        links.add(getViaLink(feed.getFeedUrl()));
        return links;
    }

    private List<Link> generatePagingLinks(String prevLink, String nextLink) {
        ArrayList<Link> links = new ArrayList<Link>();
        if (!StringUtils.isBlank(nextLink)) {
            Link next = new Link();
            next.setRel("next-archive");
            next.setType(ATOM_MEDIA_TYPE);
            next.setHref(nextLink);
            links.add(next);
        }

        if (!StringUtils.isBlank(prevLink)) {
            Link prev = new Link();
            prev.setRel("prev-archive");
            prev.setType(ATOM_MEDIA_TYPE);
            prev.setHref(prevLink);
            links.add(prev);
        }
        return links;
    }

    private List<Person> mapAuthors(Feed feed) {
        String author = feed.getAuthor();
        Person person = new Person();
        person.setName(author);
        return Arrays.asList(person);
    }

    private List<Entry> mapEntries(Feed feed) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (FeedEntry feedEntry : feed.getEntries()) {
            Entry entry = new Entry();
            entry.setId(feedEntry.getId().toString());
            entry.setTitle(feedEntry.getTitle());
            entry.setContents(generateContents(feedEntry));

            Date publishedDate = null;
            try {
                publishedDate = new SimpleDateFormat(ISO_DATE_TIME_TILL_MILLIS_FORMAT1).parse(feedEntry
                        .getPublishedDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            entry.setPublished(publishedDate);
            entry.setAlternateLinks(getAlternateLinks(feedEntry));
            entry.setCategories(getCategories(feedEntry));

            entries.add(entry);
        }
        return entries;
    }

    private List<Category> getCategories(FeedEntry feedEntry) {
        ArrayList<Category> categories = new ArrayList<>();
        for (String categoryTerm : feedEntry.getCategories()) {
            Category category = new Category();
            category.setTerm(categoryTerm);
            categories.add(category);
        }
        return categories;
    }

    private List<Link> getAlternateLinks(FeedEntry feedEntry) {
        Link encLink = new Link();
        encLink.setRel(LINK_TYPE_VIA);
        encLink.setType(APPLICATION_XML);
        encLink.setHref(feedEntry.getLink());
        return Arrays.asList(encLink);
    }

    private List<Content> generateContents(FeedEntry feedEntry) {
        Content content = new Content();
        content.setType(ATOMFEED_MEDIA_TYPE);
        String contents;
        try {
            contents = new ObjectMapper().writeValueAsString(feedEntry.getContent());
        } catch (IOException e) {
            e.printStackTrace();
            contents = null;
        }
        content.setValue(wrapInCDATA(contents));
        return Arrays.asList(content);
    }

    private String wrapInCDATA(String contents) {
        if (contents == null) {
            return null;
        }
        return String.format("%s%s%s", "<![CDATA[", contents, "]]>");
    }

    private Link getViaLink(String requestUrl) {
        return getLink(requestUrl, LINK_TYPE_VIA, ATOM_MEDIA_TYPE);
    }

    private Link getSelfLink(String requestUrl) {
        return getLink(requestUrl, LINK_TYPE_SELF, ATOM_MEDIA_TYPE);
    }

    private Link getLink(String href, String rel, String type) {
        Link link = new Link();
        link.setHref(href);
        link.setRel(rel);
        link.setType(type);
        return link;
    }
}
