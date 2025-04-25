/*
 * Copyright 2004 Outerthought bvba and Schaubroeck nv
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
package org.outerj.daisy.htmlcleaner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This is a thread-safe, reusable object containing the configuration for the
 * HtmlCleaner. Instances of this object can be obtained from the
 * {@link HtmlCleanerFactory}. A concrete HtmlCleaner can be obtained by using
 * the method {@link #newHtmlCleaner()}.
 */
public class HtmlCleanerTemplate
{
	int maxLineWidth = 80;
	HashMap<String, OutputElementDescriptor> outputElementDescriptors = new HashMap<String, OutputElementDescriptor>();
	HashSet<String> allowedSpanClasses = new HashSet<String>();
	HashSet<String> allowedDivClasses = new HashSet<String>();
	HashSet<String> allowedParaClasses = new HashSet<String>();
	HashSet<String> allowedPreClasses = new HashSet<String>();
	HashSet<String> dropDivClasses = new HashSet<String>();
	HashMap<String, ElementDescriptor> descriptors = new HashMap<String, ElementDescriptor>();
	String imgAlternateSrcAttr;
	String linkAlternateHrefAttr;
	private boolean initialised = false;

	HtmlCleanerTemplate()
	{
		// package-private constructor
	}

	void addOutputElement(String tagName, int beforeOpen, int afterOpen, int beforeClose, int afterClose,
	                boolean inline)
	{
		if (initialised)
			throw new IllegalStateException();
		if (tagName == null)
			throw new NullPointerException();
		OutputElementDescriptor descriptor = new OutputElementDescriptor(beforeOpen, afterOpen, beforeClose,
		                afterClose, inline);
		outputElementDescriptors.put(tagName, descriptor);
	}

	void setMaxLineWidth(int lineWidth)
	{
		if (initialised)
			throw new IllegalStateException();
		this.maxLineWidth = lineWidth;
	}

	void addAllowedSpanClass(String clazz)
	{
		if (initialised)
			throw new IllegalStateException();
		if (clazz == null)
			throw new NullPointerException();
		allowedSpanClasses.add(clazz);
	}

	void addAllowedDivClass(String clazz)
	{
		if (initialised)
			throw new IllegalStateException();
		if (clazz == null)
			throw new NullPointerException();
		allowedDivClasses.add(clazz);
	}

	void addDropDivClass(String clazz)
	{
		if (initialised)
			throw new IllegalStateException();
		if (clazz == null)
			throw new NullPointerException();
		dropDivClasses.add(clazz);
	}

	void addAllowedParaClass(String clazz)
	{
		if (initialised)
			throw new IllegalStateException();
		if (clazz == null)
			throw new NullPointerException();
		allowedParaClasses.add(clazz);
	}

	void addAllowedPreClass(String clazz)
	{
		if (initialised)
			throw new IllegalStateException();
		if (clazz == null)
			throw new NullPointerException();
		allowedPreClasses.add(clazz);
	}

	void addAllowedElement(String tagName, String[] attributes)
	{
		if (initialised)
			throw new IllegalStateException();
		if (tagName == null)
			throw new NullPointerException();

		ElementDescriptor descriptor = new ElementDescriptor(tagName);
		for (String attribute : attributes) {
			descriptor.addAttribute(attribute);
		}

		descriptors.put(tagName, descriptor);
	}

	void initialize() throws Exception
	{
		if (initialised)
			throw new IllegalStateException();
		// build our descriptor model:
		// - retrieve the one for XHTML (so that we have information about
		// content models)
		// - filter it to only contain the elements the user configured
		HashMap<String, ElementDescriptor> full = new XhtmlDescriptorBuilder().build();
		relax(full);
		narrow(full, descriptors);
		descriptors = full;
		initialised = true;
	}

	/**
	 * Modifies the full map so that it only contains elements and attributes
	 * from the subset, but retains the child element information.
	 */
	private void narrow(HashMap<String, ElementDescriptor> full, HashMap<String, ElementDescriptor> subset)
	{
		String[] fullKeys = full.keySet().toArray(new String[full.size()]);
		for (int i = 0; i < fullKeys.length; i++) {
			if (!subset.containsKey(fullKeys[i]))
				full.remove(fullKeys[i]);
		}

		Iterator<ElementDescriptor> descriptorIt = full.values().iterator();
		while (descriptorIt.hasNext()) {
			ElementDescriptor elementDescriptor = descriptorIt.next();
			HashSet<String> children = elementDescriptor.getChildren();
			String[] childNames = children.toArray(new String[children.size()]);
			HashSet<String> newChilds = new HashSet<String>();
			for (String childName : childNames)
				if (subset.containsKey(childName))
					newChilds.add(childName);

			elementDescriptor.setChildren(newChilds);
			elementDescriptor.setAttributes(subset.get(elementDescriptor.getName())
			                .getAttributes());
		}
	}

	private void relax(HashMap<String, ElementDescriptor> descriptors)
	{
		// HTML doesn't allow ul's to be nested directly, but that's what all
		// these HTML
		// editors create, so relax that restriction a bit
		ElementDescriptor ulDescriptor = descriptors.get("ul");
		if (ulDescriptor != null) {
			ulDescriptor.getChildren().add("ul");
			ulDescriptor.getChildren().add("ol");
		}

		ElementDescriptor olDescriptor = descriptors.get("ol");
		if (olDescriptor != null) {
			olDescriptor.getChildren().add("ul");
			olDescriptor.getChildren().add("ol");
		}
	}

	public HtmlCleaner newHtmlCleaner()
	{
		return new HtmlCleaner(this);
	}

	void setImgAlternateSrcAttr(String name)
	{
		this.imgAlternateSrcAttr = name;
	}

	public void setLinkAlternateHrefAttr(String linkAlternateHrefAttr)
	{
		this.linkAlternateHrefAttr = linkAlternateHrefAttr;
	}
}
