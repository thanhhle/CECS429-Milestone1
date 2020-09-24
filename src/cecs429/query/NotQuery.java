package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other Query objects and merges their postings in an intersection-like operation.
 */
public class NotQuery implements Query {
	private Query mQuery;
	
	public NotQuery(Query query) {
		mQuery = new TermLiteral(query.toString().substring(1));
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		return mQuery.getPostings(index);
	}
	
	@Override
	public String toString() {
		return mQuery.toString();
	}
	
	@Override
	public boolean isNegative() {
		return true;
	}
}
