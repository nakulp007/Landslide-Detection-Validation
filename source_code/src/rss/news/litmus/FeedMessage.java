package rss.news.litmus;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Represents one RSS message
 */
public class FeedMessage {
	static Logger logger = LoggerFactory.getLogger(FeedMessage.class);

	String source;
	String title;
  String description;
  String link;
  String guid = java.util.UUID.randomUUID().toString();
  Date pubDate;
  Double taxonomyConfidence;
  //Taxonomy[] taxonomy;
  City[] cities;
  

  public FeedMessage(){
	  super();
  }
  
  public FeedMessage(String source, String title, String description, String link,
			String author, String pubDate) {
		super();
		this.source = source;
		this.title = title;
		this.description = description;
		this.link = link;
		setPubDate(pubDate);
	}
  public Date getPubDate() {
	return pubDate;
}

public void setPubDate(String pubDate) {
	this.pubDate = convertDate(pubDate);
}

private Date convertDate(String pubDate) {
	Date parsed = null;
	SimpleDateFormat format =  new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    try {
        parsed = format.parse(pubDate);
    }
    catch(Exception ex) {
        System.out.println("ERROR: Cannot parse \"" + pubDate + "\"");
    }
    return parsed;
}

public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public String getSource() {
	return source;
}
public void setSource(String source) {
	this.source = source;
}
/*
public Taxonomy[] getTaxonomy() {
	return taxonomy;
}

public void setTaxonomy(Taxonomy[] taxonomy) {
	this.taxonomy = taxonomy;
}
*/

public Double getTaxonomyConfidence() {
	return taxonomyConfidence;
}

public void setTaxonomyConfidence(Double taxonomyConfidence) {
	this.taxonomyConfidence = taxonomyConfidence;
}



public City[] getCities() {
	return cities;
}

public void setCities(City[] cities) {
	this.cities = cities;
}

public void setPubDate(Date pubDate) {
	this.pubDate = pubDate;
}

@Override
  public String toString() {
	return "FeedMessage [source=" + source + ", title=" + title + ", published date=" + pubDate + ", description=" + description
        + ", link=" + link + ", taxonomy=" + this.getTaxonomyConfidence() + ", guid=" + guid
        + "]";
  }

} 