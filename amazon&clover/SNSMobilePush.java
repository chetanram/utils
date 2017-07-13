package com.screensaver.util;

/*
 * Copyright 2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.screensaver.SampleMessageGenerator.Platform;




public class SNSMobilePush {

    private  Context context;
    private AmazonSNSClientWrapper snsClientWrapper;

    public SNSMobilePush(AmazonSNS snsClient, Context context) {
        this.context = context;
        this.snsClientWrapper = new AmazonSNSClientWrapper(snsClient,context);

    }

    public static final Map<Platform, Map<String, MessageAttributeValue>> attributesMap = new HashMap<Platform, Map<String, MessageAttributeValue>>();

    static {
        attributesMap.put(Platform.GCM, null);

    }


    public void demoAndroidAppNotification(String serverAPIKey, String applicationName, String registrationId) {
        // TODO: Please fill in following values for your application. You can
        // also change the notification payload as per your preferences using
        // the method
        // com.amazonaws.sns.samples.tools.SampleMessageGenerator.getSampleAndroidMessage()

        snsClientWrapper.demoNotification(Platform.GCM, "", serverAPIKey,
                registrationId, applicationName, attributesMap);
    }


}
