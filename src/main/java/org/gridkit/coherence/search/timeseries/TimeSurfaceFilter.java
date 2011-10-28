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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.filter.IndexAwareFilter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class TimeSurfaceFilter implements IndexAwareFilter {

	private ValueExtractor seriesKeyExtractor;
	private ValueExtractor timestampExtractor;
	private Comparator<?> timestampComparator;
	private Collection<Object> seriesSet;
	
	private Object timestamp;
	private boolean above;
	
	public TimeSurfaceFilter() {
		// required 
	}
	
	public TimeSurfaceFilter(ValueExtractor seriesKeyExtractor, ValueExtractor timestampExtractor, Comparator<?> timestampComparator, Object timestamp) {
		this.timestampExtractor = timestampExtractor;
		this.seriesKeyExtractor = seriesKeyExtractor;
		this.timestampComparator = timestampComparator;
		this.timestamp = timestamp;
	}

	public boolean isAbove() {
		return above;
	}

	public void setAbove(boolean above) {
		this.above = above;
	}
	
	public Collection<Object> getSeriesSet() {
		return seriesSet;
	}

	public void setSeriesSet(Collection<Object> seriesSet) {
		this.seriesSet = seriesSet;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean evaluateEntry(Entry paramEntry) {
		throw new UnsupportedOperationException("Cannot work without index");
	}

	@Override
	public boolean evaluate(Object paramObject) {
		throw new UnsupportedOperationException("Cannot work without index");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public int calculateEffectiveness(Map mapIndex, Set candidates) {
		// cannot work without index anyway
		return 1;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Filter applyIndex(Map indexes, Set candidates) {
		TimeSeriesIndex index = getIndex(indexes);
		
		Set<Object> retainSet = new HashSet<Object>();
		Collection<Object> series = this.seriesSet != null ? this.seriesSet : index.getSeriesKeys();
		//TODO if series much large than candidates, use candidates to limit series		
		for(Object sKey: series) {
			Map.Entry<Object, Object> entry = above 
				? index.ceilingEntry(sKey, timestamp)
				: index.floorEntry(sKey, timestamp);
			if (entry != null && candidates.contains(entry.getValue())) {
				retainSet.add(entry.getValue());
			}
		}
		
		candidates.retainAll(retainSet);
		
		// no further filtering required
		return null;
	}

	@SuppressWarnings("rawtypes")
	private TimeSeriesIndex getIndex(Map indexes) {
		TimeSeriesExtractor e = new TimeSeriesExtractor(seriesKeyExtractor, timestampExtractor, timestampComparator);
		TimeSeriesIndex ti = (TimeSeriesIndex) indexes.get(e);
		if (ti == null) {
			throw new IllegalArgumentException("TimeSeries index requeired, extractor spec " + e);
		}
		return ti;
	}
}
