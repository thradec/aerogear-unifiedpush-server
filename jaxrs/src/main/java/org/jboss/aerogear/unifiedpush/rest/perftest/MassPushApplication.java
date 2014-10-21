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
import org.jboss.aerogear.unifiedpush.api.BaseModel;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class MassPushApplication extends BaseModel {

    private static final long serialVersionUID = -6939411397713046090L;

    private List<PushApplication> applications;

    private int count;

    public void setApplications(List<PushApplication> applications) {
        this.applications = applications;
    }

    public List<PushApplication> getApplications() {
        return applications;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

}
