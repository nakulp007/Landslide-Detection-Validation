import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.FileWriter;

import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import rss.news.litmus.*;

import com.alchemyapi.api.AlchemyAPI;
import com.google.gson.Gson;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.opencsv.*;

public class Main {
	static Logger logger = LoggerFactory.getLogger(Main.class);
	
	//interval to check sources again
	static long INTERVAL;
	//we can put this as args array so anything can be entered
	static String[] searchWords = {"landslide", "landslides", "mudslide", "mudslides", "rockslide", "rockslides", "rockfall", "rockfalls"};

	
	//Arguments: intervalInMins outputDirectory
    public static void main(String[] args) throws IOException {

    	try {
        	if (args.length == 1) {
        		
        		INTERVAL  = Integer.parseInt(args[0])*1000*60;
        		
                
        	    Timer timer = new Timer();
        	    timer.scheduleAtFixedRate(new TimerTask() {
        	            public void run() {
        	            	Map<String, String> feedList = generateFeedList(searchWords);	
        	                logger.debug("List of all feeds to query");
        	                for (String key: feedList.keySet()){
        	                    logger.debug(key + " : " + feedList.get(key));  
        	                } 
        	                
        	              //List to hold all feed messages from all sources
        	              ArrayList<FeedMessage> feedMessages  = grabAndParseAllFeeds(feedList);
        	              
        	              Gson gson = new Gson();
        	              logger.info("All Feed Messages: \n{}", gson.toJson(feedMessages));
        	              
        	              if (feedMessages.size() > 0) {
                            //holds all the sources outputted
                            String linkSources = "linkSources.csv";
                            List<String> linkSourceList = new ArrayList<String>();
                            try {
                                CSVReader reader = new CSVReader(new FileReader(linkSources),',', '"');
                                List<String[]> allRows = reader.readAll();
                                for(String[] row : allRows){
                                    String news_url = Arrays.toString(row);
                                    news_url = news_url.replace("[", "").replace("]", "");
                                    linkSourceList.add(news_url);
                                }

                                for(Iterator<FeedMessage> iterator = feedMessages.iterator(); iterator.hasNext();) {
                                    FeedMessage feedSource = iterator.next();
                                    if(linkSourceList.contains(feedSource.getLink())){
                                        iterator.remove();
                                    }
                                }
                            } catch (Exception e) {
                                logger.error(linkSources + "File does not exist. Will create new csv file");
                            }
							//write to file with datetime milliseconds since some date as filename
							try {
                                CSVWriter writer = new CSVWriter(new FileWriter(linkSources,true));
                                for(Iterator<FeedMessage> iterator = feedMessages.iterator(); iterator.hasNext();) {
                                    FeedMessage feedSource = iterator.next();
                                    writer.writeNext(new String[]{feedSource.getLink()});
                                }
                                writer.close();
								PrintWriter out = new PrintWriter("./output/" + new Date().getTime() + ".txt");
								out.println(gson.toJson(feedMessages));
								out.close();
							} catch (Exception e) {
								logger.error("Error creating output file", e);
							}
						}
        	            }
        	    }, 0, INTERVAL);
        	}else{
        		logger.error("Please provide valid arguments: <Interval In Mins> <Output Directory>");
        	}
        }
        catch (Exception e) {
            logger.error("Unknown error occurred", e);
        }
    }
    
    private static ArrayList<FeedMessage> grabAndParseAllFeeds(Map<String, String> feedList) {
    	//List to hold all feed messages from all sources
    	ArrayList<FeedMessage> feedMessages  = new ArrayList<FeedMessage>();
    	
    	//grab and parse each feed in feedList
    	for(String key: feedList.keySet()){
    		try {
				logger.info("Getting feed {} using URL {}", key, feedList.get(key));
				SyndFeed feed = getFeed(feedList.get(key));
				//System.out.println(feed);
				
				logger.trace("Fetched Feed:\n{}", feed);
				
				List<SyndEntry> entries = feed.getEntries();
	            for (SyndEntry entry : entries) {
	            	FeedMessage tempFM = null;
	            	
	            	
	            	if(key.matches("^HuffPost_.*$")){
	            		logger.debug("Creating and adding a HuffPostFeedMessage with Title: {}", entry.getTitle() + "");
	            		tempFM = new HuffPostFeedMessage(entry);
	            	}else if(key.matches("^TheGuardian.*$")){
	            		logger.debug("Creating and adding a TheGuardianFeedMessage with Title: {}", entry.getTitle() + "");
	            		tempFM = new TheGuardianFeedMessage(key, entry);
	            	}else if(key.matches("^ScienceDaily.*$")){
	            		logger.debug("Creating and adding a ScienceDailyFeedMessage with Title: {}", entry.getTitle() + "");
	            		tempFM = new ScienceDailyFeedMessage(key, entry);
	            	}else if(key.matches("^CBS.*$")){
	            		logger.debug("Creating and adding a CBSLocalFeedMessage with Title: {}", entry.getTitle() + "");
	            		tempFM = new CBSLocalFeedMessage(key, entry);
	            	}else if(key.matches("^Bing_.*$")){
	            		logger.debug("Creating and adding a BingFeedMessage with Title: {}", entry.getTitle() + "");
	            		tempFM = new BingFeedMessage(entry);
	            	}else if(key.matches("^Google_.*$")){
	            		logger.debug("Creating and adding a GoogleFeedMessage with Title: {}", entry.getTitle() + "");
	            		tempFM = new GoogleFeedMessage(entry);
	            	}else if(key.matches("^GoogleAlert_.*$")){
	            		logger.debug("Creating and adding a GoogleAlertFeedMessage with Title: {}", entry.getTitle() + "");
	            		tempFM = new GoogleAlertFeedMessage(key, entry);
                    }else if(key.matches("^BbcUK_.*$")){
                        logger.debug("Creating and adding a BbcUKFeedMessage with Title: {}", entry.getTitle() + "");
                        tempFM = new BbcUKFeedMessage(key, entry);
                    }else{
                        logger.debug("Creating and adding a StandardFeedMessage with Title: {}", entry.getTitle() + "");
                        tempFM = new StandardFeedMessage(key,entry);
	            	}
	            	
	            	
	            	//only add to list of feed messages from all providers if it doesn't exist
	            	//also only add if within published date within interval
	            	
	            	
	            	long now = new Date().getTime();
	            	logger.trace("Checking article with title: {} \n and description: {}", tempFM.getTitle(), tempFM.getDescription());
	            	logger.trace("now: {}", now);
        			logger.trace("pubDate: {}", tempFM.getPubDate().getTime());
        			logger.trace("difference: {}", now - tempFM.getPubDate().getTime());
        			logger.trace("difference in minutes: {}", (now - tempFM.getPubDate().getTime())/60/1000);
        			//check time interval - 10 Hours or Less
                    Integer checkInterval = 1000*60*8*60;
	            	if(tempFM != null && now - tempFM.getPubDate().getTime() <= checkInterval){
	            		//check if article relevant
	            		String regExKeyWords = generateRegExForCheckingRelevantArticles(searchWords);
	            		
	            		if(tempFM.getTitle().matches(regExKeyWords) || tempFM.getDescription().matches(regExKeyWords)){
	            			//check if its not dupliacate
	            			boolean duplicate = false;
	            			for(int i = 0; i < feedMessages.size(); i++){
			            		if(tempFM.getLink().equals(feedMessages.get(i).getLink())){
			            			duplicate = true;
			            			break;
			            		}
			            	}
	            			if(!duplicate){
	            				//check taxonomy to see if it is related article
	            				Taxonomy[] taxonomy = new Taxonomy[0];
	            				AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("api_key.txt");
	            				Document doc;
	            		        try {
	            		        	// Create an AlchemyAPI object.
		            		        logger.info("Checking Taxonomy using AlchemyAPI for {}", tempFM.getLink() + "");
		            		        doc = alchemyObj.URLGetTaxonomy(tempFM.getLink() + "");
	            					taxonomy = getTaxonomyFromDocument(doc);
	            					
	            				} catch (Exception e) {
	            					logger.error("Error occurred checking Taxonomy using AlchemyAPI", e);
	            					
	            					try{
										logger.info("Checking Entity using AlchemyAPI for {}", tempFM.getLink() + "");
			            		        doc = alchemyObj.URLGetRankedNamedEntities(tempFM.getLink() + "");
		            					tempFM.setCities(getCitiesFromDocument(doc));
									}catch(Exception ex){}
	            					
	            					//logger.debug("Adding article: {}", tempFM.getTitle());
		            				//feedMessages.add(tempFM);
	            				}
	            		        
	            		        if (taxonomy.length > 0) {
									//if taxonomy matches then add the article
									boolean matches = false;
									String[] relWords = { "weather",
											"disaster", "geology",
											"landslide", "mudslide",
											"rockslide"};
									String regExToMatch = generateRegExForCheckingRelevantArticles(relWords);
									for (int i=0; i < taxonomy.length; i++) {
										if (taxonomy[i].getLabel().matches(regExToMatch)) {
											matches = true;
											//add taxonomy info to tempFM before adding
											tempFM.setTaxonomyConfidence(taxonomy[i].getConfidenceScore());
											break;
										}
									}
									if (matches) {						
										try{
											logger.info("Checking Entity using AlchemyAPI for {}", tempFM.getLink() + "");
				            		        doc = alchemyObj.URLGetRankedNamedEntities(tempFM.getLink() + "");
			            					tempFM.setCities(getCitiesFromDocument(doc));
										}catch(Exception e){}
										
										logger.debug("Adding article: {}",
												tempFM.getTitle());
										feedMessages.add(tempFM);
									} else {
										logger.info(
												"Skipping irrelevant article based on AlchemyAPI Taxonomy: {}",
												tempFM.getTitle());
									}
								}else{
									logger.debug("No Taxonomy info available.");
									logger.info("Skipping irrelevant article: {}", tempFM.getTitle());
								}
	            			}else{
	            				logger.info("Skipping duplicate article: {}", tempFM.getTitle());
	            			}
	            		}else{
	            			logger.info("Skipping irrelevant article: {}", tempFM.getTitle());
	            		}
	            	}else{
	            		logger.info("Skipping old article: {}", tempFM.getTitle());
	            	}
	            }					
			} catch (IOException e){
				logger.error("IOException occurred", e);
			} catch (IllegalArgumentException e){
				logger.error("IllegalArgumentException occurred", e);
			} catch (FeedException e){
				logger.error("FeedException occurred", e);
			} catch (Exception e) {
				logger.error("Unknown error occurred", e);
			}
    	}
    	
    	return feedMessages;
	}
    
	private static City[] getCitiesFromDocument(Document doc) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);

        String xmlOutput =  writer.toString();
        logger.trace("XML Output of AlchemyAPI Entity \n {}", xmlOutput);
        
        InputStream iStream = new ByteArrayInputStream(xmlOutput.getBytes(StandardCharsets.UTF_8));
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document document = dBuilder.parse(iStream);
        
    	//optional, but recommended
    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
    	document.getDocumentElement().normalize();
     
    	logger.trace("Root element : {}", document.getDocumentElement().getNodeName());
     
    	NodeList nList = document.getElementsByTagName("entity");
     
    	logger.trace("----------------------------");
    	//ArrayList<String> labels = new ArrayList<String>();
    	List<City> cities = new ArrayList<City>();
    	
    	for (int temp = 0; temp < nList.getLength(); temp++) {
    		Node nNode = nList.item(temp);
    		logger.trace("\nCurrent Element : {}", nNode.getNodeName());
     
    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    			Element eElement = (Element) nNode;
    			try{
    				if(eElement.getElementsByTagName("type").item(0).getTextContent().toString().equals("City")){
    					String name = null;
    					String geoLocation = null;
    					Double relevance = null;
    					int count = 0;
    					
    					
    					try{
    						name = eElement.getElementsByTagName("text").item(0).getTextContent().toString();
    						//second option is better but might fail so try second
    						name = eElement.getElementsByTagName("name").item(0).getTextContent().toString();
    					}catch(Exception e){}
    					try{
    						geoLocation = eElement.getElementsByTagName("geo").item(0).getTextContent().toString();
    					}catch(Exception e){}
    					try{
    						relevance = Double.parseDouble(eElement.getElementsByTagName("relevance").item(0).getTextContent().toString());
    					}catch(Exception e){}
    					try{
    						count = Integer.parseInt(eElement.getElementsByTagName("count").item(0).getTextContent().toString());
    					}catch(Exception e){}
    							
    					cities.add(new City(name, geoLocation, relevance, count));
    				}
    					
    			} catch(Exception e){
    				logger.trace("Error parsing an Alchemy Entity element, moving to next one.");
    			}       			
    		}
    	}
    	
    	City[] citiesArray = cities.toArray(new City[cities.size()]);
    	
    	for(int i=0; i < citiesArray.length; i++){
    		logger.debug("Returning city: {} with relevance: {}, count: {}, and geoLocation: {}", citiesArray[i].getName(), citiesArray[i].getRelevance(), citiesArray[i].getCount(), citiesArray[i].getGeoLocation());
    	}
    	
        return citiesArray;
	}

	private static String generateRegExForCheckingRelevantArticles(String[] searchWords) {
		//case-insensitive, begining of string, any number of characters including new line characters, 
		//keywords, any number of characters, end of string
		//"(?i)^([\\s\\S]*?)(landslide|mudslide|item3)([\\s\\S]*?)$"
		StringBuilder allWords = new StringBuilder();
		
		if (searchWords.length > 0) {
		    for (String word : searchWords) {
		        allWords.append(word.trim()).append("|");
		    }
		    allWords.deleteCharAt(allWords.length() - 1);
		} else {
		    allWords.append("");
		}
		
		return "(?i)^([\\s\\S]*?)(" + allWords + ")([\\s\\S]*?)$";
	}
	private static SyndFeed getFeed(String feedURL) throws IOException, IllegalArgumentException, FeedException {
        SyndFeedInput input = new SyndFeedInput();
        XmlReader reader = new XmlReader(new URL(feedURL));
        SyndFeed feed = input.build(reader);
        logger.debug("Feed from {}\n{}", feedURL, feed);
        return feed;
	}
	private static Map<String, String> generateFeedList(String[] searchWords) {
		Map<String, String> feedList = new LinkedHashMap<String, String>();

        //Google Alerts
		feedList.put("GoogleAlert_LandslideMudslideRockslide", "https://www.google.com/alerts/feeds/07354341476456237719/10661831750158805806");
        feedList.put("GoogleAlert_Landslide_Only", "https://www.google.com/alerts/feeds/07354341476456237719/11877156655605005722");

        //Landslide Specific
		feedList.put("TheGuardian_NaturalDisasters", "http://www.theguardian.com/world/natural-disasters/rss");
		feedList.put("TheGuardian_Landslides", "http://www.theguardian.com/world/landslides/rss");
		feedList.put("ScienceDaily_NaturalDisasters", "http://feeds.sciencedaily.com/sciencedaily/earth_climate/natural_disasters");
		feedList.put("ScienceDaily_Landslides", "http://feeds.sciencedaily.com/sciencedaily/earth_climate/landslides");
		feedList.put("HuffPost_NaturalDisasters", "http://www.huffingtonpost.com/news/natural-disasters/feed/");

        //US Local
		feedList.put("CBS_Atlanta", "http://atlanta.cbslocal.com/feed/");
		feedList.put("CBS_Boston", "http://boston.cbslocal.com/feed/");
		feedList.put("CBS_Baltimore", "http://baltimore.cbslocal.com/feed/");
		//feedList.put("CBS_Charlotte", "http://charlotte.cbslocal.com/feed/");
		feedList.put("CBS_Chicago", "http://chicago.cbslocal.com/feed/");
		feedList.put("CBS_Cleveland", "http://cleveland.cbslocal.com/feed/");
		feedList.put("CBS_Connecticut", "http://connecticut.cbslocal.com/feed/");
		feedList.put("CBS_Dallas_FortWorth", "http://dfw.cbslocal.com/feed/");
		feedList.put("CBS_Denver", "http://denver.cbslocal.com/feed/");
		feedList.put("CBS_Houston", "http://houston.cbslocal.com/feed/");
		feedList.put("CBS_LasVegas", "http://lasvegas.cbslocal.com/feed/");
		feedList.put("CBS_LosAngeles", "http://losangeles.cbslocal.com/feed/");
		feedList.put("CBS_Miami", "http://miami.cbslocal.com/feed/");
		feedList.put("CBS_Minnesota", "http://minnesota.cbslocal.com/feed/");
		feedList.put("CBS_NewYork", "http://newyork.cbslocal.com/feed/");
		feedList.put("CBS_Philadelphia", "http://philadelphia.cbslocal.com/feed/");
		feedList.put("CBS_Pittsburgh", "http://pittsburgh.cbslocal.com/feed/");
		feedList.put("CBS_Sacramento", "http://sacramento.cbslocal.com/feed/");
		feedList.put("CBS_SanFrancisco", "http://sanfrancisco.cbslocal.com/feed/");
		feedList.put("CBS_StLouis", "http://stlouis.cbslocal.com/feed/");
		feedList.put("CBS_Tampa", "http://tampa.cbslocal.com/feed/");
		feedList.put("CBS_Washington", "http://washington.cbslocal.com/feed/");

        //BBC World
        feedList.put("BbcUK_WorldAfrica", "http://feeds.bbci.co.uk/news/world/africa/rss.xml");
        feedList.put("BbcUK_WorldAmericas", "http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");
        feedList.put("BbcUK_WorldAsia", "http://feeds.bbci.co.uk/news/world/asia/rss.xml");
        feedList.put("BbcUK_WorldEurope", "http://feeds.bbci.co.uk/news/world/europe/rss.xml");
        feedList.put("BbcUK_WorldMiddleEast", "http://feeds.bbci.co.uk/news/world/middle_east/rss.xml");

        //The Guardian World
        feedList.put("TheGuardian_UkNews", "http://www.theguardian.com/uk-news/rss");
        feedList.put("TheGuardian_EuropeNews", "http://www.theguardian.com/world/europe-news/rss");
        feedList.put("TheGuardian_AmericasNews", "http://www.theguardian.com/world/americas/rss");
        feedList.put("TheGuardian_AsiaNews", "http://www.theguardian.com/world/asia/rss");
        feedList.put("TheGuardian_MiddleEastNews", "http://www.theguardian.com/world/middleeast/rss");
        feedList.put("TheGuardian_AfricaNews", "http://www.theguardian.com/world/africa/rss");
        feedList.put("TheGuardian_AustraliaNews", "http://www.theguardian.com/australia-news/rss");

        //HuffPost World

        feedList.put("HuffPostStandard_WorldNews", "http://www.huffingtonpost.com/feeds/verticals/world/index.xml");
        feedList.put("HuffPost_WorldAsiaNews", "http://www.huffingtonpost.com/news/worldpost-asia-pacific/feed/");
        feedList.put("HuffPost_WorldMidEastNews", "http://www.huffingtonpost.com/news/worldpost-middle-east/feed/");
        feedList.put("HuffPost_WorldAmericasNews", "http://www.huffingtonpost.com/news/worldpost-americas/feed/");
        feedList.put("HuffPost_WorldAfricaNews", "http://www.huffingtonpost.com/news/worldpost-africa/feed/");
        feedList.put("HuffPost_WorldEuropeNews", "http://www.huffingtonpost.com/news/worldpost-europe/feed/");
        feedList.put("HuffPost_WorldGlobalOrderNews", "http://www.huffingtonpost.com/news/worldpost-global-order/feed/");

        //ABCNews
        feedList.put("ABCNews_USNews", "http://feeds.abcnews.com/abcnews/usheadlines");

        //CNN
        feedList.put("CNN_WorldNews", "http://rss.cnn.com/rss/cnn_world.rss");
        feedList.put("CNN_USNews", "http://rss.cnn.com/rss/cnn_us.rss");


        //NY Times
        feedList.put("NYTimes_WorldNews", "http://rss.nytimes.com/services/xml/rss/nyt/World.xml");
        feedList.put("Forbes_LatestHeadlines", "http://www.forbes.com/real-time/feed2/");
        feedList.put("TimesOfIndia_LatestHeadlines", "http://timesofindia.feedsportal.com/c/33039/f/533916/index.rss");
        feedList.put("FoxNews_Latest", "http://feeds.foxnews.com/foxnews/latest");
        feedList.put("FoxNews_World", "http://feeds.foxnews.com/foxnews/world");
        feedList.put("Reuters_Top", "http://feeds.reuters.com/reuters/topNews");
        feedList.put("Reuters_World", "http://feeds.reuters.com/Reuters/worldNews");
        feedList.put("NewsComAu_National", "http://feeds.news.com.au/public/rss/2.0/news_national_3354.xml");
        feedList.put("NewsComAu_World", "http://feeds.news.com.au/public/rss/2.0/news_theworld_3356.xml");

		
		for(int i = 0; i < searchWords.length; i++){
			//Bing rss no filter
			//feedList.put("Bing_" + searchWords[i], "http://www.bing.com/news?q=" + searchWords[i] + "&format=RSS");
			
			//Bing rss with "Past Hour" filter. Past hour of their update, not article date. This is to get less popular stories. Could result in lot of irrelevant information.
			//feedList.put("Bing_" + searchWords[i], "https://www.bing.com/news/search?q=" + searchWords[i].trim() + "&qft=interval%3d%224%22&form=PTFTNR&format=RSS");

            //Bing rss with "Past 24 Hour" filter. Past hour of their update, not article date. This is to get less popular stories. Could result in lot of irrelevant information.
            feedList.put("Bing_" + searchWords[i], "https://www.bing.com/news/search?q=" + searchWords[i].trim() + "&qft=interval%3d%228%22&form=PTFTNR&format=RSS");

			//Google with no filter
			feedList.put("Google_" + searchWords[i], "https://news.google.com/news?q=" + searchWords[i].trim() + "&output=rss");
		}		
		
		return feedList;
	}
	
	private static Taxonomy[] getTaxonomyFromDocument(Document doc) throws JAXBException, SAXException, IOException, ParserConfigurationException, TransformerException {
		DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);

        String xmlOutput =  writer.toString();
        logger.trace("XML Output of AlchemyAPI Taxonomy \n {}", xmlOutput);
        
        InputStream iStream = new ByteArrayInputStream(xmlOutput.getBytes(StandardCharsets.UTF_8));
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document document = dBuilder.parse(iStream);
        
    	//optional, but recommended
    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
    	document.getDocumentElement().normalize();
     
    	logger.trace("Root element : {}", document.getDocumentElement().getNodeName());
     
    	NodeList nList = document.getElementsByTagName("element");
     
    	logger.trace("----------------------------");
    	//ArrayList<String> labels = new ArrayList<String>();
    	List<Taxonomy> taxonomy = new ArrayList<Taxonomy>();
    	
    	for (int temp = 0; temp < nList.getLength(); temp++) {
    		Node nNode = nList.item(temp);
    		logger.trace("\nCurrent Element : {}", nNode.getNodeName());
     
    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    			Element eElement = (Element) nNode;
    			taxonomy.add(new Taxonomy(eElement.getElementsByTagName("label").item(0).getTextContent(), Double.parseDouble(eElement.getElementsByTagName("score").item(0).getTextContent())));
    		}
    	}
    	
    	Taxonomy[] taxonomyArray = taxonomy.toArray(new Taxonomy[taxonomy.size()]);
    	
    	for(int i=0; i < taxonomyArray.length; i++){
    		logger.debug("Returning taxonomy with label: {} and confidence: {}", taxonomyArray[i].getLabel(), taxonomyArray[i].getConfidenceScore());
    	}
    	
        return taxonomyArray;
    }
}