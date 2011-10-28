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

import java.util.Collection;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tangosol.util.aggregator.DistinctValues;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AlwaysFilter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class RandomAggregation_TestSet extends AbstractTimeseriesFunctional_TestSet {

	TimeSeriesHelper<String, SampleValue, Long> helper = new TimeSeriesHelper<String, SampleValue, Long>(
			new KeyExtractor("getSerieKey"), 
			new ReflectionExtractor("getTimestamp"));
	
	{
		helper.setAffinityEnabled(useAffinity);
	}
	
	@Before
	public void cleanUp() {
//		helper.destroyIndex(testCache);
		testCache.clear();
		helper.createIndex(testCache);
	}
	
	int counter = 0;
	Random rnd = new Random(0);
	
	private void randomPut() {
		int ord = ++counter;
		String key = String.valueOf((char)('A' + rnd.nextInt(16)));
		SampleKey ckey = new SampleKey(key, ord);
		long timestamp = rnd.nextInt(950) + 50;
		SampleValue val = new SampleValue(timestamp, key + "@" + timestamp);
		testCache.put(ckey, val);
	}

	private void randomRemove() {
		String key = String.valueOf('A' + rnd.nextInt(16));
		long timestamp = rnd.nextInt(950) + 50;
		testCache.keySet(helper.floor(key, timestamp)).clear();
		testCache.keySet(helper.ceiling(key, timestamp)).clear();
	}

	@Test
	public void step1() {
		testCycle(1000);
	}

	@Test
	public void step2() {
		rnd.setSeed(1);
		testCycle(1000);
	}

	@Test
	public void step3() {
		rnd.setSeed(2);
		testCycle(1000);
	}

	@Test
	public void step4() {
		rnd.setSeed(3);
		testCycle(1000);
	}

	@Test
	public void step5() {
		rnd.setSeed(4);
		testCycle(1000);
	}
	
	public void testCycle(int iterations) {
		for(int i = 0; i != iterations; ++i) {
			randomPut();
			if (i % 3 == 1 && testCache.size() > 100) {
				randomRemove();
			}
			for(int j = 0; j != 10; ++j) {
				testFloorSnapshot(rnd.nextInt(1000));
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void testFloorSnapshot(long ts) {

		Collection<Object> result = (Collection<Object>) testCache.aggregate(AlwaysFilter.INSTANCE, new SurafceAggregator(ts));
		TreeSet<Object> sq1 = new TreeSet<Object>(result);
		
		result = (Collection<Object>) testCache.aggregate(helper.floor(ts), new DistinctValues("getValue"));
		TreeSet<Object> sq2 = new TreeSet<Object>(result);

		if (!sq1.toString().equals(sq2.toString())) {
			Assert.assertEquals(sq1.toString(), sq2.toString());
		}			
	}

//	@SuppressWarnings("unchecked")
//	public void testCeilingSnapshot(long ts) {
//		
//		Collection<Object> result = (Collection<Object>) testCache.aggregate(AlwaysFilter.INSTANCE, new SurafceAggregator(ts));
//		TreeSet<Object> sq1 = new TreeSet<Object>(result);
//		
//		result = (Collection<Object>) testCache.aggregate(helper.ceiling(ts), new DistinctValues("getValue"));
//		TreeSet<Object> sq2 = new TreeSet<Object>(result);
//		
//		if (!sq1.toString().equals(sq2.toString())) {
//			Assert.assertEquals(sq1.toString(), sq2.toString());
//		}			
//	}	
}
