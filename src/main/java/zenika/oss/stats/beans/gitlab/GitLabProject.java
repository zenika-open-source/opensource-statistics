package zenika.oss.stats.beans.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import zenika.oss.stats.beans.Project;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabProject extends Project {
    public GitLabProject() {
        this.setSource("GitLab");
    }

    private String description;
    @JsonProperty("forked_from_project")
    private Object forkedFromProject;

    @JsonProperty("path_with_namespace")
    public void setGitLabPathWithNamespace(String pathWithNamespace) {
        this.setFullName(pathWithNamespace);
    }

    @JsonProperty("web_url")
    public void setGitLabWebUrl(String webUrl) {
        this.setUrl(webUrl);
    }

    @JsonProperty("star_count")
    public void setGitLabStarCount(Long starCount) {
        this.setStarsCount(starCount);
    }

    @JsonProperty("forks_count")
    public void setGitLabForksCount(Long forksCount) {
        this.setForks(forksCount);
    }

    @JsonProperty("archived")
    public void setGitLabArchived(Object archived) {
        if (archived instanceof Boolean) {
            this.setArchived((Boolean) archived);
        } else if (archived instanceof String) {
            this.setArchived(Boolean.parseBoolean((String) archived));
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isFork() {
        return forkedFromProject != null;
    }

    public Object getForkedFromProject() {
        return forkedFromProject;
    }

    public void setForkedFromProject(Object forkedFromProject) {
        this.forkedFromProject = forkedFromProject;
    }

    // Compatibility getters if needed by other parts of the code
    public String getPathWithNamespace() {
        return getFullName();
    }

    public String getWebUrl() {
        return getUrl();
    }

    public Long getStarCount() {
        return getStarsCount();
    }

    public Long getForksCount() {
        return getForks();
    }

}
