package net.finetunes.ftcldstr.operations.search;

import java.util.HashMap;

import org.w3c.dom.Element;

public class SearchService {
	
	// TODO: clarify the type of expression param
	// TODO: matches requires to be passes by reference
	public static void doBasicSearch(
			String expression, 
			String base, String href, 
			int depth, int limit, 
			// $matches, 
			HashMap visited) {
		
		
		// TODO: implement
		return;
		
	}
	
	public static String getSearchResult(String searchString,
			String filename, String ru,
			boolean isRecursive, int fullcount,
			HashMap visited) {
		
		// TODO: implement
		return null;
		
	}
	
	// TODO: define xmlref type and return type
	public static void buildExprFromBasicSearchWhereClause(
			String operator, /*xmlref, */ String superOperator) {
		
	}

}
