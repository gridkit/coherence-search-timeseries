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

import java.util.Collections;
import java.util.Comparator;

import com.tangosol.util.Filter;
import com.tangosol.util.QueryMap;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.filter.KeyAssociatedFilter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class TimeSeriesHelper<K, V, T> {

	private ValueExtractor seriesKeyExtractor;
	private ValueExtractor affinityExtractor;;
	private ValueExtractor timestampExtractor;
	private Comparator<?> timestampComparator;
	
	private boolean affinityEnabled = true;
	
	public TimeSeriesHelper(ValueExtractor seriesKeyExtractor, ValueExtractor timestampExtractor) {
		this(seriesKeyExtractor, timestampExtractor, null);
	}

	public TimeSeriesHelper(ValueExtractor seriesKeyExtractor, ValueExtractor timestampExtractor, Comparator<?> timestampComparator) {
		this.seriesKeyExtractor = seriesKeyExtractor;
		this.affinityExtractor = null;
		this.timestampExtractor = timestampExtractor;
		this.timestampComparator = timestampComparator;
	}
	
	public ValueExtractor getAffinityExtractor() {
		return affinityExtractor;
	}

	public void setAffinityExtractor(ValueExtractor affinityExtractor) {
		this.affinityExtractor = affinityExtractor;
	}

	public boolean getAffinityEnabled() {
		return affinityEnabled;
	}

	public void setAffinityEnabled(boolean affinityEnabled) {
		this.affinityEnabled = affinityEnabled;
	}

	public ValueExtractor getSeriesKeyExtractor() {
		return seriesKeyExtractor;
	}

	public ValueExtractor getTimestampExtractor() {
		return timestampExtractor;
	}

	public Comparator<?> getTimestampComparator() {
		return timestampComparator;
	}

	public Filter floor(K key, T ts) {
		Object akey = affinityExtractor == null ? key : affinityExtractor.extract(key);
		TimeSurfaceFilter f = new TimeSurfaceFilter(seriesKeyExtractor, timestampExtractor, timestampComparator, ts);
		f.setSeriesSet(Collections.singleton((Object)key));
		if (affinityEnabled) {
			KeyAssociatedFilter kf = new KeyAssociatedFilter(f, akey);
			return kf;
		}
		else {
			return f;
		}
	}

	public Filter floor(T ts) {
		Filter f = new TimeSurfaceFilter(seriesKeyExtractor, timestampExtractor, timestampComparator, ts);
		return f;
	}

	public Filter ceiling(T ts) {
		TimeSurfaceFilter f = new TimeSurfaceFilter(seriesKeyExtractor, timestampExtractor, timestampComparator, ts);
		f.setAbove(true);
		return f;
	}

	public Filter ceiling(K key, T ts) {
		Object akey = affinityExtractor == null ? key : affinityExtractor.extract(key);
		TimeSurfaceFilter f = new TimeSurfaceFilter(seriesKeyExtractor, timestampExtractor, timestampComparator, ts);
		f.setSeriesSet(Collections.singleton((Object)key));
		f.setAbove(true);
		if (affinityEnabled) {
			KeyAssociatedFilter kf = new KeyAssociatedFilter(f, akey);
			return kf;
		}
		else {
			return f;
		}
	}	
	
	public void createIndex(QueryMap cache) {
		cache.addIndex(new TimeSeriesExtractor(seriesKeyExtractor, timestampExtractor, timestampComparator), false, null);
	}

	public void destroyIndex(QueryMap cache) {
		cache.removeIndex(new TimeSeriesExtractor(seriesKeyExtractor, timestampExtractor, timestampComparator));
	}
}
