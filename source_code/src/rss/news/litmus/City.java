package rss.news.litmus;

public class City {
	String name;
	String geoLocation;
	Double relevance;
	int count;
		
	
	public City(String name, String geoLocation, Double relevance, int count) {
		super();
		this.name = name;
		this.geoLocation = geoLocation;
		this.relevance = relevance;
		this.count = count;
	}

	public Double getRelevance() {
		return relevance;
	}

	public void setRelevance(Double relevance) {
		this.relevance = relevance;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(String geoLocation) {
		this.geoLocation = geoLocation;
	}
}
