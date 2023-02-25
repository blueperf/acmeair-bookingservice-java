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

package com.acmeair.mongo.services;

import static com.mongodb.client.model.Filters.eq;

import com.acmeair.mongo.ConnectionManager;
import com.acmeair.service.BookingService;
import com.acmeair.service.KeyGenerator;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.bson.Document;

import org.eclipse.microprofile.opentracing.Traced;

import io.opentracing.Span;
import io.opentracing.Tracer;

@ApplicationScoped
public class BookingServiceImpl implements BookingService {

  private static final  Logger logger = Logger.getLogger(BookingService.class.getName());

  private MongoCollection<Document> bookingCollection;

  @Inject
  io.opentracing.Tracer configuredTracer;

  @Inject
  KeyGenerator keyGenerator;

  @Inject 
  ConnectionManager connectionManager;
  
  @PostConstruct
  public void initialization() {
    MongoDatabase database = connectionManager.getDb();
    bookingCollection = database.getCollection("booking");
  }
  
  /**
   * Book Flight.
   */
  @Traced
  public String bookFlight(String customerId, String flightId) {
    try {

      String bookingId = keyGenerator.generate().toString();

      Document bookingDoc = new Document("_id", bookingId)
          .append("customerId", customerId)
          .append("flightId", flightId)
          .append("dateOfBooking", new Date());

      bookingCollection.insertOne(bookingDoc);

      return bookingId;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Traced
  public String bookFlight(String customerId, String flightSegmentId, String flightId) {
    if (flightSegmentId == null) {
      return bookFlight(customerId, flightId);
    } else {

      try {

        String bookingId = keyGenerator.generate().toString();

        Document bookingDoc = new Document("_id", bookingId).append("customerId", customerId)
            .append("flightId", flightId).append("dateOfBooking", new Date())
            .append("flightSegmentId", flightSegmentId);

        Span activeSpan = configuredTracer.activeSpan();
        //System.out.println("activeSpan: " + activeSpan);
        Tracer.SpanBuilder spanBuilder = configuredTracer.buildSpan("Created bookFlight Span");
        if (activeSpan != null) {
            spanBuilder.asChildOf(activeSpan.context());
        }
        
        Span childSpan = spanBuilder.start();
        childSpan.setTag("Created", true);
        
        bookingCollection.insertOne(bookingDoc);
        
        childSpan.finish();

        return bookingId;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }
  }

  @Override
  @Traced
  public String getBooking(String user, String bookingId) {
    try {
      return bookingCollection.find(eq("_id", bookingId)).first().toJson();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Traced
  public List<String> getBookingsByUser(String user) {
    List<String> bookings = new ArrayList<String>();
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("getBookingsByUser : " + user);
    }
    try (MongoCursor<Document> cursor = bookingCollection.find(eq("customerId", user)).iterator()) {

      while (cursor.hasNext()) {
        Document tempBookings = cursor.next();
        Date dateOfBooking = (Date) tempBookings.get("dateOfBooking");
        tempBookings.remove("dateOfBooking");
        tempBookings.append("dateOfBooking", dateOfBooking.toString());

        if (logger.isLoggable(Level.FINE)) {
          logger.fine("getBookingsByUser cursor data : " + tempBookings.toJson());
        }
        bookings.add(tempBookings.toJson());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return bookings;
  }

  @Override
  @Traced
  public void cancelBooking(String user, String bookingId) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("cancelBooking _id : " + bookingId);
    }
    try {
      bookingCollection.deleteMany(eq("_id", bookingId));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Long count() {
    return bookingCollection.countDocuments();
  }

  @Override
  public void dropBookings() {
    bookingCollection.deleteMany(new Document());
  }

  @Override
  public String getServiceType() {
    return "mongo";
  }
  
  @Override
  public boolean isConnected() {
    return (bookingCollection.countDocuments() >= 0);
  }
}
