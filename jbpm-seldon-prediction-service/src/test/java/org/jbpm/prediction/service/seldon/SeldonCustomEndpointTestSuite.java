/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.prediction.service.seldon;

import org.jbpm.prediction.service.seldon.examples.ExampleSeldonPredictionService;

public class SeldonCustomEndpointTestSuite extends SeldonBaseTestSuite
{

   public SeldonCustomEndpointTestSuite() {
        System.setProperty("org.jbpm.task.prediction.service", ExampleSeldonPredictionService.IDENTIFIER);
        System.setProperty("org.jbpm.task.prediction.service.seldon.url", "http://localhost:5000");
        System.setProperty("org.jbpm.task.prediction.service.seldon.endpoint", "api/v1.0/predictions");
    }

}