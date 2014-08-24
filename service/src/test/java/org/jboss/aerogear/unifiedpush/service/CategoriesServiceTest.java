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
package org.jboss.aerogear.unifiedpush.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPACategoriesDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.jboss.aerogear.unifiedpush.service.impl.CategoryServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.ClientInstallationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.GenericVariantServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(ApplicationComposer.class)
public class CategoriesServiceTest extends AbstractBaseServiceTest {

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    private GenericVariantService variantService;

    @Inject
    private CategoryService categoryService;

    private AndroidVariant androidVariant;

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(AbstractBaseServiceTest.EntityManagerProducer.class);
        beans.addManagedClass(ClientInstallationServiceImpl.class);
        beans.addManagedClass(CategoryServiceImpl.class);
        beans.addManagedClass(GenericVariantServiceImpl.class);
        beans.addManagedClass(JPACategoriesDao.class);
        beans.addManagedClass(JPAInstallationDao.class);
        beans.addManagedClass(JPAVariantDao.class);

        return beans;
    }

    @Before
    public void setup() {
        // setup a variant:
        androidVariant = new AndroidVariant();
        androidVariant.setGoogleKey("Key");
        androidVariant.setName("Android");
        androidVariant.setDeveloper("me");
        variantService.addVariant(androidVariant);
    }

    @Test
    public void categoriesTest() {

        final int NUMBER_OF_INSTALLATIONS = 3;
        final List<Installation> devices = new ArrayList<Installation>();

        final Set<Category> categories = new HashSet<Category>();
        categories.add(new Category("category1"));
        categories.add(new Category("category2"));
        categories.add(new Category("category3"));

        for (int i = 0; i < NUMBER_OF_INSTALLATIONS; i++) {
            Installation device = new Installation();
            device.setDeviceToken(generateFakedDeviceTokenString());

            device.setCategories(categories);
            devices.add(device);
        }

        for (Installation installation : devices) {
            clientInstallationService.addInstallation(androidVariant, installation);
        }

        List<String> foundCategories = categoryService.findAllCategories();

        Assert.assertEquals(foundCategories.size(), 3);
    }

    private String generateFakedDeviceTokenString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        sb.append(UUID.randomUUID().toString());
        return sb.toString();
    }

}
