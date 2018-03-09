package com.conductor.app.core.service;import static org.mockito.Matchers.any;import static org.mockito.Matchers.anySet;import static org.mockito.Mockito.times;import static org.mockito.Mockito.verify;import static org.mockito.Mockito.verifyNoMoreInteractions;import static org.testng.Assert.assertEquals;import static org.testng.Assert.assertTrue;import java.util.ArrayList;import java.util.Collections;import java.util.List;import java.util.Set;import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;import java.util.concurrent.Future;import java.util.concurrent.TimeUnit;import java.util.concurrent.TimeoutException;import java.util.stream.Collectors;import java.util.stream.Stream;import org.apache.tomcat.jni.Time;import org.mockito.InjectMocks;import org.mockito.MockitoAnnotations;import org.mockito.Spy;import org.testng.annotations.BeforeMethod;import org.testng.annotations.Test;import com.conductor.app.core.model.Document;import com.conductor.app.core.service.tokenization.SimpleASCIITokenizer;public class InvertedIndexServiceImplTest {	@Spy	@InjectMocks	InvertedIndexServiceImpl indexService;	@Spy	SimpleASCIITokenizer wordTokenizer;	@BeforeMethod	private void initMocks() {		MockitoAnnotations.initMocks(this);	}	@Test	public void testSearchDocumentIdsEmptyTokenString() {		//Given		final String tokens = "";		//When		Set<Long> resultSet = indexService.searchDocumentIds(tokens);		//Then		assertEquals(resultSet, Collections.emptySet());		verify(wordTokenizer, times(1)).tokenize(tokens);		verify(indexService, times(1)).searchDocumentIds(tokens);		verify(indexService, times(1)).searchDocuments(Collections.emptySet());		verifyNoMoreInteractions(indexService);	}	@Test	public void testSearchDocumentIdsWhiteSpaceTokenString() {		//Given		final String tokens = "   ";		//When		Set<Long> resultSet = indexService.searchDocumentIds(tokens);		//Then		assertEquals(resultSet, Collections.emptySet());		verify(wordTokenizer, times(1)).tokenize(tokens);		verify(indexService, times(1)).searchDocumentIds(tokens);		verify(indexService, times(1)).searchDocuments(Collections.emptySet());		verifyNoMoreInteractions(indexService);	}	@Test	public void testSearchDocumentIdsSingleResults() {		//Given		final Long doc1ID = 1L;		final Long doc2ID = 2L;		final String text1 = "The impact of Java SE 9 on operations and development teams";		final String text2 = "Modular Development with Java SE 9";		Document document1 = new Document(doc1ID, text1);		Document document2 = new Document(doc2ID, text2);		indexService.updateIndexes(document1);		indexService.updateIndexes(document2);		Set<Long> expectedSet = Stream.of(doc1ID).collect(Collectors.toSet());		final String tokens = "impact of Java SE 9";		//When		Set<Long> resultSet = indexService.searchDocumentIds(tokens);		//Then		assertEquals(resultSet, expectedSet);		verify(indexService, times(1)).searchDocumentIds(tokens);		verify(indexService, times(1)).searchDocuments(anySet());		verify(indexService, times(2)).updateIndexes(any());		verifyNoMoreInteractions(indexService);	}	@Test	public void testSearchDocumentIdsMultipleResults() {		//Given		final Long doc1ID = 1L;		final Long doc2ID = 2L;		final String text1 = "The impact of Java SE 9 on operations and development teams";		final String text2 = "Modular Development with Java SE 9";		Document document1 = new Document(doc1ID, text1);		Document document2 = new Document(doc2ID, text2);		indexService.updateIndexes(document1);		indexService.updateIndexes(document2);		Set<Long> expectedSet = Stream.of(doc1ID, doc2ID).collect(Collectors.toSet());		final String tokens = "Java SE 9";		//When		Set<Long> resultSet = indexService.searchDocumentIds(tokens);		//Then		assertEquals(resultSet, expectedSet);		verify(indexService, times(1)).searchDocumentIds(tokens);		verify(indexService, times(1)).searchDocuments(anySet());		verify(indexService, times(2)).updateIndexes(any());		verifyNoMoreInteractions(indexService);	}	@Test	public void testSearchDocumentIdsMultipleThreadsSameResult() throws Exception {		//Given		final Long doc1ID = 1L;		final Long doc2ID = 2L;		final String text1 = "The impact of Java SE 9 on operations and development teams";		final String text2 = "Modular Development with Java SE 9";		final int numSimultaneousRequests = 5;		final int numTries = 100;		Document document1 = new Document(doc1ID, text1);		Document document2 = new Document(doc2ID, text2);		indexService.updateIndexes(document1);		indexService.updateIndexes(document2);		final String tokens1 = "impact of Java SE 9";		final String tokens2 = "with Java SE 9";		final String tokens12 = "Java SE 9";		Set<Long> expectedSet1 = Stream.of(doc1ID).collect(Collectors.toSet());		Set<Long> expectedSet2 = Stream.of(doc2ID).collect(Collectors.toSet());		Set<Long> expectedSet12 = Stream.of(doc1ID, doc2ID).collect(Collectors.toSet());		//when		ExecutorService executorService = Executors.newFixedThreadPool(numSimultaneousRequests);		try {			List<Future<Set<Long>>> responses = new ArrayList<>();			for (int i = 0; i < numTries; i++) {				responses.add(executorService.submit(() -> indexService.searchDocumentIds(tokens1)));				responses.add(executorService.submit(() -> indexService.searchDocumentIds(tokens2)));				responses.add(executorService.submit(() -> indexService.searchDocumentIds(tokens12)));			}		//then			for (int i = 0; i < responses.size(); ) {				assertEquals(responses.get(i++).get(1l, TimeUnit.SECONDS), expectedSet1);				assertEquals(responses.get(i++).get(1l, TimeUnit.SECONDS), expectedSet2);				assertEquals(responses.get(i++).get(1l, TimeUnit.SECONDS), expectedSet12);			}		} catch (InterruptedException ie) {			assertTrue(false, "InterruptedException was thrown");		} catch (TimeoutException tmoe) {			assertTrue(false, "Running too long... timeouted");		} finally {			executorService.shutdownNow();		}	}	@Test	public void testUpdateIndexes() {		//Given		final Long doc1ID = 1L;		final String text1 = "Modular Development with Java SE 9";		final Document document1 = new Document(doc1ID, text1);		//when		indexService.updateIndexes(document1);		//then		verify(indexService, times(1)).updateIndexes(document1);		verify(wordTokenizer, times(1)).tokenize(text1);		verifyNoMoreInteractions(indexService);	}}