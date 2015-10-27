package org.jboss.aerogear.unifiedpush.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;

@Path("/test")
public class TestEndpoint {

    @Inject
    private ClientInstallationService instalationService;

    @GET
    public Response test() {
        System.out.println("TestEndpoint.test()");
        for (int i = 0; i < 1000; i++) {
            instalationService.generateInstalations();
            System.out.println(i);
        }

        return Response.ok().entity("OK").build();
    }

}