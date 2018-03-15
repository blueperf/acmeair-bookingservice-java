/*******************************************************************************
* Copyright (c) 2017 IBM Corp.
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
*******************************************************************************/

package com.acmeair.client;

public interface CustomerClient {

  // default to amalgam8
  static final String CUSTOMER_SERVICE_LOC = 
      ((System.getenv("CUSTOMER_SERVICE") == null) ? "localhost:6379/customer"
      : System.getenv("CUSTOMER_SERVICE"));
  static final String UPDATE_REWARD_PATH = "/updateCustomerTotalMiles";

  public abstract void updateTotalMiles(String customerId, String miles);
}
