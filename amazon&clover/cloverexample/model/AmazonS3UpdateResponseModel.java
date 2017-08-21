package com.texasbrokers.screensaver.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by chetan on 5/7/17.
 */

public class AmazonS3UpdateResponseModel {

    @SerializedName("default")
    @Expose
    private Default _default;

    public Default getDefault() {
        return _default;
    }

    public void setDefault(Default _default) {
        this._default = _default;
    }

    public class Bucket {

        @SerializedName("arn")
        @Expose
        private String arn;
        @SerializedName("ownerIdentity")
        @Expose
        private OwnerIdentity ownerIdentity;
        @SerializedName("name")
        @Expose
        private String name;

        public String getArn() {
            return arn;
        }

        public void setArn(String arn) {
            this.arn = arn;
        }

        public OwnerIdentity getOwnerIdentity() {
            return ownerIdentity;
        }

        public void setOwnerIdentity(OwnerIdentity ownerIdentity) {
            this.ownerIdentity = ownerIdentity;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public class Default {

        @SerializedName("Records")
        @Expose
        private List<Record> records = null;

        public List<Record> getRecords() {
            return records;
        }

        public void setRecords(List<Record> records) {
            this.records = records;
        }

    }

    public class Object {

        @SerializedName("eTag")
        @Expose
        private String eTag;
        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("sequencer")
        @Expose
        private String sequencer;
        @SerializedName("size")
        @Expose
        private Integer size;

        public String getETag() {
            return eTag;
        }

        public void setETag(String eTag) {
            this.eTag = eTag;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getSequencer() {
            return sequencer;
        }

        public void setSequencer(String sequencer) {
            this.sequencer = sequencer;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

    }

    public class OwnerIdentity {

        @SerializedName("principalId")
        @Expose
        private String principalId;

        public String getPrincipalId() {
            return principalId;
        }

        public void setPrincipalId(String principalId) {
            this.principalId = principalId;
        }

    }

    public class Record {

        @SerializedName("eventSource")
        @Expose
        private String eventSource;
        @SerializedName("userIdentity")
        @Expose
        private UserIdentity userIdentity;
        @SerializedName("responseElements")
        @Expose
        private ResponseElements responseElements;
        @SerializedName("eventName")
        @Expose
        private String eventName;
        @SerializedName("eventVersion")
        @Expose
        private String eventVersion;
        @SerializedName("eventTime")
        @Expose
        private String eventTime;
        @SerializedName("s3")
        @Expose
        private S3 s3;
        @SerializedName("awsRegion")
        @Expose
        private String awsRegion;
        @SerializedName("requestParameters")
        @Expose
        private RequestParameters requestParameters;

        public String getEventSource() {
            return eventSource;
        }

        public void setEventSource(String eventSource) {
            this.eventSource = eventSource;
        }

        public UserIdentity getUserIdentity() {
            return userIdentity;
        }

        public void setUserIdentity(UserIdentity userIdentity) {
            this.userIdentity = userIdentity;
        }

        public ResponseElements getResponseElements() {
            return responseElements;
        }

        public void setResponseElements(ResponseElements responseElements) {
            this.responseElements = responseElements;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public String getEventVersion() {
            return eventVersion;
        }

        public void setEventVersion(String eventVersion) {
            this.eventVersion = eventVersion;
        }

        public String getEventTime() {
            return eventTime;
        }

        public void setEventTime(String eventTime) {
            this.eventTime = eventTime;
        }

        public S3 getS3() {
            return s3;
        }

        public void setS3(S3 s3) {
            this.s3 = s3;
        }

        public String getAwsRegion() {
            return awsRegion;
        }

        public void setAwsRegion(String awsRegion) {
            this.awsRegion = awsRegion;
        }

        public RequestParameters getRequestParameters() {
            return requestParameters;
        }

        public void setRequestParameters(RequestParameters requestParameters) {
            this.requestParameters = requestParameters;
        }

    }

    public class RequestParameters {

        @SerializedName("sourceIPAddress")
        @Expose
        private String sourceIPAddress;

        public String getSourceIPAddress() {
            return sourceIPAddress;
        }

        public void setSourceIPAddress(String sourceIPAddress) {
            this.sourceIPAddress = sourceIPAddress;
        }

    }

    public class ResponseElements {

        @SerializedName("x-amz-id-2")
        @Expose
        private String xAmzId2;
        @SerializedName("x-amz-request-id")
        @Expose
        private String xAmzRequestId;

        public String getXAmzId2() {
            return xAmzId2;
        }

        public void setXAmzId2(String xAmzId2) {
            this.xAmzId2 = xAmzId2;
        }

        public String getXAmzRequestId() {
            return xAmzRequestId;
        }

        public void setXAmzRequestId(String xAmzRequestId) {
            this.xAmzRequestId = xAmzRequestId;
        }

    }

    public class S3 {

        @SerializedName("configurationId")
        @Expose
        private String configurationId;
        @SerializedName("s3SchemaVersion")
        @Expose
        private String s3SchemaVersion;
        @SerializedName("object")
        @Expose
        private Object object;
        @SerializedName("bucket")
        @Expose
        private Bucket bucket;

        public String getConfigurationId() {
            return configurationId;
        }

        public void setConfigurationId(String configurationId) {
            this.configurationId = configurationId;
        }

        public String getS3SchemaVersion() {
            return s3SchemaVersion;
        }

        public void setS3SchemaVersion(String s3SchemaVersion) {
            this.s3SchemaVersion = s3SchemaVersion;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public Bucket getBucket() {
            return bucket;
        }

        public void setBucket(Bucket bucket) {
            this.bucket = bucket;
        }

    }

    public class UserIdentity {

        @SerializedName("principalId")
        @Expose
        private String principalId;

        public String getPrincipalId() {
            return principalId;
        }

        public void setPrincipalId(String principalId) {
            this.principalId = principalId;
        }

    }

}
