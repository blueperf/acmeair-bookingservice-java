package com.acmeair.mongo;

import java.util.Optional;

import jakarta.enterprise.context.Dependent;

import org.eclipse.microprofile.config.inject.ConfigProperties;

@ConfigProperties(prefix="mongo")
@Dependent
public class MongoProperties {
  public MongoProperties() {}

  public String host;
  public Integer port;
  public String database;
  
  public Optional<String> username;
  public Optional<String> password;
  public Optional<Boolean> sslEnabled;
  public Optional<Integer> minConnectionsPerHost;
  public Optional<Integer> connectionsPerHost;
  public Optional<Integer> maxWaitTime;
  public Optional<Integer> connectTimeOut;
  public Optional<Integer> socketTimeOut;
  public Optional<Integer> threadsAllowedToBlockForConnectionMultiplier;
  public Optional<Integer> maxConnectionIdleTime;
}
