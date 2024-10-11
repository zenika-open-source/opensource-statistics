package zenika.oss.stats.beans;

public class CustomStatsContributionsUserByMonth {
    
    private int month;
    private String monthLabel;
    private int contributions;

    public CustomStatsContributionsUserByMonth(final int month, final String monthLabel, final int contributions) {

        this.month = month;
        this.monthLabel = monthLabel;
        this.contributions = contributions;
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
}
