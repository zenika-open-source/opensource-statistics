package zenika.oss.stats.ui;

import io.javelit.core.Jt;
import io.javelit.core.Server;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import zenika.oss.stats.ui.tabs.ContributionsTab;
import zenika.oss.stats.ui.tabs.MembersTab;
import zenika.oss.stats.ui.tabs.ProjectsTab;
import zenika.oss.stats.ui.tabs.StatsTab;

import java.util.List;

@ApplicationScoped
public class JavelitDashboard {

    @Inject
    MembersTab membersTab;

    @Inject
    ProjectsTab projectsTab;

    @Inject
    ContributionsTab contributionsTab;

    @Inject
    StatsTab statsTab;

    void onStart(@Observes StartupEvent ev) {
        Server.builder(() -> {
            Jt.header("📊 Opensource Statistics Dashbord").use();
            Jt.subheader("Welcome to the Zenika Open Source contributions dashboard").use();
            Jt.markdown(
                    "This dashboard get publics datas from GitHub (and GitLab as soon)")
                    .use();

            var tabs = Jt.tabs(List.of("🙋 Members", "🚀 Projects", "📊 Contributions", "📈 Stats")).use();

            membersTab.render(tabs.tab("🙋 Members"));
            projectsTab.render(tabs.tab("🚀 Projects"));
            contributionsTab.render(tabs.tab("📊 Contributions"));
            statsTab.render(tabs.tab("📈 Stats"));

        }, 8888).build().start();
    }
}
