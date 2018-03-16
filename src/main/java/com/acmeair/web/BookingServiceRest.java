/*******************************************************************************
* Copyright (c) 2013 IBM Corp.
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

package com.acmeair.web;

import com.acmeair.securityutils.SecurityUtils;
import com.acmeair.service.BookingService;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.metrics.annotation.Timed;

@Path("/")
public class BookingServiceRest {

  @Inject
  BookingService bs;

  @Inject
  private SecurityUtils secUtils;

  @Inject
  private RewardTracker rewardTracker;

  private static final Logger logger = Logger.getLogger(BookingServiceRest.class.getName());
  private static final JsonReaderFactory factory = Json.createReaderFactory(null);  
  
  /**
   * Book flights.
   */
  @POST
  @Consumes({ "application/x-www-form-urlencoded" })
  @Path("/bookflights")
  @Produces("text/plain")
  @Timed(name = "com.acmeair.web.BookingServiceRest.bookFlights",tags = "app=bookingservice-java") 
  public /* BookingInfo */ Response bookFlights(@FormParam("userid") String userid,
      @FormParam("toFlightId") String toFlightId, 
      @FormParam("toFlightSegId") String toFlightSegId,
      @FormParam("retFlightId") String retFlightId, 
      @FormParam("retFlightSegId") String retFlightSegId,
      @FormParam("oneWayFlight") boolean oneWay, 
      @CookieParam("jwt_token") String jwtToken) {
    try {
      // make sure the user isn't trying to bookflights for someone else
      if (secUtils.secureUserCalls() && !secUtils.validateJwt(userid, jwtToken)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }

      String bookingIdTo = bs.bookFlight(userid, toFlightSegId, toFlightId);
      
      if (rewardTracker.trackRewardMiles()) {
        rewardTracker.updateRewardMiles(userid, toFlightSegId, true);
      }
      
      String bookingInfo = "";
      String bookingIdReturn = null;
      
      if (!oneWay) {
        bookingIdReturn = bs.bookFlight(userid, retFlightSegId, retFlightId);
        
        if (rewardTracker.trackRewardMiles()) {
          rewardTracker.updateRewardMiles(userid, retFlightSegId, true);
        }
        
        bookingInfo = "{\"oneWay\":false,\"returnBookingId\":\"" 
            + bookingIdReturn + "\",\"departBookingId\":\""
            + bookingIdTo + "\"}";
      } else {
        bookingInfo = "{\"oneWay\":true,\"departBookingId\":\"" + bookingIdTo + "\"}";
      }
      return Response.ok(bookingInfo).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get Booking by Number.
   */
  @GET
  @Path("/bybookingnumber/{userid}/{number}")
  @Produces("text/plain")
  @Timed(name = "com.acmeair.web.BookingServiceRest.getBookingByNumber",
      tags = "app=bookingservice-java") 
  public Response getBookingByNumber(@PathParam("number") String number, 
      @PathParam("userid") String userid,
      @CookieParam("jwt_token") String jwtToken) {
    try {
      // make sure the user isn't trying to bookflights for someone else
      if (secUtils.secureUserCalls() && !secUtils.validateJwt(userid, jwtToken)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
      return Response.ok(bs.getBooking(userid, number)).build();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Get bookins for a customer.
   */
  @GET
  @Path("/byuser/{user}")
  @Produces("text/plain")
  @Timed(name = "com.acmeair.web.bookFlights.BookingServiceRest.getBookingsByUser",
      tags = "app=bookingervice-java")
  public Response getBookingsByUser(@PathParam("user") String user, 
      @CookieParam("jwt_token") String jwtToken) {

    try {
      // make sure the user isn't trying to bookflights for someone else
      if (secUtils.secureUserCalls() && !secUtils.validateJwt(user, jwtToken)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
      return Response.ok(bs.getBookingsByUser(user).toString()).build();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Cancel bookings.
   */
  @POST
  @Consumes({ "application/x-www-form-urlencoded" })
  @Path("/cancelbooking")
  @Produces("text/plain")
  @Timed(name = "com.acmeair.web.bookFlights.BookingServiceRest.cancelBookingsByNumber",
      tags = "app=bookingervice-java")
  public Response cancelBookingsByNumber(@FormParam("number") String number, 
      @FormParam("userid") String userid,
      @CookieParam("jwt_token") String jwtToken) {
    try {
      // make sure the user isn't trying to bookflights for someone else
      if (secUtils.secureUserCalls() && !secUtils.validateJwt(userid, jwtToken)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
     
      if (rewardTracker.trackRewardMiles()) {
        try {
          JsonReader jsonReader = factory.createReader(new StringReader(bs
              .getBooking(userid, number)));
          JsonObject booking = jsonReader.readObject();
          jsonReader.close();

          bs.cancelBooking(userid, number);
          rewardTracker.updateRewardMiles(userid, booking.getString("flightSegmentId"), false);
        } catch (RuntimeException re) {
          // booking does not exist
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("booking : This booking does not exist: " + number);
          }
        }
      } else {
        bs.cancelBooking(userid, number);
      }
      return Response.ok("booking " + number + " deleted.").build();

    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GET
  public Response status() {
    return Response.ok("OK").build();
  }
}