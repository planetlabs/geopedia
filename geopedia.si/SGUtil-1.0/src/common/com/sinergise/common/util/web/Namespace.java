/*
 * Copyright 2006 Robert Hanson <iamroberthanson AT gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sinergise.common.util.web;

public class Namespace {
	private String prefix;
	private String uri;
	
	public Namespace(final String prefix, final String uri) {
		this.prefix = prefix;
		this.uri = uri;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(final String uri) {
		this.uri = uri;
	}
}
