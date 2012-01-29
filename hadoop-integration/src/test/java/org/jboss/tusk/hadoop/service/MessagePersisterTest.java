package org.jboss.tusk.hadoop.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.hbase.TableExistsException;
import org.jboss.tusk.hadoop.HBaseException;
import org.jboss.tusk.hadoop.HBaseFacade;
import org.jboss.tusk.hadoop.service.MessagePersister;
import org.junit.Before;
import org.junit.Test;



public class MessagePersisterTest {
	
	private String messageKey1 = "message1";
	private byte[] data1 = "<message><name>data1</name><value>something1</value></message>".getBytes();
	private String metadataStr1 = "<metadata>" +
			"<field><name>field1.1</name><value>blue1</value></field>" +
			"<field><name>field1.2</name><value>orange1</value></field>" +
			"<field><name>field1.3</name><value>purple1</value></field>" +
			"</metadata>";
	private byte[] metadata1 = metadataStr1.getBytes();
	private Map<String, Object> metadataFields1 = new HashMap<String, Object>();
	
	private Map<String, Map<String, byte[]>> dataAndMetadata1;
	private List<String> columnFamilies1 = new ArrayList<String>();

	private String messageKey2 = "message2";
	private byte[] data2 = "<message><name>data2</name><value>something2</value></message>".getBytes();
	private String metadataStr2 = "<metadata>" +
			"<field><name>field2.1</name><value>blue2</value></field>" +
			"<field><name>field2.2</name><value>orange2</value></field>" +
			"<field><name>field2.3</name><value>purple2</value></field>" +
			"</metadata>";
	private byte[] metadata2 = metadataStr2.getBytes();
	private Map<String, Object> metadataFields2 = new HashMap<String, Object>();
	
	private Map<String, Map<String, byte[]>> dataAndMetadata2;
	private List<String> columnFamilies2 = new ArrayList<String>();

	private String messageKey3 = "message3";
	private String messageKey4 = "message4";
	private String messageKey5 = "message5";
	private String messageKey6 = "message6";
	private String messageKey7 = "message7";
	private String messageKey8 = "message8";
	private String messageKey9 = "message9";
	private String messageKey10 = "message10";
	
	@Before
	public void setup() {
		//message1 data
		dataAndMetadata1 = new HashMap<String, Map<String, byte[]>>();
		
		Map<String, byte[]> dataCells1 = new HashMap<String, byte[]>();
		dataCells1.put(MessagePersister.QUANTIFIER_VALUE, data1);
		dataAndMetadata1.put(MessagePersister.COL_FAMILY_DATA, dataCells1);
		
		Map<String, byte[]> metadataCells1 = new HashMap<String, byte[]>();
		metadataCells1.put(MessagePersister.QUANTIFIER_VALUE, metadata1);
		dataAndMetadata1.put(MessagePersister.COL_FAMILY_METADATA, metadataCells1);
		
		//message1 reading
		columnFamilies1.add(MessagePersister.COL_FAMILY_DATA);
		columnFamilies1.add(MessagePersister.COL_FAMILY_METADATA);
		
		//metadata1 fields for message-index
		metadataFields1.put("field1.1", "blue1");
		metadataFields1.put("field1.2", "orange1");
		metadataFields1.put("field1.3", "purple1");

		//message2 data
		dataAndMetadata2 = new HashMap<String, Map<String, byte[]>>();
		
		Map<String, byte[]> dataCells2 = new HashMap<String, byte[]>();
		dataCells2.put(MessagePersister.QUANTIFIER_VALUE, data2);
		dataAndMetadata2.put(MessagePersister.COL_FAMILY_DATA, dataCells2);
		
		Map<String, byte[]> metadataCells2 = new HashMap<String, byte[]>();
		metadataCells2.put(MessagePersister.QUANTIFIER_VALUE, metadata2);
		dataAndMetadata2.put(MessagePersister.COL_FAMILY_METADATA, metadataCells2);
		
		//message1 reading
		columnFamilies2.add(MessagePersister.COL_FAMILY_DATA);
		columnFamilies2.add(MessagePersister.COL_FAMILY_METADATA);
		
		//metadata1 fields for message-index
		metadataFields2.put("field2.1", "blue2");
		metadataFields2.put("field2.2", "orange2");
		metadataFields2.put("field2.3", "purple2");
	}

	@Test
	public void noBrainer() throws HBaseException {
		
	}
	
	/**
	 * This tests writing a message, reading its data, and removing it.
	 * @throws HBaseException
	 */
	@Test
	public void writeMessageData() throws HBaseException {
		MessagePersister mp = new MessagePersister();
		mp.writeMessage(messageKey1, data1);
		
		byte[] result = mp.readMessage(messageKey1);
		assertTrue(Arrays.equals(result, data1));
		
		mp.removeMessage(messageKey1);
		result = mp.readMessage(messageKey1);
		assertNull(result);
	}
	
	/**
	 * This tests writing a message along with some corresponding metadata,
	 * which is stored in a separate column family for this row.
	 * @throws HBaseException
	 */
	@Test
	public void writeMessageWithMetaData() throws HBaseException {
		MessagePersister mp = new MessagePersister();
		mp.writeMessage(messageKey1, dataAndMetadata1);
		
		Map<String, Map<String, byte[]>> resultMap = mp.readMessage(messageKey1, columnFamilies1);
		assertNotNull(resultMap);
		assertTrue(resultMap.size() == 2);
		for (String columnFamily : columnFamilies1) {
			Map<String, byte[]> columnFamilyDataMap = resultMap.get(columnFamily);
			assertTrue(columnFamily + " does not match.", 
					Arrays.equals(
							dataAndMetadata1.get(columnFamily).get(MessagePersister.QUANTIFIER_VALUE), 
							columnFamilyDataMap.get(MessagePersister.QUANTIFIER_VALUE)));
		}
		
		mp.removeMessage(messageKey1);
		resultMap = mp.readMessage(messageKey1, columnFamilies1);
		assertNotNull(resultMap);
		assertTrue(resultMap.size() == 0);
	}
	
	/**
	 * This tests writing a message and corresponding index metadata, then reading
	 * that index metadata, then deleting the message and index metadata.
	 * @throws HBaseException
	 */
	@Test
	public void writeMessageAndIndex() throws HBaseException {
		MessagePersister mp = new MessagePersister();
		mp.writeMessage(messageKey1, data1);
		mp.writeMessageIndex(messageKey1, metadataFields1);
		
		Map<String, byte[]> indexFields = mp.readMessageIndex(messageKey1);
		for (Entry<String, byte[]> field : indexFields.entrySet()) {
			assertTrue(Arrays.equals(metadataFields1.get(field.getKey()).toString().getBytes(), field.getValue()));
		}
		
		mp.removeMessageIndex(messageKey1);
		indexFields = mp.readMessageIndex(messageKey1);
		assertNotNull(indexFields);
		assertTrue(indexFields.size() == 0);
		
		mp.removeMessage(messageKey1);
		byte[] result = mp.readMessage(messageKey1);
		assertNull(result);
	}
	
	/**
	 * This tests writing messages, scanning the table, and removing the messages.
	 * @throws HBaseException
	 */
	@Test
	public void writeAndScanMessageData() throws HBaseException {
		MessagePersister mp = new MessagePersister();
		HBaseFacade hbs = new HBaseFacade();
		
		mp.writeMessage(messageKey1, data1);
		mp.writeMessage(messageKey2, data2);
		mp.writeMessage(messageKey3, data2);
		mp.writeMessage(messageKey4, data2);
		mp.writeMessage(messageKey5, data2);
		mp.writeMessage(messageKey6, data2);
		mp.writeMessage(messageKey7, data2);
		mp.writeMessage(messageKey8, data2);
		mp.writeMessage(messageKey9, data2);
		mp.writeMessage(messageKey10, data2);
		
		Map<String, byte[]> dataMap = hbs.scan(MessagePersister.TABLE_MESSAGE, 10, 
				MessagePersister.COL_FAMILY_DATA, MessagePersister.QUANTIFIER_VALUE);

		assertTrue(Arrays.equals(dataMap.get(messageKey1), data1));
		assertTrue(Arrays.equals(dataMap.get(messageKey2), data2));
		assertTrue(Arrays.equals(dataMap.get(messageKey3), data2));
		assertTrue(Arrays.equals(dataMap.get(messageKey4), data2));
		assertTrue(Arrays.equals(dataMap.get(messageKey5), data2));
		assertTrue(Arrays.equals(dataMap.get(messageKey6), data2));
		assertTrue(Arrays.equals(dataMap.get(messageKey7), data2));
		assertTrue(Arrays.equals(dataMap.get(messageKey8), data2));
		assertTrue(Arrays.equals(dataMap.get(messageKey9), data2));
		assertTrue(Arrays.equals(dataMap.get(messageKey10), data2));

		mp.removeMessage(messageKey1);
		mp.removeMessage(messageKey2);
		mp.removeMessage(messageKey3);
		mp.removeMessage(messageKey4);
		mp.removeMessage(messageKey5);
		mp.removeMessage(messageKey6);
		mp.removeMessage(messageKey7);
		mp.removeMessage(messageKey8);
		mp.removeMessage(messageKey9);
		mp.removeMessage(messageKey10);
	}
	
	/**
	 * This tests writing messages, scanning the table for the keys, and removing the messages.
	 * @throws HBaseException
	 */
	@Test
	public void writeAndScanMessageKeys() throws HBaseException {
		MessagePersister mp = new MessagePersister();
		HBaseFacade hbs = new HBaseFacade();
		
		mp.writeMessage(messageKey1, data1);
		mp.writeMessage(messageKey2, data2);
		mp.writeMessage(messageKey3, data2);
		mp.writeMessage(messageKey4, data2);
		mp.writeMessage(messageKey5, data2);
		mp.writeMessage(messageKey6, data2);
		mp.writeMessage(messageKey7, data2);
		mp.writeMessage(messageKey8, data2);
		mp.writeMessage(messageKey9, data2);
		mp.writeMessage(messageKey10, data2);
		
		Set<Object> keys = hbs.scanForKeys(MessagePersister.TABLE_MESSAGE);
		assertTrue("Did not return a key", keys.contains(messageKey1));
		assertTrue("Did not return a key", keys.contains(messageKey2));
		assertTrue("Did not return a key", keys.contains(messageKey3));
		assertTrue("Did not return a key", keys.contains(messageKey4));
		assertTrue("Did not return a key", keys.contains(messageKey5));
		assertTrue("Did not return a key", keys.contains(messageKey6));
		assertTrue("Did not return a key", keys.contains(messageKey7));
		assertTrue("Did not return a key", keys.contains(messageKey8));
		assertTrue("Did not return a key", keys.contains(messageKey9));
		assertTrue("Did not return a key", keys.contains(messageKey10));

		mp.removeMessage(messageKey1);
		mp.removeMessage(messageKey2);
		mp.removeMessage(messageKey3);
		mp.removeMessage(messageKey4);
		mp.removeMessage(messageKey5);
		mp.removeMessage(messageKey6);
		mp.removeMessage(messageKey7);
		mp.removeMessage(messageKey8);
		mp.removeMessage(messageKey9);
		mp.removeMessage(messageKey10);
	}
	/**
	 * This tests writing messages and removing them in a bulk delete.
	 * @throws HBaseException
	 */
	@Test
	public void writeAndRemoveRows() throws HBaseException {
		MessagePersister mp = new MessagePersister();
		HBaseFacade hbs = new HBaseFacade();
		
		mp.writeMessage(messageKey1, data1);
		mp.writeMessage(messageKey2, data2);
		mp.writeMessage(messageKey3, data2);
		mp.writeMessage(messageKey4, data2);
		
		Set<Object> keys = new HashSet<Object>();
		keys.add(messageKey1);
		keys.add(messageKey2);
		keys.add(messageKey3);
		keys.add(messageKey4);
		
		hbs.removeRows(MessagePersister.TABLE_MESSAGE, keys);

		byte[] result = mp.readMessage(messageKey1);
		assertNull(result);
		result = mp.readMessage(messageKey2);
		assertNull(result);
		result = mp.readMessage(messageKey3);
		assertNull(result);
		result = mp.readMessage(messageKey4);
		assertNull(result);
	}
	
	/**
	 * IMPORTANT - cannot run this test two times in a row
	 * without disabling and dropping the test table.
	 * @throws HBaseException
	 */
	@Test
	public void createTable() throws HBaseException {
		String testCF = "testCF";
		String testDataVal = "This is some data.";
		String testTable = "testTable";
		String testField = "testField";
		String testKey = "testKey";
		
		List<String> colFamilies = new ArrayList<String>();
		colFamilies.add(testCF);

		HBaseFacade hbs = new HBaseFacade();
		try {
			hbs.createTable(testTable, colFamilies);
		} catch (HBaseException ex) {
			if (ex.getCause() instanceof TableExistsException) {
				System.err.println("Cannot test createTable because the " + testTable +
						" table already exists.");
				return;
			} else {
				throw ex;
			}
		}

		Map<String, Map<String, byte[]>> testData = 
			new HashMap<String, Map<String, byte[]>>();
		
		Map<String, byte[]> dataCells1 = new HashMap<String, byte[]>();
		dataCells1.put(testField, testDataVal.getBytes());
		testData.put(colFamilies.get(0), dataCells1);
		hbs.addRow(testTable, testKey, testData);

		testData = hbs.readRow(testTable, testKey, colFamilies);
		assertTrue(Arrays.equals(testData.get(colFamilies.get(0)).get(testField), testDataVal.getBytes()));
		
		hbs.removeRow(testTable, testKey);
	}

}

