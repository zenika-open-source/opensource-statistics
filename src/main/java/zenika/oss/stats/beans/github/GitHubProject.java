package zenika.oss.stats.beans.github;

public class GitHubProject {
    public String id;
    public String name;
    public String full_name;
    public String html_url;
    public boolean private_field;
    private boolean fork;
    private boolean archived;
    private String visibility;
    private Long watchers_count;
    private Long forks;
    
    public Long getWatchers_count() {

        return watchers_count;
    }

    public String getFull_name() {

        return full_name;
    }
    
    public String getHtml_url() {

        return html_url;
    }

    public boolean isFork() {

        return fork;
    }

    public String getName() {

        return name;
    }

    public String getId() {

        return id;
    }

    public boolean isPrivate() {

        return private_field;
    }

    public String getVisibility() {

        return visibility;
    }

    public Long getForks() {

        return forks;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public boolean isPrivate_field() {
        return private_field;
    }

    public void setPrivate_field(boolean private_field) {
        this.private_field = private_field;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setWatchers_count(Long watchers_count) {
        this.watchers_count = watchers_count;
    }

    public void setForks(Long forks) {
        this.forks = forks;
    }
}
