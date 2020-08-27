package org.jax.gweaver.variant.orthology.transaction;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;

/**
 * 
 * This class runs tranactions with basic
 * threads and a semaphore to control number.
 * It is easier to deal with than parallel streams
 * but slightly slower.
 * 
 * 
 * @author gerrim
 *
 */
public class ThreadTransactionManager<T> extends AbstractTransactionManager<T> {

	
	private int parallelization;
	
	/**
	 * Automatic commits are done every interval saves.
	 * @param session
	 * @param transactionCommitInterval
	 */
	public ThreadTransactionManager(Session session) {
		this(session, Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * Automatic commits are done every interval saves.
	 * @param session
	 * @param transactionCommitInterval
	 */
	public ThreadTransactionManager(Session session, int parallelization) {
		super(session);
		this.parallelization = parallelization;
	}
	
	/**
	 * Submit and wait to be executed the processing of the stream of 
	 * line from the file.
	 */
	@Override
	public long run(AbstractReader<T> reader, Consumer<T> consumer, Consumer<TimeInfo> onClose) throws InterruptedException {
	
		// Tried parallel streams, thread pools all sorts but problem it that at 
		// it's end the thread must always commit for itself.
		final Semaphore semaphore = new Semaphore(parallelization); // Number of running threads at one time.
		
		ThreadFactory factory = Executors.defaultThreadFactory();
		while(!reader.isEmpty()) {
			semaphore.acquire();
			factory.newThread(()->chunk(semaphore, reader, consumer, onClose)).start();
		}
		
		// Block until all are not waiting (there might be some threads running)
		semaphore.acquire(parallelization);
		
		return -1; // TOOD estimate how many ran.
	}
	
	private void chunk(Semaphore semaphore, 
						AbstractReader<T> reader, 
						Consumer<T> consumer, 
						Consumer<TimeInfo> onClose) {
		
		final TimeInfo info = new TimeInfo();
		try (info) {
			try {
				Stream<T> beans = reader.wind();
				if (beans==null) return;
				Transaction transaction = session.beginTransaction();
				try {
					beans.forEach(bean->{
						consumer.accept(bean);
						info.increment();
					});
				} finally {
					transaction.commit();
					transaction.close();
				}
			} finally {
				semaphore.release();
			}
		}
		if (onClose!=null) {
			onClose.accept(info);
		}

	}

	@Override
	public int save(T node) {
		session.save(node);
		return -1;
	}

}
