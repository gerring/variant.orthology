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
	   .count();
  
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
	protected Transaction beginTransaction() {
		this.transaction = session.beginTransaction(Type.READ_WRITE);
		transCount = 0;
		return getTransaction();
	}

	@Override
	protected Transaction getTransaction() {
		return this.transaction;
	}

	@Override
	protected Transaction removeTransaction() {
		Transaction ret = this.transaction;
		this.transaction = null;
		return ret;
	}
	
	public int save(Object node) {
		
		session.save(node);

		++transCount;
		if (transactionCommitInterval>0 && transCount>=transactionCommitInterval) {
			commit();
		}
		++totalCount;
		return transCount;
	}
	
	public void close() {
		transaction.commit();
		transaction.close();
		session.clear(); // @see https://dzone.com/articles/improving-neo4j-ogm-performance
	}
}
