package rss.news.litmus;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndEntry;


public class GoogleFeedMessage extends FeedMessage {
	static Logger logger = LoggerFactory.getLogger(GoogleFeedMessage.class);

	public GoogleFeedMessage(SyndEntry entry) {
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

		values.put("title", entry.getTitle().substring(0, entry.getTitle().indexOf(" - ")) + "");
		values.put("pubDate", entry.getPublishedDate() + "");
		values.put("description", Jsoup.parse(entry.getDescription().getValue()).text() + "");
		values.put("source", entry.getTitle().substring(entry.getTitle().lastIndexOf(" - ")+3) + "");
		     
		values.put("link", Commons.getRedirectURL(entry.getLink() + ""));
		
		return values;
	}
}
