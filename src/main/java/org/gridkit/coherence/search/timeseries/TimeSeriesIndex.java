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

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.tangosol.net.BackingMapContext;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.MapIndex;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.AbstractExtractor;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class TimeSeriesIndex implements MapIndex {

	private ConcurrentMap<Object, NavigableMap<Object, Object>> index = new ConcurrentHashMap<Object, NavigableMap<Object,Object>>();
	
	private static Field ABSTRACT_EXTRACTOR_TARGET;
	static {
		try {
			Field f = AbstractExtractor.class.getDeclaredField("m_nTarget");			
			f.setAccessible(true);
			ABSTRACT_EXTRACTOR_TARGET = f;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private ValueExtractor seriesKeyExtractor;
	private ValueExtractor timestampExtractor;
	private Comparator<?>  timestampComparator;
	
	private EntryExtractor currSeriesKey;
	private EntryExtractor origSeriesKey;
	private EntryExtractor currTimestamp;
	private EntryExtractor origTimestamp;
	
	private boolean constKey = false;
	private boolean constTs = false;
	
	@SuppressWarnings("unused")
	private BackingMapContext bmc;
	
	public TimeSeriesIndex(TimeSeriesExtractor timeSeriesExtractor, BackingMapContext bmc) {
		seriesKeyExtractor = timeSeriesExtractor.getSeriesKeyExtractor();
		timestampExtractor = timeSeriesExtractor.getTimestampExtractor();
		timestampComparator = timeSeriesExtractor.getTimestampComparator();
		
		currSeriesKey = initValueExtractor(bmc, seriesKeyExtractor);
		currTimestamp = initValueExtractor(bmc, timestampExtractor);
		
		if (isKeyExtractor(seriesKeyExtractor)) {
			constKey = true;
			origSeriesKey = currSeriesKey;
		}
		else {
			origSeriesKey = initOriginalValueExtractor(bmc, seriesKeyExtractor);
		}
		
		if (isKeyExtractor(timestampExtractor)) {
			constTs = true;
			origTimestamp = currTimestamp;
		}
		else {
			origTimestamp = initOriginalValueExtractor(bmc, timestampExtractor);
		}
		
		this.bmc = bmc; 
	}

	@Override
	public ValueExtractor getValueExtractor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOrdered() {
		return false;
	}

	@Override
	public boolean isPartial() {
		return false;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Map getIndexContents() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(Object key) {
		throw new UnsupportedOperationException();
	}
	
	public Set<Object> getSeriesKeys() {
		return index.keySet();
	}
	
	public NavigableMap<Object, Object> getSeries(Object sKey) {
		NavigableMap<Object, Object> line = getLine(sKey);
		if (line != null) {
			synchronized (line) {
				return new TreeMap<Object, Object>(line);
			}
		}
		else {
			return null;
		}
	}

	public Object get(Object sKey, Object ts) {
		SortedMap<Object, Object> line = getLine(sKey);
		if (line != null) {
			synchronized(line) {
				return line.get(ts);
			}
		}
		else {
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map.Entry<Object, Object> ceilingEntry(Object sKey, Object ts) {
		NavigableMap<Object, Object> line = getLine(sKey);
		if (line != null) {
			synchronized(line) {
				if (ts == null) {
					Map.Entry<Object, Object> e = line.lastEntry();
					return e == null ? null : new SimpleEntry(e);
				}
				else {
					Map.Entry<Object, Object> e = line.ceilingEntry(ts);
					return e == null ? null : new SimpleEntry(e);
				}
			}
		}
		else {
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map.Entry<Object, Object> floorEntry(Object sKey, Object ts) {
		NavigableMap<Object, Object> line = getLine(sKey);
		if (line != null) {
			synchronized(line) {
				if (ts == null) {
					Map.Entry<Object, Object> e = line.firstEntry(); 
					return e == null ? null : new SimpleEntry(e);
				}
				else {
					Map.Entry<Object, Object> e = line.floorEntry(ts); 
					return e == null ? null : new SimpleEntry(e);
				}
			}
		}
		else {
			return null;
		}
	}
		
	@Override
	@SuppressWarnings("rawtypes")
	public Comparator getComparator() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void insert(Entry entry) {
		Object key = entryKey(entry);		
		Object sKey = currSeriesKey.extractFromEntry((MapTrigger.Entry) entry);		
		Object ts = currTimestamp.extractFromEntry((MapTrigger.Entry) entry);
		addToIndex(sKey, ts, key);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void update(Entry entry) {
		Object key = entryKey(entry);
		
		Object sKeyN = currSeriesKey.extractFromEntry((MapTrigger.Entry) entry);		
		Object tsN = currTimestamp.extractFromEntry((MapTrigger.Entry) entry);
		Object sKeyO = constKey ? null : origSeriesKey.extractFromEntry((MapTrigger.Entry) entry);		
		Object tsO = constTs ? null : origTimestamp.extractFromEntry((MapTrigger.Entry) entry);
		
		if ((constTs || equals(tsN, tsO, timestampComparator)) 
				&& (constKey || equals(sKeyN, sKeyO, null))) {
			// no changes in index
			return;
		}
		else {
			removeFromIndex(sKeyO, tsO, key);
			addToIndex(sKeyN, tsN, key);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void delete(Entry entry) {
		Object key = entryKey(entry);
		Object sKey = origSeriesKey.extractFromEntry((MapTrigger.Entry) entry);		
		Object ts = origTimestamp.extractFromEntry((MapTrigger.Entry) entry);
		removeFromIndex(sKey, ts, key);
	}
	
	private void addToIndex(Object sKey, Object ts, Object key) {
		while(true) {
			SortedMap<Object, Object> line = getLine(sKey);
			synchronized(line) {
				line.put(ts, key);
				if (line == getLine(sKey)) {
					return;
				}
			}					
		}
	}
	
	private void removeFromIndex(Object sKey, Object ts, Object entry) {
		while(true) {
			SortedMap<Object, Object> line = getLine(sKey);
			synchronized(line) {
				line.remove(ts);
				if (line.isEmpty()) {
					if (index.remove(sKey, line)) {
						return;
					}
				}
				else if (line == getLine(sKey)) {
					return;
				}
			}					
		}
	}

	private NavigableMap<Object, Object> getLine(Object sKey) {
		while(true) {
			NavigableMap<Object, Object> line = index.get(sKey);
			if (line == null) {
				line = new TreeMap<Object, Object>();
				index.putIfAbsent(sKey, line);
			}
			else {
				return line;
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean equals(Object a, Object b, Comparator cmp) {
		if (cmp != null) {
			return cmp.compare(a, b) == 0;
		}
		else if (a == null && b == null) {
			return true;
		}
		else if (a == null || b == null) {
			return false;
		}
		else {
			return a.equals(b);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Object entryKey(Entry entry) {
		if (entry instanceof BinaryEntry) {
			return ((BinaryEntry)entry).getBinaryKey();
		}
		else {
			return entry.getKey();
		}
	}
	
	private boolean isKeyExtractor(ValueExtractor e) {
		try {
			if (e instanceof AbstractExtractor) {
				AbstractExtractor ae = (AbstractExtractor)e;
				int target = ABSTRACT_EXTRACTOR_TARGET.getInt(ae);
				return target == AbstractExtractor.KEY;
			}
			else {
				return false;
			}
		} catch (IllegalArgumentException x) {
			return false;
		} catch (IllegalAccessException x) {
			return false;
		}		
	}
	
	private EntryExtractor initValueExtractor(BackingMapContext bmc, final ValueExtractor e) {
		if (e instanceof AbstractExtractor) {
			final AbstractExtractor ae = (AbstractExtractor) e;
			return new EntryExtractor() {
				@Override
				public Object extractFromEntry(com.tangosol.util.MapTrigger.Entry entry) {
					return ae.extractFromEntry(entry);
				}
			};
		}
		else {
			return new EntryExtractor() {
				@Override
				public Object extractFromEntry(com.tangosol.util.MapTrigger.Entry entry) {
					return e.extract(entry.getValue());
				}
			};
		}
	}
	
	private EntryExtractor initOriginalValueExtractor(BackingMapContext bmc, final ValueExtractor e) {
		if (e instanceof AbstractExtractor) {
			final AbstractExtractor ae = (AbstractExtractor) e;
			if (isKeyExtractor(e)) {
				return new EntryExtractor() {
					@Override
					public Object extractFromEntry(com.tangosol.util.MapTrigger.Entry entry) {
						return ae.extractFromEntry(entry);
					}
				};
			}
			else {
				return new EntryExtractor() {
					@Override
					public Object extractFromEntry(com.tangosol.util.MapTrigger.Entry entry) {
						return ae.extractOriginalFromEntry(entry);
					}
				};				
			}
		}
		else {
			return new EntryExtractor() {
				@Override
				public Object extractFromEntry(com.tangosol.util.MapTrigger.Entry entry) {
					return e.extract(entry.getOriginalValue());
				}
			};
		}		
	}
	
	private interface EntryExtractor {		
		public Object extractFromEntry(MapTrigger.Entry entry);		
	}
	
	private static class SimpleEntry<K, V> implements Map.Entry<K, V> {
		private final K key;
		private final V value;
		
		@SuppressWarnings("unused")
		public SimpleEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public SimpleEntry(Map.Entry<K, V> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}
	}
}
