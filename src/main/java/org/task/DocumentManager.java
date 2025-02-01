package org.task;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc.
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested - Tests are implemented using JUnit 5
 */
public class DocumentManager {

    private final Map<String, Document> documents;

    public DocumentManager(Map<String, Document> documents) {
        this.documents = documents.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            throw new DocumentNotGivenException("Document cannot be null");
        }

        Author author = Author.builder()
                .id(document.getAuthor().getId())
                .name(document.getAuthor().getName())
                .build();

        if (document.getId() == null || document.getId().isEmpty()) {
            String id = UUID.randomUUID().toString();
            Document doc = Document.builder()
                    .id(id)
                    .title(document.getTitle())
                    .content(document.getContent())
                    .author(author)
                    .created(document.getCreated())
                    .build();

            documents.put(id, doc);
            return doc;
        }

        Optional<Document> existingDocOptional = Optional.ofNullable(documents.get(document.getId()));
        existingDocOptional.orElseThrow(() -> new DocumentNotFoundException("Document with id " + document.getId() + " not found"));

        Document updatedDoc = Document.builder()
                .id(existingDocOptional.get().getId())
                .title(document.getTitle())
                .content(document.getContent())
                .author(document.getAuthor())
                .created(document.getCreated())
                .build();

        documents.put(updatedDoc.getId(), updatedDoc);
        return updatedDoc;
    }

    /**
     * Implementation this method should find documents which match with request
     * <p>
     * Implementation provides conjunctive filtering i.e. documents must match all filters.
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        List<DocumentFilter> filters = List.of(
                new TitlePrefixFilter(request.getTitlePrefixes()),
                new ContentFilter(request.getContainsContents()),
                new AuthorFilter(request.getAuthorIds()),
                new DateFilter(request.getCreatedFrom(), request.getCreatedTo())
        );

        List<Document> filteredDocs = new ArrayList<>(documents.values());
        for (DocumentFilter filter : filters) {
            filteredDocs = filter.matches(filteredDocs);
        }

        return filteredDocs;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    /**
     * Returns current document storage capacity
     *
     * @return document storage capacity
     */
    public int getStorageSize() {
        return documents.size();
    }

    /**
     * Represents a filter strategy for filtering documents.
     * Concrete classes must provide logic to filter documents.
     */
    private interface DocumentFilter {

        /**
         * Filters a list of documents based on specific criteria defined by the concrete implementation.
         *
         * @param documents the list of documents to be filtered
         * @return a list of documents that match the filtering criteria
         */
        List<Document> matches(List<Document> documents);

        /**
         * Checks if a filtering criteria list is null or empty.
         *
         * @param list the list to be checked
         * @return true if the list is null or empty, false otherwise
         */
        default boolean isEmptyFilterCriteria(List<?> list) {
            return list == null || list.isEmpty();
        }
    }

    @Data
    private static class TitlePrefixFilter implements DocumentFilter {
        private final List<String> prefixes;

        /**
         * Filters a list of documents based on list of titles case-insensitively.
         *
         * @param documents the list of documents to be filtered
         * @return a list of documents that contain provided titles
         */
        @Override
        public List<Document> matches(List<Document> documents) {
            if (isEmptyFilterCriteria(prefixes)) {
                return documents;
            }

            List<String> lowerCaseTitles = prefixes.stream()
                    .map(String::toLowerCase)
                    .toList();

            return documents.stream().
                    filter(doc -> doc.getTitle() != null &&
                            lowerCaseTitles.stream().anyMatch(pref ->
                                    doc.getTitle().toLowerCase().startsWith(pref)
                            )
                    ).toList();
        }
    }

    @Data
    private static class ContentFilter implements DocumentFilter {

        private final List<String> contents;

        /**
         * Filters a list of documents based on list of contents case-insensitively.
         *
         * @param documents the list of documents to be filtered
         * @return a list of documents that contain provided content
         */
        @Override
        public List<Document> matches(List<Document> documents) {
            if (isEmptyFilterCriteria(contents)) {
                return documents;
            }

            List<String> lowerCaseContents = contents.stream()
                    .map(String::toLowerCase)
                    .toList();

            return documents.stream()
                    .filter(doc -> doc.getContent() != null &&
                            lowerCaseContents.stream().anyMatch(content ->
                                    doc.getContent().toLowerCase().contains(content)
                            )
                    ).toList();
        }
    }

    @Data
    private static class AuthorFilter implements DocumentFilter {
        private final List<String> authorIds;

        /**
         * Filters a list of documents based on list of authors.
         *
         * @param documents the list of documents to be filtered
         * @return a list of documents that are authored by provided author
         */
        @Override
        public List<Document> matches(List<Document> documents) {
            if (isEmptyFilterCriteria(authorIds)) {
                return documents;
            }

            return documents.stream().filter(doc -> doc.getAuthor().getId() != null &&
                    authorIds.stream().anyMatch(id -> doc.getAuthor().getId().equals(id))
            ).toList();
        }
    }

    @Data
    private static class DateFilter implements DocumentFilter {
        private final Instant from;
        private final Instant to;

        /**
         * Filters a list of documents based on dare range.
         *
         * @param documents the list of documents to be filtered
         * @return a list of documents that are created within provided date range
         */
        @Override
        public List<Document> matches(List<Document> documents) {
            if (from == null || to == null) {
                return documents;
            }

            return documents.stream()
                    .filter(doc -> {
                        if (doc.getCreated() == null) return false;
                        return doc.getCreated().isAfter(from) && doc.getCreated().isBefore(to);
                    }).toList();
        }
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }

    static class DocumentNotGivenException extends IllegalArgumentException {
        public DocumentNotGivenException(String s) {
            super(s);
        }
    }

    static class DocumentNotFoundException extends RuntimeException {
        public DocumentNotFoundException(String s) {
            super(s);
        }
    }
}


