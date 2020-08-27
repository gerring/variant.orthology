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
 * programming for instance.
 * 
 * If this is used with a parallel stream as it is entirely
 * synchronized and uses one transaction, no speed up will result.
 * 
 * <code><pre>
 
 AbstractTransactionManager<Object> man = new SimpleTransactionManager(...);
 reader.stream()
 	   .mapToInt(node->man.save(node)
	   .count();
  </code></pre>
 * 
 * @author gerrim
 *
 */
public class SimpleTransactionManager extends AbstractTransactionManager<Object> {

	
	private Transaction transaction;
	private volatile int transCount;
	
	/**
	 * When calling this no automatic commits are done.
	 * @param session
	 */
	public SimpleTransactionManager(Session session) {
		this(session, -1);
	}
	
	/**
	 * Automatic commits are done every interval saves.
	 * @param session
	 * @param transactionCommitInterval
	 */
	public SimpleTransactionManager(Session session, int transactionCommitInterval) {
		super(session, transactionCommitInterval);
		this.transCount = 0;
		beginTransaction();
	}
	
	@Override
	protected synchronized Transaction beginTransaction() {
		this.transaction = session.beginTransaction(Type.READ_WRITE);
		transCount = 0;
		return getTransaction();
	}

	@Override
	protected synchronized Transaction getTransaction() {
		return this.transaction;
	}

	@Override
	protected synchronized Transaction removeTransaction() {
		Transaction ret = this.transaction;
		this.transaction = null;
		return ret;
	}
	
	public synchronized int save(Object node) {
		
		session.save(node);

		++transCount;
		if (transactionCommitInterval>0 && transCount>=transactionCommitInterval) {
			commit();
		}
		++totalCount;
		return transCount;
	}
	
	public synchronized void close() {
		transaction.commit();
		transaction.close();
		session.clear(); // @see https://dzone.com/articles/improving-neo4j-ogm-performance
		transaction = null;
	}
}
