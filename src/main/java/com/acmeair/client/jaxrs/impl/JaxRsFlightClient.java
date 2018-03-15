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

package com.acmeair.client.jaxrs.impl;

import com.acmeair.client.FlightClient;
import com.acmeair.client.cdi.ClientType;
import com.acmeair.client.jaxrs.JaxRsClient;

import javax.annotation.PostConstruct;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ClientType("jaxrs")
public class JaxRsFlightClient extends JaxRsClient implements FlightClient {

  private static WebTarget flightTarget;

  static {
    System.out.println("Using JAXRSFlightClient");
    System.out.println("SECURE_SERVICE_CALLS: " + SECURE_SERVICE_CALLS);
  }

  /**
   * Init webtarget.
   */
  @PostConstruct
  public void init() {
    if (flightTarget == null) {
      Client flightClient = ClientBuilder.newClient();
      flightClient.property("http.maxConnections", Integer.valueOf(50));
      flightClient.property("thread.safe.client", Boolean.valueOf(true));
      flightTarget = flightClient.target("http://" + FLIGHT_SERVICE_LOC + GET_REWARD_PATH);
    }
  }

  /**
   * Call flight service to get miles value.
   */
  public String getRewardMiles(String customerId, String flightSegId, boolean add) {

    Form form = new Form("flightSegment", flightSegId);
    Builder builder = createInvocationBuilder(flightTarget, form, customerId, GET_REWARD_PATH);
    Response res = builder.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), 
        Response.class);

    JsonObject jsonObject = res.readEntity(JsonObject.class);
    Long milesLong = jsonObject.getJsonNumber("miles").longValue();
    String miles = milesLong.toString();

    if (!add) {
      miles = ((Integer) (Integer.parseInt(miles) * -1)).toString();
    }

    return miles;
  }

}
