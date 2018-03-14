/**
 * 
 */
package com.acmeair.web;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;

import com.acmeair.health.HealthCheckBean;

/**
 * @author jagraj
 *
 */
@ApplicationScoped
@Path("/bsHealthCheck")
@Metered(name = "com.acmeair.web.HealthCheckRest.Type.Metered", tags = "app=customerservice-java")
public class HealthCheckRest {

	@Inject
	private HealthCheckBean healthCheckBean;

	@GET
	@Counted(name = "com.acmeair.web.HealthCheckRest.checkStatus.monotonic(true)", monotonic = true, tags = "app=flightservice-java")
	public Response checkStatus() {
		return Response.ok("OK").build();
	}

	@POST
	@Path("/updateHealthStatus")
	@Produces("text/plain")
	@Consumes("text/plain")
	@Counted(name = "com.acmeair.web.HealthCheckRest.updateHealthStatus.monotonic.absolute(true)", monotonic = true, tags = "customerservice-java")
	public void updateHealthStatus(@QueryParam("isAppDown") Boolean isAppDown) {
		healthCheckBean.setIsAppDown(isAppDown);
	}
}
