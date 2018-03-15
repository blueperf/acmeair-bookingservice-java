/*******************************************************************************
* Copyright (c) 2018 IBM Corp.
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
