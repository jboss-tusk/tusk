package org.jboss.tusk.datastore.nosql;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.tusk.common.TuskCassandraConfiguration;
import org.jboss.tusk.datastore.DataStore;
import org.jboss.tusk.exception.DataStoreException;

public class CassandraDataStore implements DataStore {
	
	private static final Log LOG = LogFactory.getLog(CassandraDataStore.class);

	//TODO should these be static?
	private static TuskCassandraConfiguration tuskCassConf = new TuskCassandraConfiguration();
	private static Cluster dataCluster = null;
	private static Keyspace ksp = null;
	private static ColumnFamilyTemplate<String, String> cfTemplate = null;
	
	public CassandraDataStore() {
		if (dataCluster == null) {
			//TODO make a CassandraFacade class that encapsulates all the cassandra stuff
			dataCluster = HFactory.getOrCreateCluster("data-cluster", tuskCassConf.getCluster());
			LOG.debug("Hector dataCluster=" + dataCluster);
			
			// This is the keyspace to use for the data we are storing
			KeyspaceDefinition keyspaceDef = dataCluster.describeKeyspace(tuskCassConf.getKeyspace());
			LOG.debug("Hector keyspaceDef=" + keyspaceDef);
			
			ksp = HFactory.createKeyspace(tuskCassConf.getKeyspace(), dataCluster);
			LOG.debug("Hector keyspace=" + ksp);
			
			cfTemplate = new ThriftColumnFamilyTemplate<String, String>(
					ksp, tuskCassConf.getColumnFamily(), StringSerializer.get(), StringSerializer.get());
			LOG.debug("Hector cfTemplate=" + cfTemplate);
		}
	}

	@Override
	public void put(String id, byte[] data) throws DataStoreException {
		ColumnFamilyUpdater<String, String> updater = cfTemplate.createUpdater(id);
		updater.setByteArray("body", data);
		updater.setLong("timestamp", System.currentTimeMillis());
		
		try {
			cfTemplate.update(updater);
		} catch (HectorException ex) {
			throw new DataStoreException("Got HectorException writing " +
					"message to data storage: " + ex.getMessage(), ex);
		}
	}

	@Override
	public byte[] get(String id) throws DataStoreException {
	    ColumnFamilyResult<String, String> result = cfTemplate.queryColumns(id);
	    return result.getString("body").getBytes(); //TODO does this work?
	}

}
