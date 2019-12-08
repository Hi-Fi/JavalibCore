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

package org.robotframework.javalib.keyword;

import java.util.List;
import java.util.Map;

public abstract class PreparableKeyword implements Keyword {
    public Object execute(List arguments, Map kwargs) {
        prepare(arguments);
        try {
            return operate(arguments);
        } finally {
            finish(arguments);
        }
    }

    protected void prepare(List arguments) {}
    protected void finish(List arguments) {}
    protected abstract Object operate(List arguments);
}
