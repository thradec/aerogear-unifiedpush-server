/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.perftest;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.InstallationDao;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@Stateless
@Path("/mass/installations")
@TransactionAttribute
public class BatchPushInstallationEndpoint {

    private static final Logger logger = Logger.getLogger(BatchPushInstallationEndpoint.class.getName());

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    private GenericVariantService genericVariantService;

    @Inject
    private InstallationDao installationDao;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerInstallations(@Context HttpServletRequest request, MassInstallation massInstallation) {

        for (Map.Entry<String, List<Installation>> entry : massInstallation.map.entrySet()) {
            Variant variant = genericVariantService.findByVariantID(entry.getKey());

            int countOfInstallations = entry.getValue().size();

            logger.log(Level.INFO, "Going to register {0} installations for variant {1}", new Object[] {
                countOfInstallations, variant.getVariantID()
            });

            for (Installation installation : entry.getValue()) {
                clientInstallationService.addInstallation(variant, installation);
            }

            logger.log(Level.INFO, "Registered {0} installations for variant {1}", new Object[] {
                countOfInstallations, variant.getVariantID()
            });
        }

        return Response.ok().build();
    }

    @GET
    @Path("{variantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countAllInstallationsForVariant(@Context HttpServletRequest request, @PathParam("variantId") String variantId) {

        long count = installationDao.getNumberOfDevicesForVariantIDs(HttpRequestUtil.extractUsername(request));

        logger.info("Count of installations: " + count);

        return Response.ok(count).build();
    }

}
