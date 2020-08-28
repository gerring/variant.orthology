package org.jax.gweaver.variant.orthology.transaction;

import java.util.function.Consumer;

import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.neo4j.ogm.session.Session;
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
public class SimpleTransactionManager<T> extends AbstractTransactionManager<T> {

	
	protected final int transactionCommitInterval;

	protected volatile long totalCount = 0; // Count of saves made.
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
		super(session);
		this.transactionCommitInterval = transactionCommitInterval;
		this.transCount = 0;
		beginTransaction();
	}
	
	@Override
	public long run(AbstractReader<T> reader, Consumer<T> lines, Consumer<TimeInfo> overallTime) throws InterruptedException {
		TimeInfo info = new TimeInfo();
		long count = reader.stream()
				.map(node->{lines.accept(node); return node;})
				.mapToInt(this::save)
				.mapToLong(info::increment)
				.count();
		info.stop();
		overallTime.accept(info);
		return count;
	}
	
	protected synchronized Transaction beginTransaction() {
		this.transaction = session.beginTransaction(Type.READ_WRITE);
		transCount = 0;
		return getTransaction();
	}

	protected synchronized Transaction getTransaction() {
		return this.transaction;
	}

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
	

	public long getTotalCount() {
		return totalCount;
	}

}
