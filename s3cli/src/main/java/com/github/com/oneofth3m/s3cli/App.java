package com.github.com.oneofth3m.s3cli;

/**
 * S3 CLI
 *
 */

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketPolicy;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class App
{
  public static void main(String[] args) {
    String nutanixEndpoint = "10.51.155.130";
    String bucketName = "javatransfermanagertest";
    String accessKey = "hV1BjZ4mink1BGxN2eBZBLeXiMAzj2jz";
    String secretKey = "i_qzir3pCkaQuo6gKXvLDgwB-GQvRHvv";
    String filePath = "/tmp/smallFile.txt";
    String objectKey = "smallObject";
    String xferMgrFilePath = "/tmp/largeFile.txt";
    String xferMgrObjectKey = "largeObject";

    BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
    /*
    // Create S3 client for AWS.
    final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
      .withRegion(Regions.US_EAST_1)
      .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
      .build();
    // End Create S3 client for AWS.
    // */

    // Create S3 client for Nutanix Objects.
    AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(nutanixEndpoint, "us-east-1"))
      .withCredentials(new AWSStaticCredentialsProvider(awsCreds));
    amazonS3ClientBuilder.setPathStyleAccessEnabled(false);
    final AmazonS3 s3 = amazonS3ClientBuilder.build();
    // End Create S3 client for Nutanix Objects.

    // ListBuckets.
    System.out.printf("\n=== ListBuckets ===\n");
    List<Bucket> buckets = s3.listBuckets();
    for (Bucket b : buckets) {
      System.out.printf("\tBucket Name: '%s'\n", b.getName());
    }
    System.out.printf("\n=== ListBuckets ===\n");

    // Check if bucket exists.
    System.out.printf("\n=== Check Bucket ===\n");
    if (s3.doesBucketExistV2(bucketName)) {
      System.out.printf("Bucket '%s' already exists\n", bucketName);
    }
    System.out.printf("\n=== Check Bucket ===\n");

    // GetBucketPolicy.
    System.out.printf("\n=== GetBucketPolicy ===\n");
    String policyText = null;
    try {
      BucketPolicy bucket_policy = s3.getBucketPolicy(bucketName);
      policyText = bucket_policy.getPolicyText();
      System.out.printf("Bucket policy: %s\n", policyText);
    } catch (AmazonServiceException e) {
      System.err.println(e.getErrorMessage());
      System.exit(1);
    }
    System.out.printf("\n=== GetBucketPolicy ===\n");

    // Upload Object.
    System.out.printf("\n=== PutObject ===\n");
    try {
      s3.putObject(bucketName, objectKey, new File(filePath));
    } catch (AmazonServiceException e) {
      System.err.println(e.getErrorMessage());
      System.exit(1);
    }
    System.out.printf("\n=== PutObject ===\n");

    // List Objects.
    System.out.printf("\n=== ListObjectsV2 ===\n");
    ListObjectsV2Result result = s3.listObjectsV2(bucketName);
    List<S3ObjectSummary> objects = result.getObjectSummaries();
    for (S3ObjectSummary os: objects) {
      System.out.printf("\tObject Name: %s\n", os.getKey());
    }
    System.out.printf("\n=== ListObjectsV2 ===\n");

    // Get Objects.
    System.out.printf("\n=== GetObject ===\n");
    try {
      S3Object o = s3.getObject(bucketName, objectKey);
      S3ObjectInputStream s3is = o.getObjectContent();
      byte[] readBuf = new byte[1024];
      int readLen = 0;
      long readTotal = 0;
      while ((readLen = s3is.read(readBuf)) > 0) {
        System.out.printf("%s", new String(readBuf));
        readTotal += readLen;
      }
      System.out.printf("Object Size : %d\n", readTotal);
    } catch (AmazonServiceException e) {
      System.err.println(e.getErrorMessage());
      System.exit(1);
    } catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
    System.out.printf("\n=== GetObject ===\n");

    // Delete Object
    System.out.printf("\n=== DeleteObject ===\n");
    try {
      s3.deleteObject(bucketName, objectKey);
    } catch (AmazonServiceException e) {
     System.out.println(e.getMessage());
      System.exit(1);
    }
    System.out.printf("\n=== DeleteObject ===\n");


    // Transfer Manager.
    System.out.printf("\n=== TransferManager Upload ===\n");
    /*
    // Transfer Manager S3 client for AWS.
    final AmazonS3 xferMgrS3 = AmazonS3ClientBuilder.standard()
      .withRegion(Regions.US_EAST_1)
      .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
      .build();
    // End Transfer Manager S3 client for AWS.
    */

    // Transfer Manager S3 client for Nutanix Objects.
    AmazonS3ClientBuilder xferMgrAmazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(nutanixEndpoint, "us-east-1"))
      .withCredentials(new AWSStaticCredentialsProvider(awsCreds));
    xferMgrAmazonS3ClientBuilder.setPathStyleAccessEnabled(false);
    final AmazonS3 xferMgrS3 = xferMgrAmazonS3ClientBuilder.build();
    // End Transfer Manager S3 client for Nutanix Objects.

    TransferManager xferMgr = TransferManagerBuilder.standard()
      .withS3Client(xferMgrS3)
      .build();
    try {
      Upload xfer = xferMgr.upload(bucketName, xferMgrObjectKey, new File(xferMgrFilePath));

      // Get progress for 5 times
      for (int i = 0; i < 5; i++) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          System.out.println(e.getMessage());
          System.exit(1);
        }
        TransferProgress xferProgress = xfer.getProgress();
        System.out.printf("%d/%d uploaded. Percent %f\n",
            xferProgress.getBytesTransfered(),
            xferProgress.getTotalBytesToTransfer(),
            xferProgress.getPercentTransferred());
      }

      // Wait for completion
      try {
        System.out.println("Wait for completion");
        xfer.waitForCompletion();
      } catch (AmazonServiceException e) {
        System.out.println(e.getMessage());
        System.exit(1);
      } catch (AmazonClientException e) {
        System.out.println(e.getMessage());
        System.exit(1);
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
        System.exit(1);
      }

    } catch (AmazonServiceException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
    xferMgr.shutdownNow();
    System.out.printf("\n=== TransferManager Upload ===\n");

    // List Objects.
    System.out.printf("\n=== ListObjectsV2 ===\n");
    result = s3.listObjectsV2(bucketName);
    objects = result.getObjectSummaries();
    for (S3ObjectSummary os: objects) {
      System.out.printf("\tObject Name: %s\n", os.getKey());
    }
    System.out.printf("\n=== ListObjectsV2 ===\n");

    System.exit(0);
  }
}
