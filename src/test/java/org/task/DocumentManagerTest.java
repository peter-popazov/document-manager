package org.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        Map<String, DocumentManager.Document> documentMap = new HashMap<>();

        documentMap.put("1234", DocumentManager.Document.builder()
                .id("1234")
                .title("Spring")
                .content("Spring official documentation")
                .author(DocumentManager.Author.builder().id("1").name("Peter").build())
                .created(Instant.parse("2024-09-30T20:58:00Z"))
                .build());

        documentMap.put("2345", DocumentManager.Document.builder()
                .id("2345")
                .title("Java")
                .content("Java doc content")
                .author(DocumentManager.Author.builder().id("2").name("Dan").build())
                .created(Instant.parse("2024-10-31T10:58:00Z"))
                .build());

        documentMap.put("3456", DocumentManager.Document.builder()
                .id("3456")
                .title("Bug Fix")
                .content("Implementation does not work")
                .author(DocumentManager.Author.builder().id("3").name("Monika").build())
                .created(Instant.parse("2025-01-30T20:58:00Z"))
                .build());

        documentMap.put("4567", DocumentManager.Document.builder()
                .id("4567")
                .title("Jira")
                .content("Jira is a issue abd project tracking software")
                .author(DocumentManager.Author.builder().id("4").name("Amelia").build())
                .created(Instant.parse("2025-01-30T20:58:00Z"))
                .build());

        documentManager = new DocumentManager(documentMap);
    }

    @Test
    public void Should_ThrowException_WhenDocumentIsNull() {
        Assertions.assertThrows(DocumentManager.DocumentNotGivenException.class, () -> {
            documentManager.save(null);
        });
    }

    @Test
    public void Should_InsertDocument_WhenIdIsNull() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id(null)
                .title("Test document title")
                .content("Test document content")
                .author(DocumentManager.Author.builder().id("1").name("Josh").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        Assertions.assertNotNull(savedDocument);
        Assertions.assertNotNull(savedDocument.getId());
        Assertions.assertEquals(document.getTitle(), savedDocument.getTitle());
        Assertions.assertEquals(5, documentManager.getStorageSize());
    }

    @Test
    public void Should_InsertDocument_WhenIdIsEmpty() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id("")
                .title("Test document title")
                .content("Test document content")
                .author(DocumentManager.Author.builder().id("1").name("Josh").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        Assertions.assertNotNull(savedDocument);
        Assertions.assertNotNull(savedDocument.getId());
        Assertions.assertEquals(document.getTitle(), savedDocument.getTitle());
        Assertions.assertEquals(5, documentManager.getStorageSize());
    }


    @Test
    public void Should_UpdateDocument_WhenIdIsGiven() {
        String updatedTitle = "Updated Spring";

        DocumentManager.Document document = DocumentManager.Document.builder()
                .id("1234")
                .title(updatedTitle)
                .content("Spring doc content")
                .author(DocumentManager.Author.builder().id("1").name("Peter").build())
                .created(Instant.parse("2024-09-30T20:58:00Z"))
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        Assertions.assertNotNull(savedDocument);
        Assertions.assertEquals(document.getId(), savedDocument.getId());
        Assertions.assertEquals(updatedTitle, savedDocument.getTitle());
        Assertions.assertEquals(4, documentManager.getStorageSize());
    }

    @Test
    void Should_ReturnAllDocuments_WhenEmptyFiltersGiven() {
        DocumentManager.SearchRequest request = new DocumentManager.SearchRequest(
                null, null, null, null, null);
        List<DocumentManager.Document> result = documentManager.search(request);

        Assertions.assertEquals(4, result.size());
    }

    @Test
    void Should_ReturnThreeMatchingDocuments_whenTitlePrefixesAreGiven() {
        DocumentManager.SearchRequest request = new DocumentManager.SearchRequest(
                List.of("spr", "j"), null, null, null, null);
        List<DocumentManager.Document> documents = documentManager.search(request);

        Assertions.assertEquals(3, documents.size());
    }


    @Test
    void Should_ReturnTwoMatchingDocuments_whenContentsAreGiven() {
        DocumentManager.SearchRequest request = new DocumentManager.SearchRequest(
                null, List.of("implement", "documentat"), null, null, null);
        List<DocumentManager.Document> documents = documentManager.search(request);

        Assertions.assertEquals(2, documents.size());
    }


    @Test
    void Should_ReturnTwoMatchingDocument_whenAuthorIdsAreGiven() {
        DocumentManager.SearchRequest request = new DocumentManager.SearchRequest(
                null, null, List.of("1", "2"), null, null);
        List<DocumentManager.Document> documents = documentManager.search(request);

        Assertions.assertEquals(2, documents.size());
    }


    @Test
    void Should_ReturnTwoMatchingDocument_whenDatesAreGiven() {
        DocumentManager.SearchRequest request = new DocumentManager.SearchRequest(
                null, null, null, Instant.parse(
                "2024-09-30T20:00:00Z"), Instant.parse("2024-12-30T20:00:00Z")
        );
        List<DocumentManager.Document> result = documentManager.search(request);

        Assertions.assertEquals(2, result.size());
    }

    @Test
    void Should_ReturnOneMatchingDocument_whenAuthorIdsAndContentsAreGiven() {
        DocumentManager.SearchRequest request = new DocumentManager.SearchRequest(
                null, List.of("Spring"), List.of("1", "2"), null, null);
        List<DocumentManager.Document> documents = documentManager.search(request);

        Assertions.assertEquals(1, documents.size());
    }

    @Test
    public void findDocumentById_whenDocumentIdExists() {
        Optional<DocumentManager.Document> optionalDocument = documentManager.findById("1234");
        Assertions.assertTrue(optionalDocument.isPresent());
    }

    @Test
    public void findDocumentById_whenDocumentIdIsNotPresent() {
        Optional<DocumentManager.Document> optionalDocument = documentManager.findById("0000");
        Assertions.assertTrue(optionalDocument.isEmpty());
    }

}