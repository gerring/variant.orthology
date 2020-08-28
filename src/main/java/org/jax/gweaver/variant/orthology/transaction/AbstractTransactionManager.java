package org.jax.gweaver.variant.orthology.transaction;

import java.util.function.Consumer;

import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.neo4j.ogm.session.Session;

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
	
	public AbstractTransactionManager(Session session) {
		this.session = session;
	}
	
	public abstract int save(T node);
	
	/** 
	 * Submit and wait to be executed the processing of the stream of 
	 * line from the file.
	 */
	public long run(AbstractReader<T> reader, Consumer<T> lines) throws InterruptedException {
		return run(reader, lines, null);
	}
	
	/**
	 * Submit and wait to be executed the processing of the stream of 
	 * line from the file.
	 */
	public abstract long run(AbstractReader<T> reader, Consumer<T> lines, Consumer<TimeInfo> timings) throws InterruptedException;


	public void close() {
		session.clear();
	}
}