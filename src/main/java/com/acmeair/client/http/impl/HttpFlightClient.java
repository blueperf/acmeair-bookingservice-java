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

import com.acmeair.client.FlightClient;
import com.acmeair.client.cdi.ClientType;
import com.acmeair.client.http.HttpClient;

import java.io.StringReader;
import java.net.HttpURLConnection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

@ClientType("http")
public class HttpFlightClient extends HttpClient implements FlightClient {

  private static final JsonReaderFactory factory = Json.createReaderFactory(null);

  static {
    System.out.println("Using HTTPFlightClient");
    System.out.println("SECURE_SERVICE_CALLS: " + SECURE_SERVICE_CALLS);
  }

  /**
   * See 
   * com.acmeair.client.FlightClient#getRewardMiles(java.lang.String, java.lang.String, boolean)
   */
  public String getRewardMiles(String customerId, String flightSegId, boolean add) {
    // Set maxConnections - this seems to help with keepalives/running out of
    // sockets with a high load.
    if (System.getProperty("http.maxConnections") == null) {
      System.setProperty("http.maxConnections", "50");
    }

    String flightUrl = "http://" + FLIGHT_SERVICE_LOC + GET_REWARD_PATH;
    String flightParameters = "flightSegment=" + flightSegId;

    HttpURLConnection flightConn = 
        createHttpUrlConnection(flightUrl, flightParameters, customerId, GET_REWARD_PATH);
    String output = doHttpUrlCall(flightConn, flightParameters);

    JsonReader jsonReader = factory.createReader(new StringReader(output));
    JsonObject milesObject = jsonReader.readObject();
    jsonReader.close();

    Long milesLong = milesObject.getJsonNumber("miles").longValue();
    String miles = milesLong.toString();

    if (!add) {
      miles = ((Integer) (Integer.parseInt(miles) * -1)).toString();
    }

    return miles;
  }
}
