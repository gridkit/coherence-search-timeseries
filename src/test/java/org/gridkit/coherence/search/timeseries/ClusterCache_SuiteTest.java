/**
 * Copyright 2011 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.search.timeseries;

import org.gridkit.vicluster.isolate.Isolate;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.PartitionedService;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@RunWith(Suite.class)
@SuiteClasses({
	BasicFunctional_TestSet.class,
	SimpleAggregation_TestSet.class,
	RandomAggregation_TestSet.class
})
public class ClusterCache_SuiteTest {

	static {
		System.setProperty("tangosol.coherence.cluster", "local-test");
	}

	// TODO - used ChTest API
	static Isolate node = new Isolate("Remote-1", "org.gridkit", "com.tangosol");
	
	@BeforeClass
	public static void init(){
		
		CacheFactory.getCluster().shutdown();
		
		CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("test-cache-config.xml"));
		
		node.start();
		final String config = "test-cache-config.xml"; 
		node.exec(new Runnable() {
			@Override
			public void run() {
				System.setProperty("tangosol.coherence.member", Thread.currentThread().getName());
				System.setProperty("tangosol.coherence.cluster", "local-test");
				
				System.out.println(Thread.currentThread().getName() + " starting Coherence node ...");
				CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory(config));
				CacheFactory.ensureCluster();
				System.out.println(Thread.currentThread().getName() + " Coherence node has started");
			}
		});
		final String cacheName = "distributed-cache";
		node.exec(new Runnable() {
			@Override
			public void run() {
				System.out.println(Thread.currentThread().getName() + " initializing cache [" + cacheName + "] ...");
				CacheFactory.getCache(cacheName);
				System.out.println(Thread.currentThread().getName() + " cache [" + cacheName + "] initialized");
			}
		});
		
		AbstractTimeseriesFunctional_TestSet.testCache = CacheFactory.getCache("distributed-cache");
		AbstractTimeseriesFunctional_TestSet.useAffinity = true;
	}

	@Test
	public void cacheType() {
		Assert.assertTrue(AbstractTimeseriesFunctional_TestSet.testCache.getCacheService() instanceof PartitionedService);
	}	
	
	@AfterClass
	public static void shutdown() {
		node.exec(new Runnable() {
			@Override
			public void run() {
				CacheFactory.shutdown();
			}
		});
		node.stop();
		node = null;
	}
}
