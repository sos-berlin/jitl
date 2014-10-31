package com.sos.jitl.sync;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SyncNodeListJUnitTest {
	private static final String SYNCID = "1";
	@SuppressWarnings("unused")
	private final static String conClassName = "JobSchedulerSynchronizeJobChainsJUnitTest";
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(JobSchedulerSynchronizeJobChainsJUnitTest.class);

	protected JobSchedulerSynchronizeJobChainsOptions objOptions = null;

	@Test
	public void testAddNode() {

		SyncNodeList sl = new SyncNodeList();

		SyncNode sn1 = new SyncNode();
		sn1.setSyncNodeJobchainName("my_chain1");
		sn1.setSyncNodeState("my_state1");
		sn1.setRequired(1);

		SyncNode sn2 = new SyncNode();
		sn2.setSyncNodeJobchainName("my_chain2");
		sn2.setSyncNodeState("my_state2");

		SyncNode sn3 = new SyncNode();
		sn3.setSyncNodeJobchainName("my_chain3");
		sn3.setSyncNodeState("my_state3");

		sl.addNode(sn1);
		sl.addNode(sn2);
		sl.addNode(sn3);

		assertEquals("testAddNode", 3, sl.getCount());

	}

	@Test
	public void testIsReleased() {
		// SYNCID kommt vom Parameter Snycid des Auftrages

		SyncNodeList sl = new SyncNodeList();
		SyncNodeWaitingOrder so1 = new SyncNodeWaitingOrder("so1",SYNCID);
		SyncNodeWaitingOrder so21 = new SyncNodeWaitingOrder("so21",SYNCID);
		SyncNodeWaitingOrder so22 = new SyncNodeWaitingOrder("so22",SYNCID);
		SyncNodeWaitingOrder so3 = new SyncNodeWaitingOrder("so3",SYNCID);

		// sn1 bis sn3 sind die Knoten, die den Sync-Job beeinhalten
		//
		// <show_state max_order_history="0" max_orders="0"
		// what="job_chains folders" subsystems="folder order"/>

		// Mit der Abfrage kann der Synch-Job erstmal ermitteln, in welchen
		// Job-Ketten und an welchen Knoten er zum Einsatz kommt.
		// XPath: //job_chain[job_chain_node/@job='/my/synchJob']/@path liefert
		// Ketten

		// 1,2,3 kommt aus dem Job-Parameters jobchain_required
		SyncNode sn1 = new SyncNode();
		sn1.setSyncNodeJobchainName("my_chain1");
		sn1.setSyncNodeState("my_state1");
		sn1.setRequired(1);

		SyncNode sn2 = new SyncNode();
		sn2.setSyncNodeJobchainName("my_chain2");
		sn2.setSyncNodeState("my_state2");

		SyncNode sn3 = new SyncNode();
		sn3.setSyncNodeJobchainName("my_chain3");
		sn3.setSyncNodeState("my_state3");

		
		//2.Abfrage:
		//	<show_job job="/my/synchJob" max_task_history="0" what="job_orders job_chains payload"/>
		//	Die Abfrage liefert alle Aufträge inkl. derer Parameter, die am synchJob gerade "hängen".
		
		sn1.addOrder(so1);
		sn2.addOrder(so21);

		sl.addNode(sn1);
		sl.addNode(sn2);
		sl.addNode(sn3);

		sl.setRequired("my_chain2_required_orders_2");
		sl.setRequired("my_chain3_required_orders_1");
		sl.setRequired("my_chain3xxx_3");

		assertEquals("testExecute", false, sl.isReleased()); //$NON-NLS-1$

		// "*" kommt aus dem Jobparametern
		sn2.addOrder(so22, "*");
		assertEquals("testExecute", false, sl.isReleased()); //$NON-NLS-1$

		// SYNCID kommt aus dem Jobparametern
		sn2.addOrder(so22, SYNCID);
		sn3.addOrder(so3, SYNCID);
		assertEquals("testExecute", true, sl.isReleased()); //$NON-NLS-1$

	}

	@Test
	public void setRequired() {

		SyncNodeList sl = new SyncNodeList();

		SyncNode sn1 = new SyncNode();
		sn1.setSyncNodeJobchainName("my_chain1");
		sn1.setSyncNodeState("my_state1");
		sn1.setRequired(1);

		SyncNode sn2 = new SyncNode();
		sn2.setSyncNodeJobchainName("my_chain2");
		sn2.setSyncNodeState("my_state2");

		sl.addNode(sn1);
		sl.addNode(sn2);

		sl.setRequired(1);
		sl.setRequired("my_chain2_required_orders_123");

		assertEquals("setRequired", 123, sn2.getRequired());

		
		sl.setRequired(1);
		sl.setRequired("my_chain2,my_state2_required_orders_456");

		assertEquals("setRequired", 456, sn2.getRequired());
	}

	@Test
	public void getRequiredFromJobchain() {
		SyncNodeList sl = new SyncNodeList();
		String erg = sl.getRequiredFromPrefix("my_chain2", "my_chain2_required_orders_xxx");
		erg = sl.getRequiredFromPrefix("my_chain2", "my_chain2_required_orders_123");
		assertEquals("getRequiredFromJobchain", "123", erg);
	}

	@Test
	public void getRequiredFromJobchainNode() {
		SyncNodeList sl = new SyncNodeList();
		String erg = "";
		erg = sl.getRequiredFromPrefix("my_chain2;test", "my_chain2;test_required_orders_xxx");
		erg = sl.getRequiredFromPrefix("my_chain2;test", "my_chain2;test_required_orders_123");
		assertEquals("getRequiredFromJobchainNode", "123", erg);
	}

	@Test
	public void setRequiredInt() {
		SyncNodeList sl = new SyncNodeList();

		SyncNode sn1 = new SyncNode();
		sn1.setSyncNodeJobchainName("my_chain1");
		sn1.setSyncNodeState("my_state1");
		sn1.setRequired(1);

		SyncNode sn2 = new SyncNode();
		sn2.setSyncNodeJobchainName("my_chain2");
		sn2.setSyncNodeState("my_state2");

		sl.addNode(sn1);
		sl.addNode(sn2);

		sl.setRequired(1);
		sl.setRequired("my_chain2_required_orders_123");

		assertEquals("setRequiredInt", 1, sn1.getRequired());
		assertEquals("setRequiredInt", 123, sn2.getRequired());
		
		sl.setRequired("my_chain2,my_state2_required_orders_12356");
		assertEquals("setRequiredInt", 12356, sn2.getRequired());
	}

} // class Test