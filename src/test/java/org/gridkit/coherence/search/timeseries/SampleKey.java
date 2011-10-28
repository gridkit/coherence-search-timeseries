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

import org.junit.Ignore;

import com.tangosol.net.cache.KeyAssociation;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
@SuppressWarnings("serial")
public class SampleKey implements Serializable, KeyAssociation {

	private Object sKey;
	private int ord;
	
	public SampleKey(Object sKey, int ord) {
		this.sKey = sKey;
		this.ord = ord;
	}

	@Override
	public Object getAssociatedKey() {
		return sKey;
	}

	public Object getSerieKey() {
		return sKey;
	}

	public int getOrd() {
		return ord;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ord;
		result = prime * result + ((sKey == null) ? 0 : sKey.hashCode());
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
		SampleKey other = (SampleKey) obj;
		if (ord != other.ord)
			return false;
		if (sKey == null) {
			if (other.sKey != null)
				return false;
		} else if (!sKey.equals(other.sKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(").append(sKey).append(" v").append(ord).append(")");
		return builder.toString();
	}
}
