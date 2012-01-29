package org.jboss.tusk.hadoop.service;

//import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.jboss.tusk.common.TuskHBaseConfiguration;
import org.jboss.tusk.hadoop.HBaseException;
import org.jboss.tusk.hadoop.HBaseFacade;

//import org.apache.hadoop.hbase.util.Bytes;


/**
 * The *index methods were written before Infinispan was in the picture for
 * storing the message index metadata. They shouldn't be used if Infinispan
 * is used. If it isn't used, then the *index methods can be used to store
 * the index metadata in the same HBase data store (it just won't be searchable
 * like it is via the Infinispan-Lucene integration.)
 * @author justin
 *
 */
public class MessagePersister {
	
	private static final TuskHBaseConfiguration tuskHbaseConf = new TuskHBaseConfiguration();

	public static final String TABLE_MESSAGE = tuskHbaseConf.getMpTable();
	public static final String COL_FAMILY_DATA = tuskHbaseConf.getMpDataCF();
	public static final String COL_FAMILY_METADATA = tuskHbaseConf.getMpMetadataCF();
	public static final String QUANTIFIER_VALUE = tuskHbaseConf.getMpValueQuantifier();

	public static final String TABLE_MESSAGE_INDEX = tuskHbaseConf.getMpIndexTable();
	public static final String COL_FAMILY_FIELDS = tuskHbaseConf.getMpFieldsCF();

	private HBaseFacade hbf;
	
	public MessagePersister() {
		hbf = new HBaseFacade();
	}
	
	/**
	 * Persists a message.
	 * @param messageKey
	 * @param data
	 * @throws HBaseException
	 */
	public void writeMessage(String messageKey, byte[] data) throws HBaseException {
		Map<String, Map<String, byte[]>> dataMap = new HashMap<String, Map<String, byte[]>>(1);
		Map<String, byte[]> dataCells = new HashMap<String, byte[]>();
		dataCells.put(QUANTIFIER_VALUE, data);
		dataMap.put(COL_FAMILY_DATA, dataCells);
		writeMessage(messageKey, dataMap);
	}
	
	/**
	 * Persists a message, providing data for multiple column families
	 * (e.g. data and metadata). 
	 * @param messageKey
	 * @param dataMap
	 * @throws HBaseException
	 */
	public void writeMessage(String messageKey, Map<String, Map<String, byte[]>> dataMap) throws HBaseException {
		hbf.addRow(TABLE_MESSAGE, messageKey, dataMap);
	}
	
	/**
	 * Persists message index metadata.
	 * 
	 * TODO this is probably unnecessary since the index metadata will be handled elsewhere
	 * 
	 * @param messageKey corresponds the row in the messages table to which this metadata belongs
	 * @param fields a simple map of string-keyed fields
	 * @throws HBaseException
	 */
	public void writeMessageIndex(String messageKey, Map<String, Object> fields) throws HBaseException {
		//TODO is it ok to use the message's key as the metadata row's primary key?
		//Or should we have a unique message index row key and use the messageKey as just another index field?

		Map<String, Map<String, byte[]>> dataMap = new HashMap<String, Map<String, byte[]>>(1);
		
		Map<String, byte[]> dataCells = new HashMap<String, byte[]>();		
		for (Entry<String, Object> field : fields.entrySet()) {
			byte[] fieldVal = field.getValue().toString().getBytes();
//			dataCells.put(field.getKey(), Bytes.toBytes(ByteBuffer.wrap(fieldVal)));
			dataCells.put(field.getKey(), fieldVal); //TODO test this line
		}
		dataMap.put(COL_FAMILY_FIELDS, dataCells);
		
		hbf.addRow(TABLE_MESSAGE_INDEX, messageKey, dataMap);
	}
		
	/**
	 * Reads the data for a message.
	 * @param messageKey
	 * @return
	 * @throws HBaseException
	 */
	public byte[] readMessage(String messageKey) throws HBaseException {
		if (hbf.isEmpty(messageKey)) {
			throw new IllegalArgumentException("messageKey cannot be null or empty.");
		}
		
		List<String> columnFamilies = new ArrayList<String>();
		columnFamilies.add(COL_FAMILY_DATA);
		
		Map<String, Map<String, byte[]>> resultMap = readMessage(messageKey, columnFamilies);
		if (!resultMap.containsKey(COL_FAMILY_DATA)) {
			return null;
		}
		
		Map<String, byte[]> columnFamilyData = resultMap.get(COL_FAMILY_DATA);
		return columnFamilyData.get(QUANTIFIER_VALUE);
	}
	
	/**
	 * Reads the data from multiple column families for a message.
	 * @param messageKey
	 * @param columnFamilies
	 * @return
	 * @throws HBaseException
	 */
	public Map<String, Map<String, byte[]>> readMessage(String messageKey, List<String> columnFamilies) throws HBaseException {
		return hbf.readRow(TABLE_MESSAGE, messageKey, columnFamilies);
	}
	
	/**
	 * Reads the data for a message index.
	 * @param messageKey
	 * @return
	 * @throws HBaseException
	 */
	public Map<String, byte[]> readMessageIndex(String messageKey) throws HBaseException {
		if (hbf.isEmpty(messageKey)) {
			throw new IllegalArgumentException("messageKey cannot be null or empty.");
		}
		
		List<String> columnFamilies = new ArrayList<String>();
		columnFamilies.add(COL_FAMILY_FIELDS);
		
		Map<String, Map<String, byte[]>> resultMap = hbf.readRow(TABLE_MESSAGE_INDEX, messageKey, columnFamilies);
		Map<String, byte[]> results = resultMap.get(COL_FAMILY_FIELDS);
		if (hbf.isEmpty(results)) {
			results = new HashMap<String, byte[]>();
		}
		
		return results;
	}
	

	/**
	 * Removes a message index row.
	 * @param messageKey
	 * @throws HBaseException
	 */
	public void removeMessageIndex(String messageKey) throws HBaseException {
		hbf.removeRow(TABLE_MESSAGE_INDEX, messageKey);
	}
	
	/**
	 * Removes a message.
	 * @param messageKey
	 * @throws HBaseException
	 */
	public void removeMessage(String messageKey) throws HBaseException {
		hbf.removeRow(TABLE_MESSAGE, messageKey);
	}
	
}
