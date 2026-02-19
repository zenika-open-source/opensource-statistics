package zenika.oss.stats.beans.gitlab.graphql;

public class GitLabCalendarDay {
    private String date;
    private int count;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
