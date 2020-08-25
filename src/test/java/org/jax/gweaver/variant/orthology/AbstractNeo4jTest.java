package org.jax.gweaver.variant.orthology;

import java.util.Collections;
import java.util.Map;

import org.jax.gweaver.variant.orthology.domain.Gene;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;


public abstract class AbstractNeo4jTest {

	@SuppressWarnings("rawtypes")
	@Container 
	protected static final Neo4jContainer databaseServer = new Neo4jContainer();
	protected static SessionFactory sessionFactory;
	
	@BeforeAll
	public static void prepareSessionFactory() {
		
		Configuration testConfiguration = new Configuration.Builder()
	        .uri(databaseServer.getBoltUrl())
	        .credentials("neo4j", databaseServer.getAdminPassword())
	        .build();
	    sessionFactory = new SessionFactory(testConfiguration, Gene.class.getPackageName());
	}
	
	@AfterAll
	public static void close() {
		sessionFactory.close();
	}

	protected Session session;
	
	@BeforeEach
	public void createSession() {
		session = sessionFactory.openSession();
	}
	
	@AfterEach
	public void clearSession() {
		Map<String,Object> empty =  Collections.emptyMap();
		session.query("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r;", empty);
		session.purgeDatabase();
		session.clear();
	}
}
