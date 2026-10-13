package fr.zenika.opensource.stats.beans.github.graphql;

public class PullRequestNode {

    private String title;
    private String url;
    private String state;
    private String createdAt;
    private PullRequestRepository repository;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public PullRequestRepository getRepository() {
        return repository;
    }

    public void setRepository(PullRequestRepository repository) {
        this.repository = repository;
    }
}
