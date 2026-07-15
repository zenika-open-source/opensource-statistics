package fr.zenika.opensource.stats.schedules;

import fr.zenika.opensource.stats.ressources.workflow.WorkflowRessources;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.logging.Log;

@ApplicationScoped
public class DataSyncSchedule {

    @Inject
    WorkflowRessources workflowRessources;

    @Scheduled(cron = "${datasync.cron}", timeZone = "${datasync.timezone}")
    @RunOnVirtualThread
    public void syncData() {
        Log.info("🔄 Triggering scheduled data synchronization...");
        try {
            workflowRessources.syncData();
        } catch (Exception e) {
            Log.error("❌ Error running scheduled data sync", e);
        }
    }
}
