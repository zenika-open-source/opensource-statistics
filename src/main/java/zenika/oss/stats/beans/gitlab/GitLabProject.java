package zenika.oss.stats.beans.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabProject {
    private String id;
    private String name;
    @JsonProperty("path_with_namespace")
    private String pathWithNamespace;
    @JsonProperty("web_url")
    private String webUrl;
    private String description;
    @JsonProperty("star_count")
    private Long starCount;
    @JsonProperty("forks_count")
    private Long forksCount;
    @JsonProperty("forked_from_project")
    private Object forkedFromProject;

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

    public String getPathWithNamespace() {
        return pathWithNamespace;
    }

    public void setPathWithNamespace(String pathWithNamespace) {
        this.pathWithNamespace = pathWithNamespace;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getStarCount() {
        return starCount;
    }

    public void setStarCount(Long starCount) {
        this.starCount = starCount;
    }

    public Long getForksCount() {
        return forksCount;
    }

    public void setForksCount(Long forksCount) {
        this.forksCount = forksCount;
    }

    public boolean isFork() {
        return forkedFromProject != null;
    }

    public Object getForkedFromProject() {
        return forkedFromProject;
    }

    public void setForkedFromProject(Object forkedFromProject) {
        this.forkedFromProject = forkedFromProject;
    }
}
