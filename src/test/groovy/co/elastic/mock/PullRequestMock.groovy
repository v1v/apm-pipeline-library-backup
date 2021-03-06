// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package co.elastic.mock

/**
 * Mock pullrequest class from pipeline-github plugin.
 */
class PullRequestMock implements Serializable {

  String description = ''

  static int ERROR = -1

  public PullRequestMock() { }

  public Map comment(String description) {
    if (description.equals('error')) {
      throw new Exception('org.eclipse.egit.github.core.client.RequestException: Not Found (404)')
    }
    this.description = description
    return [ id: 42 ]
  }

  public void editComment(int id, String description) {
    if (id == ERROR) {
      throw new Exception('org.eclipse.egit.github.core.client.RequestException: Not Found (404)')
    }
    this.description = description
  }
}
