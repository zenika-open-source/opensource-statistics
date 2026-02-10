package zenika.oss.stats.ui.tabs;

import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubProject;
import zenika.oss.stats.services.FirestoreServices;
import org.icepear.echarts.Pie;
import org.icepear.echarts.charts.pie.PieSeries;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class StatsTab {

    @Inject
    FirestoreServices firestoreServices;

    public void render(JtContainer statsTab) {
        try {
            List<ZenikaMember> members = firestoreServices.getAllMembers();

            Map<String, Long> cityStats = members.stream()
                    .collect(Collectors.groupingBy(m -> m.getCity() == null ? "Unknown" : m.getCity(),
                            Collectors.counting()));

            Object[] data = cityStats.entrySet().stream()
                    .map(e -> Map.of("name", e.getKey(), "value", e.getValue()))
                    .toArray();

            Pie pie = new Pie()
                    .setTooltip("item")
                    .setLegend();

            pie.addSeries(new PieSeries()
                    .setName("\uD83C\uDF0D Members by City")
                    .setRadius("50%")
                    .setData(data));

            Jt.subheader("Members by City").use(statsTab);
            if (!members.isEmpty()) {
                Jt.echarts(pie).use(statsTab);
            } else {
                Jt.text("no data available").use(statsTab);
            }

            // Top 3 Projects Section
            try {
                List<GitHubProject> allProjects = firestoreServices.getAllProjects();
                
                if (!allProjects.isEmpty()) {
                    Jt.subheader("\uD83C\uDFC6 Top 3 Projects by Stars").use(statsTab);
                    
                    record ProjectDisplay(String name, String fullName, String url, Long stars, Long forks) {}
                    
                    List<ProjectDisplay> topProjects = allProjects.stream()
                            .sorted((p1, p2) -> Long.compare(p2.getWatchers_count(), p1.getWatchers_count()))
                            .limit(3)
                            .map(p -> new ProjectDisplay(p.getName(), p.getFull_name(), p.getHtml_url(),
                                    p.getWatchers_count(), p.getForks()))
                            .collect(Collectors.toList());
                            
                    Jt.table(topProjects).use(statsTab);
                }
            } catch (Exception e) {
                Jt.warning("Could not load top projects: " + e.getMessage()).use(statsTab);
            }

        } catch (Exception e) {
            Jt.warning("Could not load stats: " + e.getMessage()).use(statsTab);
        }
    }
}
