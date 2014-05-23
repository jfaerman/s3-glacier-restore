package com.chaordicsystems.tools.s3;

public class S3Object {

    public static final String GLACIER_STORAGE_CLASS = "GLACIER";
    private String bucketName;
    private String objectKey;
    private String storageClass = GLACIER_STORAGE_CLASS; //let's assume all objects are in glacier

    public S3Object(String bucketName, String objectKey) {
        super();
        this.bucketName = bucketName;
        this.objectKey = objectKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }
}
