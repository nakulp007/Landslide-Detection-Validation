package rss.news.litmus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndEntry;


public class BingFeedMessage extends FeedMessage {
	static Logger logger = LoggerFactory.getLogger(BingFeedMessage.class);

	public BingFeedMessage(SyndEntry entry) {
		super();
		
		Map<String, String> values = parseEntry(entry);
		this.setSource(values.get("source"));
		this.setTitle(values.get("title"));
		this.setDescription(values.get("description"));
		this.setLink(values.get("link"));
		this.setPubDate(values.get("pubDate"));
	}

	private Map<String, String> parseEntry(SyndEntry entry) {
		Map<String, String> values = new HashMap<String, String>();
		
		values.put("title", entry.getTitle() + "");
		values.put("pubDate", entry.getPublishedDate() + "");
		
		values.put("description", entry.getDescription().getValue() + "");
    	
    	List<Element> foreignMarkups = (List<Element>) entry.getForeignMarkup();
		for (Element foreignMarkup : foreignMarkups) {
			if(foreignMarkup.getName().equals("Source")){
				values.put("source", foreignMarkup.getValue() + "");
			}
		}          
		values.put("link", Commons.getRedirectURL(entry.getLink() + ""));
		
		return values;
	}
}
