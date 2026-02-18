package zenika.oss.stats.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import zenika.oss.stats.beans.Project;
import zenika.oss.stats.beans.ZenikaMember;
import zenika.oss.stats.beans.gcp.StatsContribution;
import zenika.oss.stats.config.FirestoreCollections;
import zenika.oss.stats.exception.DatabaseException;
import zenika.oss.stats.mapper.ZenikaMemberMapper;
import zenika.oss.stats.mapper.ZenikaProjectMapper;

import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class FirestoreServices {

    @Inject
    Firestore firestore;

    /**
     * Create one member in the databse.
     *
     * @param zMember the member to create.
     */
    @CacheInvalidateAll(cacheName = "members-cache")
    public void createMember(ZenikaMember zMember) throws DatabaseException {
        createDocument(zMember, FirestoreCollections.MEMBERS.value, zMember.getId());
    }

    @CacheResult(cacheName = "members-cache")
    public List<ZenikaMember> getAllMembers() throws DatabaseException {
        CollectionReference zmembers = firestore.collection(FirestoreCollections.MEMBERS.value);
        ApiFuture<QuerySnapshot> querySnapshot = zmembers.get();
        try {
            return querySnapshot.get().getDocuments().stream()
                    .map(ZenikaMemberMapper::mapFirestoreZenikaMemberToZenikaMember)
                    .collect(Collectors.toList());
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
    @CacheInvalidateAll(cacheName = "contributions-cache")
    public void deleteStatsForAGitHubAccountForAYear(String githubMember, int year) throws DatabaseException {
        CollectionReference zStats = firestore.collection(FirestoreCollections.STATS.value);
        Query query = zStats.whereEqualTo("githubHandle", githubMember).whereEqualTo("year", String.valueOf(year));
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        try {
            List<QueryDocumentSnapshot> stats = querySnapshot.get().getDocuments();
            if (stats.isEmpty()) {
                return;
            }
            // Use a WriteBatch for atomic and efficient bulk deletes
            WriteBatch batch = firestore.batch();
            for (QueryDocumentSnapshot document : stats) {
                batch.delete(document.getReference());
            }
            batch.commit().get(); // Wait for the batch to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Save stats for a GitHub account for a specific month of a year.
     *
     * @param statsContribution : stats to save
     */
    @CacheInvalidateAll(cacheName = "contributions-cache")
    public void saveStatsForAGitHubAccountForAYear(StatsContribution statsContribution) throws DatabaseException {
        try {
            // Use deterministic ID to prevent duplicates (Upsert behavior)
            String documentId = String.format("%s-%s-%s",
                    statsContribution.getYear(),
                    statsContribution.getMonth(),
                    statsContribution.getGithubHandle());

            // .get() waits for the operation to complete, making it synchronous and
            // reliable
            firestore.collection(FirestoreCollections.STATS.value)
                    .document(documentId)
                    .set(statsContribution)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Delete all stats for the year in parameter.
     *
     * @param year : the year that we want to remove stats
     * @throws DatabaseException exception
     */
    @CacheInvalidateAll(cacheName = "contributions-cache")
    public void deleteStatsForAllGitHubAccountForAYear(int year) throws DatabaseException {
        CollectionReference zStats = firestore.collection(FirestoreCollections.STATS.value);
        Query query = zStats.whereEqualTo("year", String.valueOf(year));
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        try {
            List<QueryDocumentSnapshot> stats = querySnapshot.get().getDocuments();
            if (stats.isEmpty()) {
                return;
            }

            // Handle deletion in batches of 500 (Firestore limit)
            final int BATCH_SIZE = 500;
            for (int i = 0; i < stats.size(); i += BATCH_SIZE) {
                WriteBatch batch = firestore.batch();
                List<QueryDocumentSnapshot> batchFiles = stats.subList(i, Math.min(i + BATCH_SIZE, stats.size()));

                for (QueryDocumentSnapshot document : batchFiles) {
                    batch.delete(document.getReference());
                }
                batch.commit().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Create a project in the Firestore database.
     *
     * @param project the project to create.
     */
    @CacheInvalidateAll(cacheName = "projects-cache")
    public void createProject(Project project) throws DatabaseException {
        createDocument(project, FirestoreCollections.PROJECTS.value, project.getId());
    }

    /**
     * Retrieve all projects from the Firestore database.
     *
     * @return a list of all projects.
     */
    @CacheResult(cacheName = "projects-cache")
    public List<Project> getAllProjects() throws DatabaseException {
        CollectionReference zProjects = firestore.collection(FirestoreCollections.PROJECTS.value);
        ApiFuture<QuerySnapshot> querySnapshot = zProjects.get();
        try {
            return querySnapshot.get().getDocuments().stream()
                    .map(ZenikaProjectMapper::mapFirestoreZenikaProjectToProject)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException exception) {
            throw new DatabaseException(exception);
        }
    }

    /**
     * Remove all projects from the Firestore database.
     *
     * @throws DatabaseException exception
     */
    @CacheInvalidateAll(cacheName = "projects-cache")
    public void deleteAllProjects() throws DatabaseException {
        deleteAllDocuments(FirestoreCollections.PROJECTS);
    }

    /**
     * Remove all members
     *
     * @throws DatabaseException exception
     */
    @CacheInvalidateAll(cacheName = "members-cache")
    public void deleteAllMembers() throws DatabaseException {
        deleteAllDocuments(FirestoreCollections.MEMBERS);
    }

    /**
     * Delete a single member by id.
     *
     * @param memberId the id of the member document to delete.
     * @throws DatabaseException exception
     */
    @CacheInvalidateAll(cacheName = "members-cache")
    public void deleteMember(String memberId) throws DatabaseException {
        try {
            firestore.collection(FirestoreCollections.MEMBERS.value)
                    .document(memberId)
                    .delete()
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Create a document in the Firestore database.
     *
     * @param document       the document to create.
     * @param collectionPath the path of the collection in which to create the
     *                       document.
     * @param documentId     the id of the document to create.
     * @param <T>            the type of the document to create.
     */
    public <T> void createDocument(T document, String collectionPath, String documentId) throws DatabaseException {
        try {
            firestore.collection(collectionPath).document(documentId).set(document).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Delete all documents in a specific Firestore collection.
     *
     * @param collectionType the type of collection to delete.
     * @param <T>            the type of document
     * @throws DatabaseException exception
     */
    public <T> void deleteAllDocuments(FirestoreCollections collectionType) throws DatabaseException {
        CollectionReference collection = firestore.collection(collectionType.value);
        ApiFuture<QuerySnapshot> querySnapshot = collection.get();
        try {
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
            if (documents.isEmpty()) {
                return;
            }
            WriteBatch batch = firestore.batch();
            for (QueryDocumentSnapshot document : documents) {
                batch.delete(document.getReference());
            }
            batch.commit().get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new DatabaseException(exception);
        }
    }

    @CacheResult(cacheName = "contributions-cache")
    public List<StatsContribution> getStatsForYear(int year) throws DatabaseException {
        CollectionReference zStats = firestore.collection(FirestoreCollections.STATS.value);
        Query query = zStats.whereEqualTo("year", String.valueOf(year));
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        try {
            return querySnapshot.get().getDocuments().stream()
                    .map(document -> document.toObject(StatsContribution.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException exception) {
            throw new DatabaseException(exception);
        }
    }

    @CacheResult(cacheName = "contributions-cache")
    public List<StatsContribution> getContributionsForAMemberOrderByYear(String memberId) throws DatabaseException {
        List<StatsContribution> stats = null;
        CollectionReference zStats = firestore.collection(FirestoreCollections.STATS.value);
        Query query = zStats.whereEqualTo("githubHandle", memberId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        try {
            stats = querySnapshot.get().getDocuments().stream()
                    .map(document -> document.toObject(StatsContribution.class))
                    .sorted(Comparator.comparing(StatsContribution::getYear).reversed()
                            .thenComparing(s -> Month.valueOf(s.getMonth().toUpperCase()).getValue(),
                                    Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException exception) {
            throw new DatabaseException(exception);
        }

        return stats;
    }

    public List<StatsContribution> getContributionsForAYearAndMonthOrderByMonth(int year, String month)
            throws DatabaseException {
        List<StatsContribution> stats = null;
        CollectionReference zStats = firestore.collection(FirestoreCollections.STATS.value);
        Query query = zStats.whereEqualTo("year", String.valueOf(year)).whereEqualTo("month", month);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        try {
            stats = querySnapshot.get().getDocuments().stream()
                    .map(document -> document.toObject(StatsContribution.class))
                    .sorted((s1, s2) -> s2.getMonth().compareTo(s1.getMonth()))
                    .collect(java.util.stream.Collectors.toList());
        } catch (InterruptedException | ExecutionException exception) {
            throw new DatabaseException(exception);
        }

        return stats;
    }

    @CacheResult(cacheName = "contributions-cache")
    public List<StatsContribution> getAllStats() throws DatabaseException {
        CollectionReference zStats = firestore.collection(FirestoreCollections.STATS.value);
        ApiFuture<QuerySnapshot> querySnapshot = zStats.get();
        try {
            return querySnapshot.get().getDocuments().stream()
                    .map(document -> document.toObject(StatsContribution.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException exception) {
            throw new DatabaseException(exception);
        }
    }
}
