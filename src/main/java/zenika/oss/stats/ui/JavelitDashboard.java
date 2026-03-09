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
import zenika.oss.stats.ui.tabs.OrganizationProjectsTab;
import zenika.oss.stats.ui.tabs.StatsTab;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JavelitDashboard {

    @ConfigProperty(name = "javelit.port", defaultValue = "8888")
    int port;

    @Inject
    MembersTab membersTab;

    @Inject
    ProjectsTab projectsTab;

    @Inject
    OrganizationProjectsTab organizationProjectsTab;

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

            var tabs = Jt.tabs(List.of("🙋 Members", "🚀 Members Projects", "🏢 Zenika Open Source Projects",
                    "📊 Contributions", "📈 Stats")).use();

            membersTab.render(tabs.tab("🙋 Members"));
            projectsTab.render(tabs.tab("🚀 Members Projects"));
            organizationProjectsTab.render(tabs.tab("🏢 Zenika Open Source Projects"));
            contributionsTab.render(tabs.tab("📊 Contributions"));
            statsTab.render(tabs.tab("📈 Stats"));

        }, port).build().start();
    }
}
