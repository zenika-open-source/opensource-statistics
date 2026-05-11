package fr.zenika.opensource.stats.ui;

import io.javelit.core.Jt;
import io.javelit.core.Server;
import io.quarkus.runtime.StartupEvent;
import fr.zenika.opensource.stats.ui.tabs.ContributionsTab;
import fr.zenika.opensource.stats.ui.tabs.MembersTab;
import fr.zenika.opensource.stats.ui.tabs.OrganizationProjectsTab;
import fr.zenika.opensource.stats.ui.tabs.ProjectsTab;
import fr.zenika.opensource.stats.ui.tabs.StatsTab;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JavelitDashboard {

    @ConfigProperty(name = "javelit.port", defaultValue = "8888")
    int port;

    @ConfigProperty(name = "organization.display-name")
    String organizationDisplayName;

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
            Jt.header("📊 " + organizationDisplayName + " Opensource Statistics Dashboard").use();
            Jt.subheader("Welcome to the " + organizationDisplayName + " Open Source contributions dashboard").use();
            Jt.markdown(
                    "This dashboard get publics datas from GitHub")
                    .use();

            var tabs = Jt.tabs(List.of("🙋 Members", "🚀 Members Projects",
                    "🏢 " + organizationDisplayName + " Open Source Projects",
                    "📊 Contributions", "📈 Stats")).use();

            membersTab.render(tabs.tab("🙋 Members"));
            projectsTab.render(tabs.tab("🚀 Members Projects"));
            organizationProjectsTab.render(tabs.tab("🏢 " + organizationDisplayName + " Open Source Projects"));
            contributionsTab.render(tabs.tab("📊 Contributions"));
            statsTab.render(tabs.tab("📈 Stats"));

        }, port).build().start();
    }
}
