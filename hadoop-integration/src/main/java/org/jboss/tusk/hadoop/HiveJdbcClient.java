package org.jboss.tusk.hadoop;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * export
 * CLASSPATH=.:/usr/lib/hive/lib/hive-exec-0.7.1-cdh3u1.jar:/usr/lib/hive/
 * lib/hive
 * -jdbc-0.7.1-cdh3u1.jar:/usr/lib/hive/lib/hive-metastore-0.7.1-cdh3u1.jar
 * :/usr/
 * lib/hive/lib/hive-service-0.7.1-cdh3u1.jar:/usr/lib/hive/lib/libfb303.jar
 * :/usr
 * /lib/hive/lib/log4j-1.2.15.jar:/usr/lib/hive/lib/slf4j-log4j12-1.6.1.jar:
 * /usr/
 * lib/hive/lib/slf4j-api-1.6.1.jar::/etc/alternatives/hadoop-lib/hadoop-core
 * .jar java -cp $CLASSPATH org/jboss/tusk/hadoop/HiveJdbcClient
 * 
 * @author justin
 * 
 */
public class HiveJdbcClient {

	private static final Log LOG = LogFactory.getLog(HiveJdbcClient.class);

	private static String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";

	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		Connection con = DriverManager.getConnection(
				"jdbc:hive://localhost:10000/default", "", "");
		Statement stmt = con.createStatement();
		String tableName = "testHiveDriverTable";
		stmt.executeQuery("drop table " + tableName);
		ResultSet res = stmt.executeQuery("create table " + tableName
				+ " (key int, value string)");
		// show tables
		String sql = "show tables '" + tableName + "'";
		LOG.debug("Running: " + sql);
		res = stmt.executeQuery(sql);
		if (res.next()) {
			LOG.debug(res.getString(1));
		}
		// describe table
		sql = "describe " + tableName;
		LOG.debug("Running: " + sql);
		res = stmt.executeQuery(sql);
		while (res.next()) {
			LOG.debug(res.getString(1) + "\t" + res.getString(2));
		}

		// load data into table
		// NOTE: filepath has to be local to the hive server
		// NOTE: /tmp/a.txt is a ctrl-A separated file with two fields per line
		// String filepath = "/tmp/a.txt";
		// sql = "load data local inpath '" + filepath + "' into table " +
		// tableName;
		// LOG.debug("Running: " + sql);
		// res = stmt.executeQuery(sql);

		// select * query
		sql = "select * from " + tableName;
		LOG.debug("Running: " + sql);
		res = stmt.executeQuery(sql);
		while (res.next()) {
			LOG.debug(String.valueOf(res.getInt(1)) + "\t" + res.getString(2));
		}

		// regular hive query
		sql = "select count(1) from " + tableName;
		LOG.debug("Running: " + sql);
		res = stmt.executeQuery(sql);
		while (res.next()) {
			LOG.debug(res.getString(1));
		}
	}
}