package com.chaordicsystems.tools.s3;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Joiner;

public class S3Utils {
	static final int MAX_KEYS=1;
			
    public static void restoreObject(S3Object s3Obj, Integer expirationInDays) {
        AmazonS3Client s3Client = new AmazonS3Client(new ProfileCredentialsProvider());
        try {
            System.out.printf("- Restoring s3://%s/%s for %d days.. ", s3Obj.getBucketName(), s3Obj.getObjectKey(), expirationInDays);

            if (!s3Obj.getStorageClass().equals(S3Object.GLACIER_STORAGE_CLASS)) {
                System.out.print("[NOT ON GLACIER]\n");
                return;
            }

            RestoreObjectRequest requestRestore = new RestoreObjectRequest(s3Obj.getBucketName(), s3Obj.getObjectKey(), 2);
            s3Client.restoreObject(requestRestore);

            System.out.print("[STARTED RESTORING]\n");
        } catch (AmazonS3Exception amazonS3Exception) {
            if (amazonS3Exception.getMessage().startsWith("Object restore is already in progress")) {
                System.out.print("[ALREADY RESTORING]\n");
            } else {
                System.out.printf("[AMAZON FAILURE] Exception: %s\n", amazonS3Exception.toString());
            }
        } catch (Exception ex) {
            System.out.printf("[PROGRAM FAILURE] %s\n", ex.toString());
            ex.printStackTrace();
        }
    }

    private static boolean isAlreadyRestoring(S3Object s3Obj, AmazonS3Client s3Client) {
        GetObjectMetadataRequest requestCheck = new GetObjectMetadataRequest(s3Obj.getBucketName(), s3Obj.getObjectKey());
        ObjectMetadata response = s3Client.getObjectMetadata(requestCheck);
        Boolean ongoingRestore = response.getOngoingRestore();
        return ongoingRestore != null && ongoingRestore.booleanValue();
    }

    public static void restoreObjects(List<S3Object> s3Objects, Integer expirationInDays) {
        System.out.printf("- Restoring %d objects\n", s3Objects.size());
        for (S3Object s3Object : s3Objects) {
            restoreObject(s3Object, expirationInDays);
        }
    }


    public static List<S3Object> listObjects(S3Object baseS3Path) {
        System.out.printf("- Fetching file list from prefix: s3://%s/%s\n",
                baseS3Path.getBucketName(), baseS3Path.getObjectKey());
        LinkedList<S3Object> result = new LinkedList<S3Object>();

        AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
        .withBucketName(baseS3Path.getBucketName())
        .withPrefix(baseS3Path.getObjectKey())
        .withMaxKeys(MAX_KEYS);
        ObjectListing objectListing = null;
        String marker;
        do {        	
            objectListing = s3client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary :
                objectListing.getObjectSummaries()) {            	
                S3Object s3Obj = new S3Object(objectSummary.getBucketName(), objectSummary.getKey());
                System.out.println("Adding object to restore list ["+objectSummary.getBucketName()+"/"+objectSummary.getKey()+"]");
                s3Obj.setStorageClass(objectSummary.getStorageClass());
                result.add(s3Obj);
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());

        return result;
    }

    public static S3Object parseS3Path(String fullS3Path) {
        String[] splittedS3Path = fullS3Path.split("/");
        String bucketName = null;
        String objectKey = null;
        if (splittedS3Path[0].equals("s3:")) {
            bucketName = splittedS3Path[2];
            String[] remaining = Arrays.copyOfRange(splittedS3Path, 3, splittedS3Path.length);
            objectKey = Joiner.on("/").join(remaining);
        } else {
            bucketName = splittedS3Path[0];
            String[] remaining = Arrays.copyOfRange(splittedS3Path, 1, splittedS3Path.length);
            objectKey = Joiner.on("/").join(remaining);
        }
        return new S3Object(bucketName, objectKey);
    }

}
