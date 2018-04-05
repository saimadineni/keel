/*
 * Copyright 2018 Netflix, Inc.
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
package com.netflix.spinnaker.keel.clouddriver

import com.netflix.spinnaker.keel.clouddriver.model.Network
import com.netflix.spinnaker.keel.clouddriver.model.SecurityGroupSummary

interface CloudDriverCache {
  fun securityGroupSummaryBy(account: String, region: String, id: String): SecurityGroupSummary
  fun networkBy(id: String): Network
  fun networkBy(name: String?, account: String, region: String): Network
  fun availabilityZonesBy(account: String, vpcId: String, region: String): Collection<String>
}

class ResourceNotFound(message: String) : RuntimeException(message)