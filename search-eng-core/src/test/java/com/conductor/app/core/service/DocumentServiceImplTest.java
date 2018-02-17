package com.conductor.app.core.service;import static org.mockito.Mockito.only;import static org.mockito.Mockito.times;import static org.mockito.Mockito.verify;import static org.mockito.Mockito.when;import static org.testng.Assert.assertNotNull;import static org.testng.Assert.assertTrue;import org.mockito.InjectMocks;import org.mockito.Mock;import org.mockito.MockitoAnnotations;import org.mockito.Spy;import org.testng.annotations.BeforeMethod;import org.testng.annotations.Test;import com.conductor.app.core.ResourceNotFoundException;import com.conductor.app.core.model.Document;import com.conductor.app.core.repository.DocumentRepository;public class DocumentServiceImplTest {	@Spy	@InjectMocks	DocumentServiceImpl documentService;	@Mock	DocumentRepository repository;	@Mock	IndexService indexService;	@BeforeMethod	private void initMocks() {		MockitoAnnotations.initMocks(this);	}	@Test	public void testGet(){		//Given		final Long docId = 1L;		final String docText = "TEXT";		when(repository.get(docId)).thenReturn(new Document(docId, docText));		//When		Document resultDoc = documentService.get(docId);		//Then		assertNotNull(resultDoc);		assertTrue(resultDoc.getId() == docId);		verify(documentService, only()).get(docId);		verify(repository,times(1)).get(docId);	}	@Test(expectedExceptions = ResourceNotFoundException.class)	public void testGetThrowsException(){		//Given		final Long docId = 1L;		when(repository.get(docId)).thenReturn(null);		//When		documentService.get(docId);		//Then		verify(repository,times(1)).get(docId);	}	@Test()	public void testAdd() {		//Given		Document document = new Document(1L, "TEXT");		//When		Long documentId = documentService.add(document);		//Then		assertTrue(documentId == 1L);		verify(repository, times(1)).add(document);		verify(indexService, times(1)).updateIndexes(document);	}}