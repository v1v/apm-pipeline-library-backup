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

import hudson.model.Cause

/**
 * Mock IssueCommentCause class. It's required to keep the IssueCommentCause class name!.
 */
class IssueCommentCause extends Cause {
  private final String userLogin
  private final String comment

  public IssueCommentCause(final String userLogin, final String comment) {
    this.userLogin = userLogin
    this.comment = comment
  }

  public String getUserLogin() {
    return userLogin
  }

  public String getComment() {
    return comment
  }

  public String getShortDescription(){
    return String.format("%s commented: %s", userLogin, comment);
  }
}
