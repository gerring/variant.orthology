package org.jax.gweaver.variant.orthology;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.Transaction.Type;

/**
 * A manager to make working with transactions and 
 * the Transaction commits easy when using functional 
 * programming for instance
 * <code><pre>
 
 TransactionManager man = ...
 reader.stream()
 	   .mapToInt(node->man.save(node)
 	   .forEach(count->{if (count%1000 == 0) man.commit());
 
 * 
 * @author gerrim
 * @param <T> the type we will save, normally object
 */
public abstract class AbstractTransactionManager<T> implements AutoCloseable {

	
	protected final Session session;
	protected final int transactionCommitInterval;

	protected volatile long totalCount = 0; // Count of saves made.
	
	public AbstractTransactionManager(SessionFactory factory) {
		this(factory.openSession());
	}
	
	public AbstractTransactionManager(Session session) {
		this(session, -1);
	}
	
	/**
	 * 
	 * @param session
	 * @param transactionCommitInterval How often to automatically commit or -1 not to do automatic commits.
	 */
	public AbstractTransactionManager(Session session, int transactionCommitInterval) {
		this.session = session;
		this.transactionCommitInterval = transactionCommitInterval;
	}

	protected abstract Transaction beginTransaction();
	
	protected abstract Transaction getTransaction();
	
	protected abstract Transaction removeTransaction();
	
	
	public long commit(int unused) {
		return commit(getTransaction(), true);
	}
	
	public long commit() {
		return commit(getTransaction(), true);
	}
	
	protected long commit(Transaction transaction, boolean beginNew) {
		if (transaction==null) return -1;
		if (transaction.canCommit()) {
			transaction.commit();
			transaction.close();
		}
		session.clear(); // @see https://dzone.com/articles/improving-neo4j-ogm-performance
		if (beginNew) beginTransaction();
		return totalCount;
	}
	
	public abstract int save(T node);

	public long getTotalCount() {
		return totalCount;
	}
	
}