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

/**
  Grab a secret from the vault, define the environment variables which have been
  passed as parameters and mask the secrets

  withSecretVault(secret: 'secret', data: [[var: 'myuser', env: 'my_user_env'],[var: 'mypass', env: 'my_password_env']]){
    //block
  }

  // Deprecated
  withSecretVault(secret: 'secret', user_var_name: 'my_user_env', pass_var_name: 'my_password_env'){
    //block
  }
  // Replaced with
  withSecretVault(secret: 'secret', data: [[var: 'user_var_name', env: 'my_user_env',[var: 'pass_var_name'. env: 'my_password_env']]){
    //block
  }
*/
def call(Map params = [:], Closure body) {
  if (params.data) {
    withMultipleValues(params) {
      body()
    }
  } else {
    withSingleValue(params) {
      body()
    }
  }
}

def withMultipleValues(Map params = [:], Closure body) {
  def secret = params.containsKey('secret') ? params.secret : error('withSecretVault: secret is a mandatory parameter.')
  body()
}

def withSingleValue(Map params = [:], Closure body) {
  def secret = params.containsKey('secret') ? params.secret : error('withSecretVault: secret is a mandatory parameter.')
  def user_variable = params.containsKey('user_var_name') ? params.user_var_name : error('withSecretVault: user_var_name is a mandatory parameter.')
  def pass_variable = params.containsKey('pass_var_name') ? params.pass_var_name : error('withSecretVault: pass_var_name is a mandatory parameter.')

  def props = getVaultSecret(secret: secret)
  if(props?.errors){
    error 'withSecretVault: Unable to get credentials from the vault: ' + props.errors.toString()
  }

  def user = props?.data?.user
  def password = props?.data?.password

  if(user == null || password == null){
    error 'withSecretVault: was not possible to get authentication info'
  }

  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
        [var: "${user_variable}", password: user],
        [var: "${pass_variable}", password: password],
  ]]) {
    withEnv(["${user_variable}=${user}", "${pass_variable}=${password}"]) {
      body()
    }
  }
}
