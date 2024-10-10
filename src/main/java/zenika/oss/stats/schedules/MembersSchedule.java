package zenika.oss.stats.schedules;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MembersSchedule {

    @Scheduled(cron = "{members.cron}")
    void getUsersFromGitHubOrganization() {
        // TODO
    }
}
