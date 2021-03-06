/*
 * Copyright (C) 2016 Asai Yusuke
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
package com.github.AsaiYusuke.SushMock.ext;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.FluentIterable.*;
import static java.util.Arrays.*;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ExtensionLoader {

	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> loadExtension(String... classNames) {
		return (Map<String, T>) asMap(from(asList(classNames))
				.transform(toClasses()).transform(toExtensions()));
	}

	public static Map<String, Extension> asMap(Iterable<Extension> extensions) {
		return Maps.uniqueIndex(extensions, new Function<Extension, String>() {
			public String apply(Extension extension) {
				return extension.name();
			}
		});
	}

	private static Function<String, Class<? extends Extension>> toClasses() {
		return new Function<String, Class<? extends Extension>>() {
			@SuppressWarnings("unchecked")
			public Class<? extends Extension> apply(String className) {
				try {
					return (Class<? extends Extension>) Class
							.forName(className);
				} catch (ClassNotFoundException e) {
					throw new AssertionError();
				}
			}
		};
	}

	private static Function<Class<? extends Extension>, Extension> toExtensions() {
		return new Function<Class<? extends Extension>, Extension>() {
			public Extension apply(Class<? extends Extension> extensionClass) {
				try {
					checkArgument(
							Extension.class.isAssignableFrom(extensionClass),
							"Extension classes must extends RecordTransformer");
					return extensionClass.newInstance();
				} catch (Exception e) {
					throw new AssertionError();
				}

			}
		};
	}

}
