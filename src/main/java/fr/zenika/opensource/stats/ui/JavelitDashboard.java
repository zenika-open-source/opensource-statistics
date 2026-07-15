package fr.zenika.opensource.stats.ui;

import io.javelit.core.Jt;
import io.javelit.core.Server;
import io.quarkus.runtime.StartupEvent;
import fr.zenika.opensource.stats.services.FirestoreServices;
import fr.zenika.opensource.stats.ressources.workflow.WorkflowRessources;
import io.quarkus.logging.Log;
import java.util.concurrent.CompletableFuture;

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
    FirestoreServices firestoreServices;

    @Inject
    WorkflowRessources workflowRessources;


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
            try {
                String lastSyncText = "";
                try {
                    String lastSync = firestoreServices.getLastExecutionDate();
                    if (lastSync != null) {
                        lastSyncText = "Last sync: " + lastSync;
                    }

                    // Check if sync needs to run this week (Automatic startup refresh for Cloud Run scale-to-zero)
                    boolean needSync = false;
                    if (lastSync == null || lastSync.isEmpty()) {
                        needSync = true;
                    } else {
                        try {
                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                            java.time.LocalDate lastRunDate = java.time.LocalDateTime.parse(lastSync, formatter).toLocalDate();
                            java.time.LocalDate startOfWeek = java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                            if (lastRunDate.isBefore(startOfWeek)) {
                                needSync = true;
                            }
                        } catch (Exception parseEx) {
                            needSync = true;
                        }
                    }

                    if (needSync) {
                        Log.info("🔄 Last sync (" + lastSync + ") is before start of this week. Triggering automatic background synchronization...");
                        CompletableFuture.runAsync(() -> {
                            try {
                                workflowRessources.syncData();
                            } catch (Exception e) {
                                Log.error("❌ Failed to run automatic background sync on startup", e);
                            }
                        });
                    }
                } catch (Exception e) {
                    // Ignore UI sync loading errors to avoid crashing dashboard
                }

                var headerCols = Jt.columns(2).key("header_cols").use();
                Jt.header("📊 " + organizationDisplayName + " Dashboard").use(headerCols.col(0));
                if (!lastSyncText.isEmpty()) {
                    Jt.markdown("<div style='text-align: right; padding-top: 25px; color: gray; font-size: 0.9em;'>" + lastSyncText + "</div>")
                            .use(headerCols.col(1));
                }


                var tabs = Jt.tabs(List.of("🙋 Members", "🚀 Members Projects",
                        "🏢 Projects",
                        "📊 Contributions", "📈 Stats")).use();

                try {
                    membersTab.render(tabs.tab("🙋 Members"));
                } catch (Exception e) {
                    Jt.error("Error loading members: " + e.getMessage()).use(tabs.tab("🙋 Members"));
                    e.printStackTrace();
                }

                try {
                    projectsTab.render(tabs.tab("🚀 Members Projects"));
                } catch (Exception e) {
                    Jt.error("Error loading projects: " + e.getMessage()).use(tabs.tab("🚀 Members Projects"));
                    e.printStackTrace();
                }

                try {
                    organizationProjectsTab.render(tabs.tab("🏢 Projects"));
                } catch (Exception e) {
                    Jt.error("Error loading organization projects: " + e.getMessage()).use(tabs.tab("🏢 Projects"));
                    e.printStackTrace();
                }

                try {
                    contributionsTab.render(tabs.tab("📊 Contributions"));
                } catch (Exception e) {
                    Jt.error("Error loading contributions: " + e.getMessage()).use(tabs.tab("📊 Contributions"));
                    e.printStackTrace();
                }

                try {
                    statsTab.render(tabs.tab("📈 Stats"));
                } catch (Exception e) {
                    Jt.error("Error loading stats: " + e.getMessage()).use(tabs.tab("📈 Stats"));
                    e.printStackTrace();
                }

            } catch (Exception e) {
                Jt.error("Critical Dashboard Error: " + e.getMessage()).use();
                e.printStackTrace();
            }
        }, port).build().start();
    }
}
