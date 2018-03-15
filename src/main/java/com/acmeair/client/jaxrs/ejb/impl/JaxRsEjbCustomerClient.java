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

package com.acmeair.client.jaxrs.ejb.impl;

import com.acmeair.client.CustomerClient;
import com.acmeair.client.cdi.ClientType;
import com.acmeair.client.jaxrs.JaxRsClient;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.json.JsonObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ClientType("jaxrs-ejb")
@Stateless
public class JaxRsEjbCustomerClient extends JaxRsClient implements CustomerClient {

  private WebTarget customerTargetBase;

  static {
    System.out.println("Using JAXRSEJBCustomerClient");
    System.out.println("SECURE_SERVICE_CALLS: " + SECURE_SERVICE_CALLS);
  }

  @PostConstruct
  public void init() {
    Client customerClient = ClientBuilder.newClient();
    customerTargetBase = customerClient.target("http://" + CUSTOMER_SERVICE_LOC + UPDATE_REWARD_PATH);
  }

  /**
   * Update reward miles.
   */
  public void updateTotalMiles(String customerId, String miles) {
    Form form = new Form("miles", miles);
    WebTarget customerClientWebTargetFinal = customerTargetBase.path(customerId);
    Builder builder = 
        createInvocationBuilder(customerClientWebTargetFinal, form, customerId, UPDATE_REWARD_PATH);

    builder.accept(MediaType.TEXT_PLAIN);
    Response res = builder.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), 
        Response.class);

    res.readEntity(JsonObject.class);
  }
}
