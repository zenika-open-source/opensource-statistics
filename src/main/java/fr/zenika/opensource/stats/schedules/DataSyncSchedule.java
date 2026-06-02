package fr.zenika.opensource.stats.schedules;

import fr.zenika.opensource.stats.ressources.workflow.WorkflowRessources;
import fr.zenika.opensource.stats.services.FirestoreServices;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import io.quarkus.logging.Log;

@ApplicationScoped
public class DataSyncSchedule {

    @Inject
    WorkflowRessources workflowRessources;

    @Inject
    FirestoreServices firestoreServices;

    @Scheduled(cron = "{datasync.cron}", timeZone = "{datasync.timezone}")
    @RunOnVirtualThread
    public void syncData() {
        Log.info("🔄 Starting scheduled data synchronization...");
        try {
            // 1. Sync members first (GitHub organization members)
            Log.info("👥 Syncing organization members...");
            workflowRessources.saveMembers();
            Log.info("✅ Organization members synced successfully.");

            // 2. Run the two main actions in parallel using Virtual Threads:
            //    - Action A: Retrieval of member projects
            //    - Action B: Contributions of members (current year and past years)
            try (var virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
                CompletableFuture<Void> projectsFuture = CompletableFuture.runAsync(() -> {
                    Log.info("📂 Syncing members projects in parallel...");
                    try {
                        workflowRessources.savePersonalProjects();
                        Log.info("✅ Members projects synced successfully.");
                    } catch (Throwable e) {
                        Log.error("❌ Error syncing members projects in scheduled task: " + e.getMessage(), e);
                        if (e instanceof java.lang.reflect.UndeclaredThrowableException) {
                            Throwable cause = e.getCause();
                            Log.error("❌ UndeclaredThrowableException ROOT CAUSE: " + (cause != null ? cause.getMessage() : "null"), cause);
                        }
                        throw new RuntimeException(e);
                    }
                }, virtualExecutor);

                CompletableFuture<Void> contributionsFuture = CompletableFuture.runAsync(() -> {
                    Log.info("📊 Syncing member contributions in parallel...");
                    try {
                        int currentYear = java.time.Year.now().getValue();
                        
                        // Sync current year
                        Log.info("📅 Syncing contributions for current year: " + currentYear);
                        workflowRessources.saveStatsForYear(currentYear);
                        
                        // Sync past 5 years
                        for (int i = 1; i <= 5; i++) {
                            int pastYear = currentYear - i;
                            Log.info("📅 Syncing contributions for past year: " + pastYear);
                            workflowRessources.saveStatsForYear(pastYear);
                        }
                        Log.info("✅ Member contributions synced successfully.");
                    } catch (Exception e) {
                        Log.error("❌ Error syncing member contributions in scheduled task", e);
                        throw new RuntimeException(e);
                    }
                }, virtualExecutor);

                CompletableFuture<Void> orgProjectsFuture = CompletableFuture.runAsync(() -> {
                    Log.info("🏢 Syncing organization projects in parallel...");
                    try {
                        workflowRessources.saveOrganizationProjects();
                        Log.info("✅ Organization projects synced successfully.");
                    } catch (Exception e) {
                        Log.error("❌ Error syncing organization projects in scheduled task", e);
                        throw new RuntimeException(e);
                    }
                }, virtualExecutor);

                // Wait for all parallel tasks to complete (blocking here blocks the virtual thread, not the OS thread!)
                CompletableFuture.allOf(projectsFuture, contributionsFuture, orgProjectsFuture).join();
            }

            // 3. Save the execution date in the "params" collection
            String executionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            firestoreServices.saveLastExecutionDate(executionDate);
            Log.info("💾 Data synchronization completed successfully at " + executionDate);

        } catch (Exception e) {
            Log.error("❌ Error during scheduled data synchronization", e);
        }
    }
}
