package zenika.oss.stats.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.github.GitHubProject;
import zenika.oss.stats.beans.gcp.StatsContribution;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.mapper.ZenikaMemberMapper;
import zenika.oss.stats.mapper.ZenikaProjectMapper;

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
        } catch (InterruptedException | ExecutionException exception) {
            throw new DatabaseException(exception);
        }
    }

    /**
     * Remove stats for a GitHub account for a specific year.
     *
     * @param githubMember : GitHub login
     * @param year         : year to delete
     * @throws DatabaseException exception
     */
    public void deleteStatsForAGitHubAccountForAYear(String githubMember, int year) throws DatabaseException {
        CollectionReference zStats = firestore.collection("stats");
        Query query = zStats.whereEqualTo("githubHandle", githubMember).whereEqualTo("year", String.valueOf(year));
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        try {
            List<QueryDocumentSnapshot> stats = querySnapshot.get().getDocuments();
            for (QueryDocumentSnapshot document : stats) {
                document.getReference().delete();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Save stats for a GitHub account for a specific month of a year.
     *
     * @param statsContribution : stats to save
     */
    public void saveStatsForAGitHubAccountForAYear(StatsContribution statsContribution) throws DatabaseException {
        List<ApiFuture<WriteResult>> futures = new ArrayList<>();
        futures.add(firestore.collection("stats").document().set(statsContribution));
    }

    /**
     * Delete all stats for the year in parameter.
     * @param year : the year that we want to remove stats
     * @throws DatabaseException exception
     */
    public void deleteStatsForAllGitHubAccountForAYear(int year) throws DatabaseException {
        CollectionReference zStats = firestore.collection("stats");
        Query query = zStats.whereEqualTo("year", String.valueOf(year));
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        try {
            List<QueryDocumentSnapshot> stats = querySnapshot.get().getDocuments();
            for (QueryDocumentSnapshot document : stats) {
                document.getReference().delete();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Remove all members
     *
     * @throws DatabaseException exception
     */
    public void deleteAllMembers() throws DatabaseException {
        CollectionReference zStats = firestore.collection("members");
        ApiFuture<QuerySnapshot> querySnapshot = zStats.get();
        try {
            List<QueryDocumentSnapshot> stats = querySnapshot.get().getDocuments();
            for (QueryDocumentSnapshot document : stats) {
                document.getReference().delete();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Create a project in the Firestore database.
     * @param project the project to create.
     */
    public void createProject(GitHubProject project) {
        List<ApiFuture<WriteResult>> futures = new ArrayList<>();
        futures.add(firestore.collection("projects").document(project.getId()).set(project));
    }

    /**
     * Retrieve all projects from the Firestore database.
     * @return a list of all projects.
     */
    public List<GitHubProject> getAllProjects() throws DatabaseException {
        CollectionReference zProjects = firestore.collection("projects");
        ApiFuture<QuerySnapshot> querySnapshot = zProjects.get();
        try {
            return querySnapshot.get().getDocuments().stream().map(ZenikaProjectMapper::mapFirestoreZenikaProjectToGitHubProject).toList();
        } catch (InterruptedException | ExecutionException exception) {
            throw new DatabaseException(exception);
        }
    }

    /**
     * Remove all projects from the Firestore database.
     * @throws DatabaseException exception
     */
    public void deleteAllProjects() throws DatabaseException {
        CollectionReference zStats = firestore.collection("projects");
        ApiFuture<QuerySnapshot> querySnapshot = zStats.get();
        try {
            List<QueryDocumentSnapshot> stats = querySnapshot.get().getDocuments();
            for (QueryDocumentSnapshot document : stats) {
                document.getReference().delete();
            }
        } catch (InterruptedException | ExecutionException exception) {
            throw new DatabaseException(exception);
        }
    }
}
