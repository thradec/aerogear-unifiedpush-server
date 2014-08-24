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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@Stateless
@Path("/mass/applications")
public class BatchPushApplicationEndpoint extends AbstractBaseEndpoint {

    @Inject
    private PushApplicationService pushAppService;

    @Inject
    private PushApplicationDao pushAppDao;

    @Inject
    private GenericVariantService variantService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/generated")
    public Response registerGeneratedApplications(@Context HttpServletRequest request, MassPushApplication massPushApplication) throws Exception {

        // validation
        for (PushApplication pushApplication : massPushApplication.getApplications()) {

            try {
                validateModelClass(pushApplication);
            } catch (ConstraintViolationException cve) {
                logger.info("Validation for en mass application registration failed");
                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }
        }

        // we can not save pushApp which has variants already set because it would update yet non-existing application

        Map<String, List<Variant>> map = new HashMap<String, List<Variant>>();

        for (PushApplication pushApp : massPushApplication.getApplications()) {
            map.put(pushApp.getPushApplicationID(), pushApp.getVariants());
            pushApp.setVariants(new ArrayList<Variant>());
        }

        // persist pushApps without variants
        for (PushApplication pushApp : massPushApplication.getApplications()) {
            pushAppService.addPushApplication(pushApp);
            logger.info(String.format("Added push application with pushAppId %s", pushApp.getPushApplicationID()));
        }

        // persist variants themselves
        for (Map.Entry<String, List<Variant>> entry : map.entrySet()) {
            PushApplication managedPushApp = pushAppService.findByPushApplicationID(entry.getKey());

            for (Variant variant : entry.getValue()) {
                variantService.addVariant(variant);
                pushAppService.addVariant(managedPushApp, variant);
            }
        }

        return Response.ok().build();
    }

    /**
     * Creates arbitrary number of applications according to count parameter in MassPushApplication bean.
     *
     * @param request
     * @param massPushApplication
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerApplications(@Context HttpServletRequest request, MassPushApplication massPushApplication) {

        // validation
        try {
            validateModelClass(massPushApplication);
        } catch (ConstraintViolationException cve) {
            logger.info("Validation for en mass application registration failed");
            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        logger.info(String.format("Going to create %s applications.", massPushApplication.getCount()));

        String developer = HttpRequestUtil.extractUsername(request);

        String pushAppId = null;

        List<String> pushAppsIds = new ArrayList<String>();

        for (int i = 0; i < massPushApplication.getCount(); i++) {
            PushApplication pushApplication = new PushApplication();

            pushAppId = UUID.randomUUID().toString();

            pushApplication.setPushApplicationID(pushAppId);
            pushApplication.setName(pushAppId);
            pushApplication.setDeveloper(developer);

            pushAppService.addPushApplication(pushApplication);

            pushAppsIds.add(pushAppId);

            if (i % 1000 == 0) {
                logger.info("Created " + i + " applications");
            }
        }

        logger.info(String.format("%s application(s) were created.", massPushApplication.getCount()));

        MassResponse massiveResponse = new MassResponse();
        massiveResponse.setAppsIds(pushAppsIds);

        return Response.ok(massiveResponse).build();
    }

    @DELETE
    public Response deleteAllApplications(@Context HttpServletRequest request) {

        String developer = HttpRequestUtil.extractUsername(request);

        logger.info("Deleting all applications");

        while (true) {
            PageResult<PushApplication> result = pushAppService.findAllPushApplicationsForDeveloper(developer, 0, 1000);
            for (PushApplication app : result.getResultList()) {
                pushAppService.removePushApplication(app);
            }
            if (result.getCount() <= 1000) {
                break;
            }
        }

        logger.info("All applications deleted.");

        return Response.noContent().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response countOfAllApplications(@Context HttpServletRequest request) {

        String developer = HttpRequestUtil.extractUsername(request);

        long count = pushAppDao.getNumberOfPushApplicationsForDeveloper(developer);

        return Response.ok(count).build();
    }
}
