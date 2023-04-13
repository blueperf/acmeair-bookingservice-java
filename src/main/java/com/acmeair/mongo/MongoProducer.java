/*******************************************************************************
* Copyright (c) 2023 IBM Corp.
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

package com.acmeair.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import org.eclipse.microprofile.config.inject.ConfigProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class MongoProducer {

  @Inject 
  @ConfigProperties
  MongoProperties mongoProps; 

  @Produces
  public MongoClient createMongo() {
    ServerAddress serverAddress = new ServerAddress(mongoProps.host, mongoProps.port);
    MongoClientOptions.Builder options = new MongoClientOptions.Builder();

    if (mongoProps.connectionsPerHost.isPresent()) {
      options.connectionsPerHost(mongoProps.connectionsPerHost.get());
    }
    if (mongoProps.minConnectionsPerHost.isPresent()) {
      options.minConnectionsPerHost(mongoProps.minConnectionsPerHost.get());
    }
    if (mongoProps.maxWaitTime.isPresent()) {
      options.maxWaitTime(mongoProps.maxWaitTime.get());
    }
    if (mongoProps.connectTimeOut.isPresent()) {
      options.connectTimeout(mongoProps.connectTimeOut.get());
    }
    if (mongoProps.socketTimeOut.isPresent()) {
      options.socketTimeout(mongoProps.socketTimeOut.get());
    }
    if (mongoProps.sslEnabled.isPresent()) {
      options.sslEnabled(mongoProps.sslEnabled.get());
    }
    if (mongoProps.threadsAllowedToBlockForConnectionMultiplier.isPresent()) {
      options.threadsAllowedToBlockForConnectionMultiplier(
          mongoProps.threadsAllowedToBlockForConnectionMultiplier.get());
    }
    if (mongoProps.maxConnectionIdleTime.isPresent()) {
      options.maxConnectionIdleTime(mongoProps.maxConnectionIdleTime.get());
    }

    MongoClientOptions builtOptions = options.build();
    
    if ((!mongoProps.username.isPresent()) || (!mongoProps.password.isPresent())) {
        return new MongoClient(serverAddress, builtOptions);
    } else {
      MongoCredential credential = MongoCredential.createCredential(
        mongoProps.username.get(), 
        mongoProps.database, 
        mongoProps.password.get().toCharArray());
      return new MongoClient(serverAddress, credential, builtOptions);
    }
  }

  @Produces
  public MongoDatabase createDB(MongoClient client) {
    return client.getDatabase(mongoProps.database);
  }

  public void close(@Disposes MongoClient toClose) {
    toClose.close();
  }
}