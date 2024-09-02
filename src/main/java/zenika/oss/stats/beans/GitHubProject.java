package zenika.oss.stats.beans;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class GitHubProject {
    public String id;
    public String name;
    public String full_name;
    public String html_url;
    public boolean private_field;
    private boolean fork;
    private String visibility;
    private int watchers_count;
    private int forks;
    
    public int getWatchers_count() {

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

    public int getForks() {

        return forks;
    }
}
