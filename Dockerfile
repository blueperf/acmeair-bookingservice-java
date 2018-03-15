FROM websphere-liberty:microProfile
COPY server.xml /config/server.xml
RUN installUtility install  --acceptLicense defaultServer
COPY jvm.options /config/jvm.options
COPY target/bookingservice-java-2.0.0-SNAPSHOT.war /config/apps/

ENV MONGO_HOST=booking-db
