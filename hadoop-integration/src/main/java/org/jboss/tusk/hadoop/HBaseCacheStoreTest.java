package org.jboss.tusk.hadoop;

import java.io.IOException;

public class HBaseCacheStoreTest {
   private static EmbeddedServerHelper embedded;

   public static void setup() throws InterruptedException {
         embedded = new EmbeddedServerHelper();
         embedded.setup();
   }

   public static void cleanup() throws IOException {
         EmbeddedServerHelper.teardown();
         embedded = null;
   }

   protected void createCacheStore() throws Exception {
      // This uses the default config settings in HBaseCacheStoreConfig

         // overwrite the ZooKeeper client port with the port from the embedded server
	   //will use the HBaseFacade 1 arg constructor and pass this arg in with the map
//         conf.setHbaseZookeeperPropertyClientPort(Integer
//                  .toString(EmbeddedServerHelper.zooKeeperPort));

   }

}