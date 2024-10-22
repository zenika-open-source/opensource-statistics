package zenika.oss.stats.beans.github.graphql;

public class ContributionsCollection {
    
    private int totalIssueContributions;
    private int totalCommitContributions;
    private int totalPullRequestContributions;
    private int totalPullRequestReviewContributions;
    private int totalRepositoryContributions;

    public int getTotalIssueContributions() {

        return totalIssueContributions;
    }

    public int getTotalCommitContributions() {

        return totalCommitContributions;
    }

    public int getTotalPullRequestContributions() {

        return totalPullRequestContributions;
    }

    public int getTotalPullRequestReviewContributions() {

        return totalPullRequestReviewContributions;
    }

    public int getTotalRepositoryContributions() {

        return totalRepositoryContributions;
    }
}
