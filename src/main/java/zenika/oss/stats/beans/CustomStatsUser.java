package zenika.oss.stats.beans;

import java.util.List;

public class CustomStatsUser {
    
    private String login;
    
    private List<CustomStatsContributionsUserByMonth> contributionsUserByMonths;

    public CustomStatsUser(final String login, final List<CustomStatsContributionsUserByMonth> contributionsUserByMonths) {

        this.login = login;
        this.contributionsUserByMonths = contributionsUserByMonths;
    }

    public String getLogin() {

        return login;
    }

    public void setLogin(final String login) {

        this.login = login;
    }

    public List<CustomStatsContributionsUserByMonth> getContributionsUserByMonths() {

        return contributionsUserByMonths;
    }

    public void setContributionsUserByMonths(final List<CustomStatsContributionsUserByMonth> contributionsUserByMonths) {

        this.contributionsUserByMonths = contributionsUserByMonths;
    }
}
