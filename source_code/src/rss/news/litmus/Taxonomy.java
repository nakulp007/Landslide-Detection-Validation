package rss.news.litmus;

public class Taxonomy {
	String label;
	Double confidenceScore;

	public Taxonomy(String label, Double confidenceScore) {
		super();
		this.label = label;
		this.confidenceScore = confidenceScore;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Double getConfidenceScore() {
		return confidenceScore;
	}

	public void setConfidenceScore(Double confidenceScore) {
		this.confidenceScore = confidenceScore;
	}
}
