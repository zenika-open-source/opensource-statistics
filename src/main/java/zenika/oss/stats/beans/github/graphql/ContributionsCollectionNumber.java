package zenika.oss.stats.beans.github.graphql;

public class ContributionsCollectionNumber {
    
    private PullRequestContributions pullRequestContributions;

    public PullRequestContributions getPullRequestContributions() {

        return pullRequestContributions;
    }

    public void setPullRequestContributions(final PullRequestContributions pullRequestContributions) {

        this.pullRequestContributions = pullRequestContributions;
    }
}
