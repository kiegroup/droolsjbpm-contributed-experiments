/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.test.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kie.test.util.compare.ComparePair;

public class ComparePairTest {

    @Test
    public void collectionsTest() {
        List<String> orig = new ArrayList<String>();
        orig.add("asdfasdfasdfasdf");

        List<String> copy = new ArrayList<String>();
        copy.add(new String(orig.get(0)));

        ComparePair.compareObjectsViaFields(orig, copy);
    }
}
