package au.com.sixtree.activemq;

import static org.junit.Assert.*;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.ldap.server.ApacheDSContainer;

public class SoapActionAuthorisationTest {

	private static ApacheDSContainer ldapServer;

	private BrokerService broker;

	@BeforeClass
	public static void createLdap() throws Exception {
		ldapServer = new ApacheDSContainer("ou=system", "test.ldif");
		ldapServer.afterPropertiesSet();
		ldapServer.start();
	}

	@AfterClass
	public static void stopLdap() throws Exception {
		ldapServer.stop();
		ldapServer = null;
	}

	@Before
	public void setUp() throws Exception {
		broker = BrokerFactory.createBroker("xbean:activemq.xml");
		broker.start();
	}

	@After
	public void tearDown() throws Exception {
		broker.stop();
		broker = null;
	}

	@Test
	public void testAuthorised() throws Exception {

		ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		Connection conn = connFactory.createConnection("first", "secret");
		conn.start();

		// Create a Session
		Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Queue requestQueue = session.createQueue("dynamicQueues/request");

		MessageProducer producer = session.createProducer(requestQueue);
		Message msg = session.createMessage();
		msg.setStringProperty("SOAPJMS_soapAction",
				"urn:deakin:StudentService:v1:getStudent");

		try {
			producer.send(msg);
		} catch (SecurityException se) {
			fail(se.getMessage());
		}

	}

	@Test
	public void testNotAuthorised() throws Exception {

		ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		Connection conn = connFactory.createConnection("first", "secret");
		conn.start();

		// Create a Session
		Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Queue requestQueue = session.createQueue("dynamicQueues/request");

		MessageProducer producer = session.createProducer(requestQueue);
		Message msg = session.createMessage();
		msg.setStringProperty("SOAPJMS_soapAction",
				"urn:deakin:StudentService:v2:badoper");

		try {
			producer.send(msg);
			fail("expected authorisation exception");
		} catch (JMSException e) {
			assertEquals(SecurityException.class, e.getCause().getClass());
		}

	}
	
	@Test
	public void testResponseQueue() throws Exception {

		ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		Connection conn = connFactory.createConnection("first", "secret");
		conn.start();

		// Create a Session
		Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Queue requestQueue = session.createQueue("dynamicQueues/response");

		MessageProducer producer = session.createProducer(requestQueue);
		Message msg = session.createMessage();

		try {
			producer.send(msg);
		} catch (SecurityException se) {
			fail(se.getMessage());
		}

	}
	
	@Test
	public void testEmptySOAPAction() throws Exception {

		ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		Connection conn = connFactory.createConnection("first", "secret");
		conn.start();

		// Create a Session
		Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Queue requestQueue = session.createQueue("dynamicQueues/request");

		MessageProducer producer = session.createProducer(requestQueue);
		Message msg = session.createMessage();

		try {
			producer.send(msg);
			fail("expected authorisation exception");
		} catch (JMSException e) {
			assertEquals(e.getCause().getMessage(), "SOAPJMS_soapAction not found in JMS properties");
		}

	}

}
