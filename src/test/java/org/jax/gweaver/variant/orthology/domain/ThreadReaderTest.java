package org.jax.gweaver.variant.orthology.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jax.gweaver.variant.orthology.AbstractNeo4jTest;
import org.jax.gweaver.variant.orthology.AbstractTransactionManager;
import org.jax.gweaver.variant.orthology.SimpleTransactionManager;
import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.jax.gweaver.variant.orthology.io.RepeatedLineReader;
import org.jax.gweaver.variant.orthology.io.VariantReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.session.Session;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test saving and tracting on many threads at the same time.
 * 
 * If you move the transaction out to be accessed by multiple threads
 * then neo4j client goes bad. Howevever if each thread has
 * one transaction then neo4j client is multithreaded.
 * 
 * @author gerrim
 *
 */
@Testcontainers
public class ThreadReaderTest extends AbstractNeo4jTest{

	
	@Test
	public void ten() throws Exception {
		Session session = sessionFactory.openSession();	
		test(session, 10, 1000);
	}
	
	@Disabled("Long running but this one is just to check it holds up. No need to run all the time.")
	@Test
	public void hundred() throws Exception {
		Session session = sessionFactory.openSession();	
		test(session, 100, 10000);
	}

	private void test(Session session, int nThreads, int linesInFile) throws Exception {
		
		List<AbstractReader<GeneticEntity>> readers = new LinkedList<>();

		List<Thread> threads = new LinkedList<>();
		CountDownLatch latch = new CountDownLatch(nThreads);
		for (int i = 0; i < nThreads; i++) {

			final int threadIndex = i;
			AbstractReader<GeneticEntity> reader = new RepeatedLineReader<>("Homo sapiens", linesInFile, VariantReader.class);
			readers.add(reader);
			Thread thread = new Thread(()->{
				// 1 transaction per thread!
				try (AbstractTransactionManager<Object> man = new SimpleTransactionManager(session)) {
					// Save nodes, every 100 
					reader.stream()
						.mapToInt(man::save)
						.filter(count -> count%(linesInFile/100) == 0)
						.forEach(man::commit);
					
				} catch (RuntimeException ne) {
					//System.out.println("Thread "+threadIndex+" ERROR");
					throw ne;
				} catch (Exception neOther) {
					//System.out.println("Thread "+threadIndex+" ERROR");
					neOther.printStackTrace();
				} finally {
					latch.countDown();
					//System.out.println("Thread "+threadIndex+" Exit");
				}
			}, ThreadReaderTest.class.getSimpleName()+"_"+threadIndex);
			thread.setDaemon(true);
			threads.add(thread);
		}

		// We start then roughly at the same time.
		for (Thread thread : threads) thread.start();

		// We wait for them
		latch.await(60, TimeUnit.SECONDS);

		// Check they were processed.
		for (AbstractReader<GeneticEntity> reader : readers) {
			assertEquals(linesInFile, reader.linesProcessed());
		}
	}
}
