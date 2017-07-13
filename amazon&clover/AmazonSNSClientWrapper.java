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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreatePlatformApplicationRequest;
import com.amazonaws.services.sns.model.CreatePlatformApplicationResult;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeletePlatformApplicationRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.NotFoundException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.screensaver.SampleMessageGenerator;
import com.screensaver.SampleMessageGenerator.Platform;

public class AmazonSNSClientWrapper {

    private final AmazonSNS snsClient;
    private Context context;

    public AmazonSNSClientWrapper(AmazonSNS client, Context context) {
        this.snsClient = client;
        this.context = context;

    }

    private CreatePlatformApplicationResult createPlatformApplication(
            String applicationName, Platform platform, String principal,
            String credential) {
        CreatePlatformApplicationRequest platformApplicationRequest = new CreatePlatformApplicationRequest();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("PlatformPrincipal", principal);
        attributes.put("PlatformCredential", credential);
        platformApplicationRequest.setAttributes(attributes);
        platformApplicationRequest.setName(applicationName);
        platformApplicationRequest.setPlatform(platform.name());

        return snsClient.createPlatformApplication(platformApplicationRequest);
    }

    private CreatePlatformEndpointResult createPlatformEndpoint(
            Platform platform, String customData, String platformToken,
            String applicationArn) {
        CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
        platformEndpointRequest.setCustomUserData(customData);
        String token = platformToken;
        String userId = null;
        if (platform == SampleMessageGenerator.Platform.BAIDU) {
            String[] tokenBits = platformToken.split("\\|");
            token = tokenBits[0];
            userId = tokenBits[1];
            Map<String, String> endpointAttributes = new HashMap<String, String>();
            endpointAttributes.put("UserId", userId);
            endpointAttributes.put("ChannelId", token);
            platformEndpointRequest.setAttributes(endpointAttributes);
        }
        platformEndpointRequest.setToken(token);
        platformEndpointRequest.setPlatformApplicationArn(applicationArn);
        return snsClient.createPlatformEndpoint(platformEndpointRequest);

    }

    private void deletePlatformApplication(String applicationArn) {
        DeletePlatformApplicationRequest request = new DeletePlatformApplicationRequest();
        request.setPlatformApplicationArn(applicationArn);
        snsClient.deletePlatformApplication(request);
    }

    private PublishResult publish(String endpointArn, SampleMessageGenerator.Platform platform,
                                  Map<SampleMessageGenerator.Platform, Map<String, MessageAttributeValue>> attributesMap) {
        PublishRequest publishRequest = new PublishRequest();
        Map<String, MessageAttributeValue> notificationAttributes = getValidNotificationAttributes(attributesMap
                .get(platform));
        if (notificationAttributes != null && !notificationAttributes.isEmpty()) {
            publishRequest.setMessageAttributes(notificationAttributes);
        }
        publishRequest.setMessageStructure("json");
        // If the message attributes are not set in the requisite method,
        // notification is sent with default attributes
        String message = getPlatformSampleMessage(platform);
        Map<String, String> messageMap = new HashMap<String, String>();
        messageMap.put(platform.name(), message);
        message = SampleMessageGenerator.jsonify(messageMap);
        // For direct publish to mobile end points, topicArn is not relevant.
        publishRequest.setTargetArn(endpointArn);

        // Display the message that will be sent to the endpoint/
        System.out.println("{Message Body: " + message + "}");
        StringBuilder builder = new StringBuilder();
        builder.append("{Message Attributes: ");
        for (Map.Entry<String, MessageAttributeValue> entry : notificationAttributes
                .entrySet()) {
            builder.append("(\"" + entry.getKey() + "\": \""
                    + entry.getValue().getStringValue() + "\"),");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        System.out.println(builder.toString());

        publishRequest.setMessage(message);

        return snsClient.publish(publishRequest);
    }

    public void demoNotification(Platform platform, String principal,
                                 String credential, String platformToken, String applicationName,
                                 Map<Platform, Map<String, MessageAttributeValue>> attrsMap) {

        String prefEndpointArn = PrefUtils.getString(context, Constants.PREF_END_POINT_ARN, "");
        boolean updateNeeded = false;
        boolean createNeeded = false;
        if (prefEndpointArn != null && !prefEndpointArn.equalsIgnoreCase("")) {
            createNeeded = false;
        } else {
            createNeeded = true;
        }
        if (createNeeded) {
            // Create Platform Application. This corresponds to an app on a
            // platform.
            try {


                CreatePlatformApplicationResult platformApplicationResult = createPlatformApplication(
                        applicationName, platform, principal, credential);

                // The Platform Application Arn can be used to uniquely identify the
                // Platform Application.
                String platformApplicationArn = platformApplicationResult
                        .getPlatformApplicationArn();

                // Create an Endpoint. This corresponds to an app on a device.

                CreatePlatformEndpointResult platformEndpointResult = createPlatformEndpoint(
                        platform,
                        "CustomData - Useful to store endpoint specific data",
                        platformToken, platformApplicationArn);
                System.out.println(platformEndpointResult);
                prefEndpointArn = platformEndpointResult.getEndpointArn();
                PrefUtils.saveString(context, Constants.PREF_END_POINT_ARN, prefEndpointArn);

                // Publish a push notification to an Endpoint.
                CreateTopicResult createTopicResult = createTopic(platform,
                        "CustomData - Useful to store endpoint specific data",
                        platformToken, platformApplicationArn);
                SubscribeRequest subscribeRequest = new SubscribeRequest(createTopicResult.getTopicArn(), "application", platformEndpointResult.getEndpointArn());
                snsClient.subscribe(subscribeRequest);
                createNeeded = false;
            } catch (Exception e) {
                String message = e.getMessage();
                System.out.println("Exception message: " + message);
                Pattern p = Pattern
                        .compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " +
                                "with the same token.*");
                Matcher m = p.matcher(message);
                if (m.matches()) {
                    // The platform endpoint already exists for this token, but with
                    // additional custom data that
                    // createEndpoint doesn't want to overwrite. Just use the
                    // existing platform endpoint.
                    prefEndpointArn = m.group(1);
                    PrefUtils.saveString(context, Constants.PREF_END_POINT_ARN, prefEndpointArn);
                } else {
                    // Rethrow the exception, the input is actually bad.
                    throw e;
                }

            }
        }

        try {
            GetEndpointAttributesRequest geaReq =
                    new GetEndpointAttributesRequest()
                            .withEndpointArn(prefEndpointArn);
            GetEndpointAttributesResult geaRes =
                    snsClient.getEndpointAttributes(geaReq);

            updateNeeded = !geaRes.getAttributes().get("Token").equals(platformToken)
                    || !geaRes.getAttributes().get("Enabled").equalsIgnoreCase("true");

        } catch (Exception nfe) {
            // We had a stored ARN, but the platform endpoint associated with it
            // disappeared. Recreate it.
            createNeeded = true;
        }

        if (createNeeded) {
            // Create Platform Application. This corresponds to an app on a
            // platform.
            try {


                CreatePlatformApplicationResult platformApplicationResult = createPlatformApplication(
                        applicationName, platform, principal, credential);

                // The Platform Application Arn can be used to uniquely identify the
                // Platform Application.
                String platformApplicationArn = platformApplicationResult
                        .getPlatformApplicationArn();

                // Create an Endpoint. This corresponds to an app on a device.

                CreatePlatformEndpointResult platformEndpointResult = createPlatformEndpoint(
                        platform,
                        "",
                        platformToken, platformApplicationArn);
                System.out.println(platformEndpointResult);
                prefEndpointArn = platformEndpointResult.getEndpointArn();
                PrefUtils.saveString(context, Constants.PREF_END_POINT_ARN, prefEndpointArn);

                // Publish a push notification to an Endpoint.
                CreateTopicResult createTopicResult = createTopic(platform,
                        "",
                        platformToken, platformApplicationArn);
                SubscribeRequest subscribeRequest = new SubscribeRequest(createTopicResult.getTopicArn(), "application", platformEndpointResult.getEndpointArn());
                snsClient.subscribe(subscribeRequest);
                createNeeded = false;
            } catch (Exception e) {
                String message = e.getMessage();
                System.out.println("Exception message: " + message);
                Pattern p = Pattern
                        .compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " +
                                "with the same token.*");
                Matcher m = p.matcher(message);
                if (m.matches()) {
                    // The platform endpoint already exists for this token, but with
                    // additional custom data that
                    // createEndpoint doesn't want to overwrite. Just use the
                    // existing platform endpoint.
                    prefEndpointArn = m.group(1);
                    PrefUtils.saveString(context, Constants.PREF_END_POINT_ARN, prefEndpointArn);
                } else {
                    // Rethrow the exception, the input is actually bad.
                    throw e;
                }

            }
        }
        if (updateNeeded){
            Map attribs = new HashMap();
            attribs.put("Token", platformToken);
            attribs.put("Enabled", "true");
            SetEndpointAttributesRequest saeReq =
                    new SetEndpointAttributesRequest()
                            .withEndpointArn(prefEndpointArn)
                            .withAttributes(attribs);
            snsClient.setEndpointAttributes(saeReq);
        }


//        PublishResult publishResult = publish(subscribeRequest.getEndpoint(), platform, attrsMap);


        // Delete the Platform Application since we will no longer be using it.
//        deletePlatformApplication(platformApplicationArn);
    }

    private CreateTopicResult createTopic(Platform platform, String s, String platformToken, String platformApplicationArn) {
        CreateTopicRequest createTopicRequest = new CreateTopicRequest();
        createTopicRequest.withName(Constants.SNS_TOPIC);
        return snsClient.createTopic(createTopicRequest);

    }

    private String getPlatformSampleMessage(Platform platform) {
        switch (platform) {
            case APNS:
                return SampleMessageGenerator.getSampleAppleMessage();
            case APNS_SANDBOX:
                return SampleMessageGenerator.getSampleAppleMessage();
            case GCM:
                return SampleMessageGenerator.getSampleAndroidMessage();
            case ADM:
                return SampleMessageGenerator.getSampleKindleMessage();
            case BAIDU:
                return SampleMessageGenerator.getSampleBaiduMessage();
            case WNS:
                return SampleMessageGenerator.getSampleWNSMessage();
            case MPNS:
                return SampleMessageGenerator.getSampleMPNSMessage();
            default:
                throw new IllegalArgumentException("Platform not supported : "
                        + platform.name());
        }
    }

    public static Map<String, MessageAttributeValue> getValidNotificationAttributes(
            Map<String, MessageAttributeValue> notificationAttributes) {
        Map<String, MessageAttributeValue> validAttributes = new HashMap<String, MessageAttributeValue>();

        if (notificationAttributes == null) return validAttributes;

        for (Map.Entry<String, MessageAttributeValue> entry : notificationAttributes
                .entrySet()) {
            if (!StringUtils.isBlank(entry.getValue().getStringValue())) {
                validAttributes.put(entry.getKey(), entry.getValue());
            }
        }
        return validAttributes;
    }
}
