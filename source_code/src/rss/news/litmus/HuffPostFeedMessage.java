package rss.news.litmus;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;

public class HuffPostFeedMessage extends FeedMessage {
	static Logger logger = LoggerFactory.getLogger(HuffPostFeedMessage.class);

	public HuffPostFeedMessage(SyndEntry entry) {
		super();
		
		Map<String, String> values = parseEntry(entry);
		this.setSource("Huffington Post - Natural Disasters");
		this.setTitle(values.get("title"));
		this.setDescription(values.get("description"));
		this.setLink(values.get("link"));
		this.setPubDate(values.get("pubDate"));
	}
	
	private Map<String, String> parseEntry(SyndEntry entry) {
		Map<String, String> values = new HashMap<String, String>();
		
		values.put("title", entry.getTitle() + "");
		values.put("pubDate", entry.getPublishedDate() + "");
		values.put("description", Jsoup.parse(((SyndContentImpl)entry.getContents().get(0)).getValue()).text() + "");
		values.put("link", entry.getLink() + "");
		
		return values;
	}

}
