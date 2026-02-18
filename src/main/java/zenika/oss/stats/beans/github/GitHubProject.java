package zenika.oss.stats.beans.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import zenika.oss.stats.beans.Project;

public class GitHubProject extends Project {

    public GitHubProject() {
        this.setSource("GitHub");
    }

    @JsonProperty("full_name")
    public void setGitHubFullName(String fullName) {
        this.setFullName(fullName);
    }

    @JsonProperty("html_url")
    public void setGitHubUrl(String url) {
        this.setUrl(url);
    }

    @JsonProperty("watchers_count")
    public void setGitHubStarsCount(Long starsCount) {
        this.setStarsCount(starsCount);
    }

    @JsonProperty("forks")
    public void setGitHubForksCount(Long forksCount) {
        this.setForks(forksCount);
    }

    // Compatibility for existing code using specific GitHub names if needed
    // But we should ideally update the code to use the base class methods.

    public String getFull_name() {
        return getFullName();
    }

    public void setFull_name(String fullName) {
        setFullName(fullName);
    }

    public String getHtml_url() {
        return getUrl();
    }

    public void setHtml_url(String url) {
        setUrl(url);
    }

    public Long getWatchers_count() {
        return getStarsCount();
    }

    public void setWatchers_count(Long watchersCount) {
        setStarsCount(watchersCount);
    }

    public Long getForks() {
        return super.getForks();
    }

    public void setForks(Long forks) {
        super.setForks(forks);
    }

    public boolean isArchived() {
        return super.isArchived();
    }

    public void setArchived(boolean archived) {
        super.setArchived(archived);
    }

    public String getVisibility() {
        return super.getVisibility();
    }

    public void setVisibility(String visibility) {
        super.setVisibility(visibility);
    }
}
