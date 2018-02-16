package com.conductor.app.core.service;import java.util.Collections;import java.util.HashSet;import java.util.List;import java.util.Map;import java.util.Set;import java.util.concurrent.ConcurrentHashMap;import java.util.stream.Collectors;import javax.annotation.PostConstruct;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;import com.conductor.app.core.model.Document;import com.conductor.app.core.service.tokenization.WordTokenizer;/** * this is very simple inverted index idea implementation */@Servicepublic class InvertedIndexServiceImpl implements IndexService {	private final Map<String, Set<Long>> tokenDocumentIdMapping = new ConcurrentHashMap<>(Short.MAX_VALUE);		@Autowired	WordTokenizer wordTokenizer;	@Override	public Set<Long> searchDocumentIds(String tokenString) {		Set<String> tokens = new HashSet<>(wordTokenizer.tokenize(tokenString));		return searchDocuments(tokens);	}	@Override	public void updateIndexes(Document document) {		Set<String> tokens = new HashSet<>(wordTokenizer.tokenize(document.getText()));		tokens.forEach(tkn -> tokenDocumentIdMapping.computeIfAbsent(tkn,			k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(document.getId()));	}		@Override	public void persist() {		throw new UnsupportedOperationException("Index structure persistence is not yet supported");	}		@Override	@PostConstruct	public void start() {		// here should be initialization code: cache start, read index info from db etc	}	Set<Long> searchDocuments(Set<String> tokens) {		//test if all search words can be found in existing documents		if (tokens.isEmpty() || !tokenDocumentIdMapping.keySet().containsAll(tokens)) {			return Collections.emptySet();		}		//run intersection on document ids set to grab only those doc containing all search tokens(words)		List<Set<Long>> anyTokenDocIds = tokens.stream().map(tokenDocumentIdMapping::get).collect(Collectors.toList());		return anyTokenDocIds.stream().skip(1).collect(() -> new HashSet<>(anyTokenDocIds.get(0)), Set::retainAll, Set::retainAll);	}}