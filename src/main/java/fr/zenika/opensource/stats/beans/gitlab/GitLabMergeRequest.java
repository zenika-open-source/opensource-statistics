package fr.zenika.opensource.stats.beans.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.zenika.opensource.stats.beans.ContributionDetail;

import java.net.URI;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabMergeRequest {

    private String title;
    private String state;

    @JsonProperty("web_url")
    private String webUrl;

    @JsonProperty("created_at")
    private String createdAt;

    private GitLabReferences references;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitLabReferences {
        private String full;

        public String getFull() {
            return full;
        }

        public void setFull(String full) {
            this.full = full;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public GitLabReferences getReferences() {
        return references;
    }

    public void setReferences(GitLabReferences references) {
        this.references = references;
    }

    public Optional<String> extractProjectUrl() {
        return Optional.ofNullable(webUrl)
                .filter(url -> url.contains("/-/merge_requests/"))
                .map(url -> url.substring(0, url.indexOf("/-/merge_requests/")));
    }

    public Optional<String> extractProjectFullName() {
        Optional<String> fromReferences = Optional.ofNullable(references)
                .map(GitLabReferences::getFull)
                .filter(full -> full.contains("!"))
                .map(full -> full.substring(0, full.lastIndexOf('!')));

        if (fromReferences.isPresent()) {
            return fromReferences;
        }

        return extractProjectUrl()
                .map(url -> {
                    try {
                        String path = URI.create(url).getPath();
                        return path != null && path.startsWith("/") ? path.substring(1) : path;
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(path -> !path.isEmpty());
    }

    public Optional<String> extractProjectName() {
        return extractProjectFullName()
                .map(fullName -> {
                    int lastSlash = fullName.lastIndexOf('/');
                    return lastSlash >= 0 ? fullName.substring(lastSlash + 1) : fullName;
                });
    }

    public ContributionDetail toContributionDetail() {
        String fullName = extractProjectFullName().orElse("");
        String name = extractProjectName().orElse(fullName);
        String projectUrl = extractProjectUrl().orElse("");
        return new ContributionDetail(
                name,
                fullName,
                projectUrl,
                Optional.ofNullable(title).orElse(""),
                Optional.ofNullable(webUrl).orElse(""),
                Optional.ofNullable(state).orElse(""),
                Optional.ofNullable(createdAt).orElse(""));
    }
}
