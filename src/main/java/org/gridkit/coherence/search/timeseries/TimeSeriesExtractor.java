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

import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.BackingMapContext;
import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.IndexAwareExtractor;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class TimeSeriesExtractor implements IndexAwareExtractor, PortableObject, Serializable {
	
	private static final long serialVersionUID = 20110913L;

	private ValueExtractor seriesKeyExtractor;
	private ValueExtractor timestampExtractor;
	private Comparator<?>  timestampComparator;
	
	public TimeSeriesExtractor() {
		// required for POF
	}
	
	public TimeSeriesExtractor(ValueExtractor seriesKeyExtractor, ValueExtractor timestampExtractor, Comparator<?> timestampComparator) {
		this.seriesKeyExtractor = seriesKeyExtractor;
		this.timestampExtractor = timestampExtractor;
		this.timestampComparator = timestampComparator;
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

	@Override
	public Object extract(Object paramObject) {
		throw new UnsupportedOperationException("Can only be used for indexing");
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MapIndex createIndex(boolean ordered, Comparator comparator, Map indexes, BackingMapContext bmc) {
		if (indexes.containsKey(this)) {
			return null;
		}
		else {
			TimeSeriesIndex index = new TimeSeriesIndex(this, bmc);
			indexes.put(this, index);
			return index;
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public MapIndex destroyIndex(Map indexes) {
		return (MapIndex) indexes.remove(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((seriesKeyExtractor == null) ? 0 : seriesKeyExtractor
						.hashCode());
		result = prime
				* result
				+ ((timestampComparator == null) ? 0 : timestampComparator
						.hashCode());
		result = prime
				* result
				+ ((timestampExtractor == null) ? 0 : timestampExtractor
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeSeriesExtractor other = (TimeSeriesExtractor) obj;
		if (seriesKeyExtractor == null) {
			if (other.seriesKeyExtractor != null)
				return false;
		} else if (!seriesKeyExtractor.equals(other.seriesKeyExtractor))
			return false;
		if (timestampComparator == null) {
			if (other.timestampComparator != null)
				return false;
		} else if (!timestampComparator.equals(other.timestampComparator))
			return false;
		if (timestampExtractor == null) {
			if (other.timestampExtractor != null)
				return false;
		} else if (!timestampExtractor.equals(other.timestampExtractor))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TimeSeriesExtractor [seriesKeyExtractor=")
				.append(seriesKeyExtractor).append(", timestampExtractor=")
				.append(timestampExtractor).append("]");
		return builder.toString();
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		int n = 1;
		
		this.seriesKeyExtractor = (ValueExtractor) in.readObject(n++);
		this.timestampExtractor = (ValueExtractor) in.readObject(n++);
		this.timestampComparator = (Comparator<?>) in.readObject(n++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		int n = 1;
		
		out.writeObject(n++, this.seriesKeyExtractor);
		out.writeObject(n++, this.timestampExtractor);
		out.writeObject(n++, this.timestampComparator);
	}
}
