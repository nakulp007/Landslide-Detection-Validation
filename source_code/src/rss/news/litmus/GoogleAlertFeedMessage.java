package rss.news.litmus;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;

public class GoogleAlertFeedMessage extends FeedMessage {
	static Logger logger = LoggerFactory.getLogger(GoogleAlertFeedMessage.class);

	public GoogleAlertFeedMessage(String source, SyndEntry entry) {
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
		
		values.put("title", Jsoup.parse(entry.getTitle()).text() + "");
		values.put("pubDate", entry.getPublishedDate() + "");
		String description = "";
		for(int i=0; i < entry.getContents().size(); i++){
			description += " " + Jsoup.parse(((SyndContentImpl)entry.getContents().get(i)).getValue() + "").text();
		}
		values.put("description", description.trim());
		values.put("source", "Google Alert");
		
		String myURL = Commons.getRedirectURL(entry.getLink() + "");		
		if(myURL.matches("(?i)^([\\s\\S]*?)(www.google.com)([\\s\\S]*?)$")){
			logger.debug("Stripping url from Google link");
			Matcher matcher = Pattern.compile("&url=(.*)&ct=").matcher(myURL);
			while ( matcher.find() ) {
			    String temp = matcher.group(0);
			    myURL = temp.substring(5, temp.length() -4);
			    break;
			}	
		}		
		logger.debug("Returning URL: {}", myURL);
		values.put("link", myURL);
		
		return values;
	}
}
