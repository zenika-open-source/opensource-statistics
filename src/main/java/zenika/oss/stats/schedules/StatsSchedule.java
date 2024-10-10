package zenika.oss.stats.schedules;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StatsSchedule {

    @Scheduled(cron = "{stats.cron}")
    void getUsersFromGitHubOrganization() {
        // TODO
    }
}
