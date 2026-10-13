package fr.zenika.opensource.stats.beans;

import java.util.List;
import java.util.Optional;

public class CustomStatsContributionsUserByMonth {

    private int month;
    private String monthLabel;
    private int contributions;
    private List<ContributionDetail> contributionsDetails;

    public CustomStatsContributionsUserByMonth(final int month, final String monthLabel, final int contributions) {
        this(month, monthLabel, contributions, List.of());
    }

    public CustomStatsContributionsUserByMonth(final int month, final String monthLabel, final int contributions,
            final List<ContributionDetail> contributionsDetails) {

        this.month = month;
        this.monthLabel = monthLabel;
        this.contributions = contributions;
        this.contributionsDetails = Optional.ofNullable(contributionsDetails).orElse(List.of());
    }

    public int getMonth() {

        return month;
    }

    public String getMonthLabel() {

        return monthLabel;
    }

    public int getContributions() {

        return contributions;
    }

    public List<ContributionDetail> getContributionsDetails() {
        return contributionsDetails;
    }
}

