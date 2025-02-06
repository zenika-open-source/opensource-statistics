package zenika.oss.stats.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.CustomStatsContributionsUserByMonth;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.mapper.ZenikaMemberMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class FirestoreServices {

    @Inject
    Firestore firestore;

    /**
     * Create one member in the databse.
     * @param zMember the member to create.
     */
    public void createMember(ZenikaMember zMember) {
        List<ApiFuture<WriteResult>> futures = new ArrayList<>();
        futures.add(firestore.collection("members").document(zMember.getId()).set(zMember));
    }

    public List<ZenikaMember> getAllMembers() throws DatabaseException {
        CollectionReference zmembers = firestore.collection("members");
        ApiFuture<QuerySnapshot> querySnapshot = zmembers.get();
        try {
            return querySnapshot.get().getDocuments().stream()
                    .map(ZenikaMemberMapper::mapFirestoreZenikaMemberToZenikaMember).toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Remove stats for a GitHub account for a specific year.
     * @param githubMember : GitHub login
     * @param year : year to delete
     */
    public void deleteStatsForAGitHubAccountForAYear(String githubMember, int year) throws DatabaseException {
        CollectionReference zStats = firestore.collection("stats");
        Query query = zStats.whereEqualTo("year", year);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        try {
            querySnapshot.get().getDocuments().forEach(documentSnapshot -> {
                documentSnapshot.getReference().delete();
            });
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Save stats for a GitHub account for a specific year.
     * @param githubMember : GitHub login
     * @param year : year to save
     * @param stats : stats to save
     */
    public void saveStatsForAGitHubAccountForAYear(String githubMember, int year, List<CustomStatsContributionsUserByMonth> stats) {
        CollectionReference zStats = firestore.collection("stats");
        List<ApiFuture<WriteResult>> futures = new ArrayList<>();
        stats.forEach(stat -> {
            DocumentReference docRef = firestore.collection("stats").document();
            stat.getClass().getDeclaredFields();
            futures.add(docRef.set(new CustomStatsContributionsUserByMonth(
                    stat.getMonth(),
                    stat.getMonthLabel(),
                    stat.getContributions()
            )));
        });
    }
}
