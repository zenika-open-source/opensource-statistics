package zenika.oss.stats.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private String id;
    private String name;
    private String full_name;
    private String html_url;
    private String visibility;
    private boolean fork;
    private boolean archived;
    private Long watchers_count;
    private Long forks;
    private String source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Long getWatchers_count() {
        return watchers_count;
    }

    public void setWatchers_count(Long watchers_count) {
        this.watchers_count = watchers_count;
    }

    public Long getForks() {
        return forks;
    }

    public void setForks(Long forks) {
        this.forks = forks;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    // Modern aliases if needed
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getFullName() {
        return full_name;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setFullName(String fullName) {
        this.full_name = fullName;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getUrl() {
        return html_url;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setUrl(String url) {
        this.html_url = url;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Long getStarsCount() {
        return watchers_count;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setStarsCount(Long starsCount) {
        this.watchers_count = starsCount;
    }
}
