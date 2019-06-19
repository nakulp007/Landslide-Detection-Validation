package rss.news.litmus;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;


public class CBSLocalFeedMessage extends FeedMessage {
	static Logger logger = LoggerFactory.getLogger(CBSLocalFeedMessage.class);
	String author;

	public CBSLocalFeedMessage(String source, SyndEntry entry) {
		super();
		

		Map<String, String> values = parseEntry(entry);
		this.setAuthor(values.get("author"));
		this.setSource(source);
		this.setTitle(values.get("title"));
		this.setDescription(values.get("description"));
		this.setLink(values.get("link"));
		this.setPubDate(values.get("pubDate"));
	}

	private Map<String, String> parseEntry(SyndEntry entry) {
		Map<String, String> values = new HashMap<String, String>();
		
		values.put("author", entry.getAuthor() + "");
		values.put("title", entry.getTitle() + "");
		values.put("pubDate", entry.getPublishedDate() + "");
		
		String description = "";
		description += " " + Jsoup.parse(entry.getDescription().getValue()).text();
		for(int i=0; i < entry.getContents().size(); i++){
			description += " " + Jsoup.parse(((SyndContentImpl)entry.getContents().get(i)).getValue() + "").text();
		}
		
		values.put("description", description.trim());
		
		values.put("link", entry.getLink() + "");
		
		return values;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}
