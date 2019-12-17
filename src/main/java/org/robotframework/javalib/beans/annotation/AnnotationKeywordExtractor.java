/*
 * Copyright 2013 Nokia Solutions and Networks Oyj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robotframework.javalib.beans.annotation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywordOverload;
import org.robotframework.javalib.keyword.DocumentedKeyword;
import org.robotframework.javalib.reflection.IKeywordInvoker;
import org.robotframework.javalib.reflection.KeywordInvoker;

public class AnnotationKeywordExtractor implements IKeywordExtractor<DocumentedKeyword> {
    public Map<String, DocumentedKeyword> extractKeywords(final Object keywordBean) {
        Map<String, DocumentedKeyword> extractedKeywords = new HashMap<String, DocumentedKeyword>();
        Method[] methods = keywordBean.getClass().getMethods();
        // Sorting method list as previous returned order might be incorrect
        Arrays.sort(methods, (m1, m2) -> -Integer.compare(m1.getParameterCount(), m2.getParameterCount()));
        for (final Method method : methods) {
            if (method.isAnnotationPresent(RobotKeyword.class) || method.isAnnotationPresent(RobotKeywordOverload.class)) {
                createOrAddKeyword(extractedKeywords, keywordBean, method);
            }
        }
        return extractedKeywords;
    }

    private void createOrAddKeyword(Map<String, DocumentedKeyword> extractedKeywords, Object keywordBean, Method method) {
        String name = method.getName();
        if(extractedKeywords.containsKey(name)){
            extractedKeywords.put(name, addPolymorphToKeywordDefinition(extractedKeywords.get(name), keywordBean, method));
        }else{
            extractedKeywords.put(name, createKeyword(keywordBean, method));
        }
    }

    IKeywordInvoker createKeywordInvoker(Object keywordBean, Method method) {
        return new KeywordInvoker(keywordBean, method);
    }

    private DocumentedKeyword createKeyword(Object keywordBean, Method method) {
        IKeywordInvoker keywordInvoker = createKeywordInvoker(keywordBean, method);
        return createKeyword(keywordInvoker);
    }

    private DocumentedKeyword createKeyword(final IKeywordInvoker keywordInvoker) {
        return new DocumentedKeyword() {
            public Object execute(List arguments, Map kwargs) {
                return keywordInvoker.invoke(arguments, kwargs);
            }

            public List<String> getArgumentNames() {
                return keywordInvoker.getParameterNames();
            }

            public String getDocumentation() {
                return keywordInvoker.getDocumentation();
            }

            public List<String> getArgumentTypes() {
                return keywordInvoker.getParameterTypes();
            }
        };
    }

    private DocumentedKeyword addPolymorphToKeywordDefinition(final DocumentedKeyword original, final Object keywordBean, final Method method) {
        final DocumentedKeyword other = createKeyword(keywordBean, method);
        final boolean isOverload = method.isAnnotationPresent(RobotKeywordOverload.class);
        if(isOverload && method.isAnnotationPresent(RobotKeyword.class))
            throw new AssertionError("Method definition should not have both RobotKeyword and RobotKeywordOverload annotations");
        final int parameterTypesLength = method.getParameterTypes().length;
        return new DocumentedKeyword() {
            public Object execute(List arguments, Map kwargs) {
                if(parameterTypesLength == arguments.size()){
                    return other.execute(arguments, kwargs);
                }
                return original.execute(arguments, kwargs);
            }

            public List<String> getArgumentNames() {
                if(isOverload){
                    return original.getArgumentNames();
                }
                return other.getArgumentNames();
            }

            public String getDocumentation() {
                if(isOverload){
                    return original.getDocumentation();
                }
                return other.getDocumentation();
            }

            public List<String> getArgumentTypes() {
                if(isOverload){
                    return original.getArgumentTypes();
                }
                return other.getArgumentTypes();
            }
        };
    }
}
