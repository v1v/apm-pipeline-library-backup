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

import net.sf.json.JSONArray
import groovy.transform.Field

@Field def cache = [:]

/**
  Make a REST API call to Github. It manage to hide the call and the token in the console output.

  githubApiCall(token: token, url: "https://api.github.com/repos/${repoName}/pulls/${prID}")

*/
def call(Map params = [:]){
  def token =  params.containsKey('token') ? params.token : error('makeGithubApiCall: no valid Github token.')
  def url =  params.containsKey('url') ? params.url : error('makeGithubApiCall: no valid Github REST API URL.')

  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'GITHUB_TOKEN', password: "${token}"],
    ]]) {
    def json = "{}"
    try {
      def key = "${token}#${url}"
      if(cache["${key}"] == null){
        log(level: 'DEBUG', text: "githubApiCall: get the JSON from GitHub.")
        json = httpRequest(url: url, headers: ["Authorization": "token ${token}"])
        cache["${key}"] = json
      } else {
        log(level: 'DEBUG', text: "githubApiCall: get the JSON from cache.")
        json = cache["${key}"]
      }
    } catch(err) {
      def obj = [:]
      obj.message = err.toString()
      json = toJSON(obj).toString()
    }
    def ret = toJSON(json)
    if(ret instanceof List && ret.size() == 0){
      log(level: 'WARN', text: "makeGithubApiCall: The REST API call ${url} return 0 elements")
    } else if(ret instanceof Map && ret.containsKey('message')){
      log(level: 'WARN', text: "makeGithubApiCall: The REST API call ${url} return the message : ${ret.message}")
    }
    return ret
  }
}
