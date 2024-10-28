package zenika.oss.stats.beans.github;

public class GitHubMember {
    public String login;

    public String id;
    
    public String type;
    
    public String getId() {

        return id;
    }

    
    public String getLogin() {

        return login;
    }

    public String getType() {

        return type;
    }

    public void setLogin(final String login) {

        this.login = login;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public void setType(final String type) {

        this.type = type;
    }
}
