package rss.news.litmus;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndEntry;

public class TheGuardianFeedMessage extends FeedMessage {
	static Logger logger = LoggerFactory.getLogger(TheGuardianFeedMessage.class);

	public TheGuardianFeedMessage(String source, SyndEntry entry) {
		super();
		

		Map<String, String> values = parseEntry(entry);
		this.setSource(source);
		this.setTitle(values.get("title"));
		this.setDescription(values.get("description"));
		this.setLink(values.get("link"));
		this.setPubDate(values.get("pubDate"));
	}

	private Map<String, String> parseEntry(SyndEntry entry) {
		Map<String, String> values = new HashMap<String, String>();
		
		values.put("title", entry.getTitle() + "");
		values.put("pubDate", entry.getPublishedDate() + "");
		values.put("description", Jsoup.parse(entry.getDescription().getValue()).text().trim() + "");
		values.put("link", entry.getLink() + "");
		
		return values;
	}
}
