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

public interface FlightClient {

  // Default to amalgam8 default
  static final String FLIGHT_SERVICE_LOC = 
      ((System.getenv("FLIGHT_SERVICE") == null) ? "localhost:6379/flight"
      : System.getenv("FLIGHT_SERVICE"));
  
  static final String GET_REWARD_PATH = "/getrewardmiles";

  public abstract String getRewardMiles(String customerId, String flightSegId, boolean add);
}
