package rss.news.litmus;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Commons {
	static Logger logger = LoggerFactory.getLogger(Commons.class);

	public static String getRedirectURL(String url){
		logger.debug("original url: {}", url);
		String redirectURL = "";
		try{
			Response response = Jsoup.connect(url).timeout(10*1000).followRedirects(true).execute();
			redirectURL = response.url().toString();
		}catch(HttpStatusException e){
			if(e.getMessage().contains("HTTP error fetching URL")){
				logger.warn("Encountered problem getting redirect URL for {}", url);
				logger.warn("Using URL: {}", e.getUrl());
				redirectURL = e.getUrl();
			}else{
				logger.error("Encountered problem getting redirect URL for {}", url, e);
				logger.info("Using original URL");
				redirectURL = url;
			}			
		}catch (Exception e) {
			logger.error("Encountered problem getting redirect URL for {}", url, e);
			logger.info("Using original URL");
			redirectURL = url;
		}
		logger.debug("redirect url: {}", redirectURL);
		return redirectURL + "";
	}
}
