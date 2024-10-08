package net.cfxp.api.composite.product;

/**
 * RecommendationSummary
 */
public class RecommendationSummary {
   


    private final int recommendationId;
    private final String author;
    private final int rate;
    private final String content;

    public String getContent() {
        return content;
    }

    public RecommendationSummary(int recommendationId, String author, int rate) {
        this.recommendationId = recommendationId;
        this.author = author;
        this.rate = rate;
        this.content = null;
    }

    public RecommendationSummary(int recommendationId, String author, int rate, String content) {
        this.recommendationId = recommendationId;
        this.author = author;
        this.rate = rate;
        this.content = content;
    }

    public int getRecommendationId() {
        return recommendationId;
    }

    public String getAuthor() {
        return author;
    }

    public int getRate() {
        return rate;
    }
}