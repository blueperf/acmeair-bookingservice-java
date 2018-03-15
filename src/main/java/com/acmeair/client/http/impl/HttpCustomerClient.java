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

package com.acmeair.client.http.impl;

import com.acmeair.client.CustomerClient;
import com.acmeair.client.cdi.ClientType;
import com.acmeair.client.http.HttpClient;
import java.net.HttpURLConnection;

@ClientType("http")
public class HttpCustomerClient extends HttpClient implements CustomerClient {

  static {
    System.out.println("Using HTTPCustomerClient");
    System.out.println("SECURE_SERVICE_CALLS: " + SECURE_SERVICE_CALLS);
  }

  /**
   * call customer to update reward miles.
   */
  public void updateTotalMiles(String customerId, String miles) {
    String customerUrl = "http://" + CUSTOMER_SERVICE_LOC + UPDATE_REWARD_PATH + "/" + customerId;
    String customerParameters = "miles=" + miles;

    HttpURLConnection customerConn = 
        createHttpUrlConnection(customerUrl, customerParameters, customerId, UPDATE_REWARD_PATH);
    
    doHttpUrlCall(customerConn, customerParameters);
    
  }
}
