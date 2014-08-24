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
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.AbstractVariantEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@Stateless
@Path("/mass/variants")
@TransactionAttribute
public class BatchPushVariantEndpoint extends AbstractVariantEndpoint {

    private static final Logger logger = Logger.getLogger(BatchPushVariantEndpoint.class.getName());

    @Inject
    private PushApplicationService pushAppService;

    @Inject
    private VariantDao variantDao;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerVariants(@Context HttpServletRequest request, MassPushVariant massVariant) {

        try {
            validateModelClass(massVariant);
        } catch (ConstraintViolationException cve) {
            logger.info("Validation for en mass application registration failed");
            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        String developer = HttpRequestUtil.extractUsername(request);

        PushApplication pushApp = null;

        if (massVariant.getApplicationId() == null) {
            PushApplication pushApplication = new PushApplication();

            String pushAppID = UUID.randomUUID().toString();

            pushApplication.setName(pushAppID);
            pushApplication.setPushApplicationID(pushAppID);
            pushApplication.setDeveloper(developer);

            pushAppService.addPushApplication(pushApplication);

            pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushAppID, developer);
        } else {
            pushApp = pushAppService.findByPushApplicationIDForDeveloper(massVariant.getApplicationId(), developer);
        }

        List<Variant> variants = new ArrayList<Variant>();

        String variantId = null;

        List<String> massiveVarsIds = new ArrayList<String>();
        List<String> massiveAppsIds = new ArrayList<String>();

        for (int i = 0; i < massVariant.getNumberOfVariants(); i++) {
            AndroidVariant variant = new AndroidVariant();
            variant.setGoogleKey(UUID.randomUUID().toString());

            variantId = UUID.randomUUID().toString();

            variant.setVariantID(variantId);
            variant.setDeveloper(developer);
            variants.add(variant);

            massiveVarsIds.add(variantId);
        }

        logger.info(String.format("Going to create %s variants for applicationId %s.",
            massVariant.getNumberOfVariants(), massVariant.getApplicationId()));

        for (Variant variant : variants) {
            variantDao.create(variant);
        }

        logger.info(String.format("%s variants created.", massVariant.getNumberOfVariants()));

        pushApp.setVariants(variants);

        pushAppService.updatePushApplication(pushApp);

        massiveAppsIds.add(pushApp.getPushApplicationID());

        logger.info(String.format("Push application updated"));

        MassResponse massiveResponse = new MassResponse();
        massiveResponse.setAppsIds(massiveAppsIds);
        massiveResponse.setVarsIds(massiveVarsIds);

        return Response.ok(massiveResponse).build();
    }

    @GET
    @Path("{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCountOfAllVariants(@Context HttpServletRequest request, @PathParam("applicationId") String applicationId) {

        PushApplication pushApp = pushAppService.findByPushApplicationID(applicationId);

        int countOfAllVariants = getVariantsByType(pushApp, AndroidVariant.class).size();

        return Response.ok(countOfAllVariants).build();
    }
}
