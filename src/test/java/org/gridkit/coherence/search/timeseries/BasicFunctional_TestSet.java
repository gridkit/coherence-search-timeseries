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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class BasicFunctional_TestSet extends AbstractTimeseriesFunctional_TestSet {

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
	
	private void fixture1() {
		testCache.put(key("A", 1), version("A-10", 10));
		testCache.put(key("A", 2), version("A-12", 12));
		testCache.put(key("A", 3), version("A-20", 20));
		testCache.put(key("B", 1), version("B-8", 8));
		testCache.put(key("B", 2), version("B-11", 11));
		testCache.put(key("B", 3), version("B-13", 13));
		testCache.put(key("B", 4), version("B-21", 21));
	}

	private void fixture2() {
		testCache.put(key("a", 1), version("A-10", 10));
		testCache.put(key("a", 2), version("A-12", 12));
		testCache.put(key("a", 3), version("A-20", 20));
		testCache.put(key("b", 1), version("B-8", 8));
		testCache.put(key("b", 2), version("B-11", 11));
		testCache.put(key("b", 3), version("B-13", 13));
		testCache.put(key("b", 4), version("B-21", 21));
	}
	
	@Test
	public void floorSize() {
		fixture1();
		
		Assert.assertEquals(0, testCache.keySet(helper.floor(7l)).size());
		Assert.assertEquals(1, testCache.keySet(helper.floor(8l)).size());
		Assert.assertEquals(1, testCache.keySet(helper.floor(9l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(10l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(15l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(25l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(null)).size());
	}

	@Test
	public void ceilingSize() {
		fixture1();
		
		Assert.assertEquals(0, testCache.keySet(helper.ceiling(25l)).size());
		Assert.assertEquals(1, testCache.keySet(helper.ceiling(21l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(20l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(15l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(13l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(7l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(null)).size());
	}

	@Test
	public void floorSize_2() {
		fixture2();
		
		Assert.assertEquals(0, testCache.keySet(helper.floor(7l)).size());
		Assert.assertEquals(1, testCache.keySet(helper.floor(8l)).size());
		Assert.assertEquals(1, testCache.keySet(helper.floor(9l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(10l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(15l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(25l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(null)).size());
	}

	@Test
	public void ceilingSize_2() {
		fixture2();
		
		Assert.assertEquals(0, testCache.keySet(helper.ceiling(25l)).size());
		Assert.assertEquals(1, testCache.keySet(helper.ceiling(21l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(20l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(15l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(13l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(7l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(null)).size());
	}

	@Test
	public void gettingElements() {
		fixture1();
		
		Assert.assertEquals(0, testCache.keySet(helper.ceiling("A", 21l)).size());
	}	
}
