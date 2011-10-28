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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Ignore;

import com.tangosol.util.InvocableMap;
import com.tangosol.util.aggregator.AbstractAggregator;
import com.tangosol.util.extractor.IdentityExtractor;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
@SuppressWarnings("serial")
public class SurafceAggregator extends AbstractAggregator implements Serializable {

	private transient Map<Object, SampleValue> buffer = new HashMap<Object, SampleValue>();
	
	private long timestamp;
	
	public SurafceAggregator(long timestamp) {
		super(IdentityExtractor.INSTANCE);
		this.timestamp = timestamp;
	}

	@Override
	protected void init(boolean second) {
	}

	protected void processEntry(InvocableMap.Entry entry) {
		process(entry, false);
	}	
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void process(Object value, boolean second) {
		if (second) {
			buffer.putAll((Map<Object, SampleValue>) value);
		}
		else {
			Entry e = (Entry) value;
			SampleKey key = (SampleKey) e.getKey();
			SampleValue val = (SampleValue) e.getValue();
			
			if (val.getTimestamp() <= timestamp) {
				Object skey = key.getSerieKey();
				SampleValue other = buffer.get(skey);
				if (other == null || val.getTimestamp() > other.getTimestamp()) {
					buffer.put(skey, val);
				}
			}
		}		
	}

	@Override
	protected Object finalizeResult(boolean second) {
		if (second) {
			List<Object> result = new ArrayList<Object>();
			for(SampleValue v : buffer.values()) {
				result.add(v.getValue());
			}
			return result;
		}
		else {
			return buffer;
		}
	}
}
