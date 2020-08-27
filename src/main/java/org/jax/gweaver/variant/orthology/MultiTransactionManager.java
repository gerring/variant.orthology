package org.jax.gweaver.variant.orthology;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jax.gweaver.variant.orthology.domain.GeneticEntity;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.Transaction.Type;

/**
 * 
 * This class is to be used with parallel streams. It
 * attempts to commit transactions automatically as the 
 * stream proceeds, there is no need to call commit
 * manually from the stream.
 * 
 * A manager to make working with transactions and 
 * the Transaction commits easy when using functional 
 * programming for instance
 * <code><pre>
 
 AbstractTransactionManager man = ...
 reader.stream()
 	   .mapToInt(node->man.save(node)
 	   .count();
 
 * 
 * @author gerrim
 *
 */
public class MultiTransactionManager<T extends GeneticEntity> extends AbstractTransactionManager<T> {

	
	private Map<Long, Transaction> transactions;
	private Map<Long, Integer> transCounts;
	
	/**
	 * When calling this no automatic commits are done.
	 * @param session
	 */
	public MultiTransactionManager(Session session) {
		this(session, -1);
	}
	
	/**
	 * Automatic commits are done every interval saves.
	 * @param session
	 * @param transactionCommitInterval
	 */
	public MultiTransactionManager(Session session, int transactionCommitInterval) {
		super(session, transactionCommitInterval);
		this.transactions = Collections.synchronizedMap(new HashMap<>());
		this.transCounts = Collections.synchronizedMap(new HashMap<>());
	}

	private final Object LOCK = new Object();

	@Override
	protected Transaction beginTransaction() {
		if (transactions.containsKey(getId())) return getTransaction();
		synchronized(LOCK) {
			if (transactions.containsKey(getId())) return getTransaction();
			Transaction transaction = session.beginTransaction(Type.READ_WRITE);
			transactions.put(getId(), transaction);
			transCounts.put(getId(), 0);
			return transaction;
		}
	}
	
	@Override
	protected Transaction getTransaction() {
		return transactions.get(getId());
	}
	
	@Override
	protected Transaction removeTransaction() {
		return transactions.remove(getId());
	}
	
	protected int getTransactionCount() {
		return transCounts.get(getId());
	}

	
	public int save(GeneticEntity node) {
		
		Transaction transaction = beginTransaction(); // This thread might not have one yet		
		synchronized(transaction) {
			session.save(node);
			
			final int transCount = getTransactionCount()+1;		
			transCounts.put(getId(), transCount);
	
			boolean commit = node.isLastInStream() || (transactionCommitInterval>0 && transCount>=transactionCommitInterval);
			if (commit) {
				commit(removeTransaction(), false);
				transCounts.put(getId(), 0);
			}
	
			++totalCount;
			return getTransactionCount();
		}
	}

	public void close() {
		
		int size = 0;
		while(transactions.size()>0) {
			try {
				Thread.sleep(1000);
				System.out.println("Waiting for all transactions to stop. Size is "+transactions.size());
				++size;
				if (size>5) break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		transactions.clear();
		transCounts.clear();
	}
	
	private Long getId() {
		return Thread.currentThread().getId();
	}
}
