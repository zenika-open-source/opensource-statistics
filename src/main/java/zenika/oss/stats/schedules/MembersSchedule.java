package zenika.oss.stats.schedules;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.reflect.Member;
import java.util.List;

@ApplicationScoped
public class MembersSchedule {

    @Scheduled(cron = "{members.cron}")
    void getUsersFromGitHubOrganization() {
        // TODO
    }
}
