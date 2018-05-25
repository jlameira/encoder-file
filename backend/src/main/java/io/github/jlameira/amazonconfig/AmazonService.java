package io.github.jlameira.amazonconfig;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import io.github.jlameira.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@EnableConfigurationProperties(PropertiesS3.class)
public class AmazonService implements IAmazonService {

    @Autowired
    private AmazonS3 amazonS3;
    private final StorageProperties storageProperties ;
    private final PropertiesS3 propertiesS3 ;

//    @Autowired
//    public AmazonService(){
//
//    }

    @Autowired
    public AmazonService( StorageProperties properties, PropertiesS3 s3_properties){

        this.storageProperties = properties;
        this.propertiesS3 = s3_properties;
    }




    @Override
    public void initial() {

    }

    @Override
    public void store(MultipartFile file, String path) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (file.isEmpty()) {
            throw new S3StorageException("Failed to store empty file " + filename);
        }
        if (filename.contains("..")) {
            throw new S3StorageException(
                    "Cannot store file with relative path outside current directory "
                            + filename);
        }

        // upload file to s3
        String fileName = path + "/" + filename;


        // Create a list of UploadPartResponse objects. You get one of these
        // for each part upload.
        List<PartETag> partETags = new ArrayList<PartETag>();

        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new
                InitiateMultipartUploadRequest(this.propertiesS3.getBucketName(), fileName);
        InitiateMultipartUploadResult initResponse =
                this.amazonS3.initiateMultipartUpload(initRequest);
        File file2 = null;
        try {
            file2 = convert(file);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            throw new S3StorageException("Failed to store empty file " + filename,e1);
        }
        long contentLength = file2.length();
        long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

        try {
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(this.propertiesS3.getBucketName()).withKey(fileName)
                        .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file2)
                        .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(
                        this.amazonS3.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
                Application.logger.info("Log Partition " + i);
                Application.logger.info("Faltando " + (contentLength - filePosition));
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest = new
                    CompleteMultipartUploadRequest(
                    this.propertiesS3.getBucketName(),
                    fileName,
                    initResponse.getUploadId(),
                    partETags);

            this.amazonS3.completeMultipartUpload(compRequest);
        } catch (Exception e) {
            this.amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(
                    this.propertiesS3.getBucketName(),fileName, initResponse.getUploadId()));
        }



    }

    @Override
    public Resource load(String filename, String local_path) {
        S3Object s3_file = this.amazonS3.getObject(this.propertiesS3.getBucketName(), local_path + "/" + filename);
        S3ObjectInputStream file_stream = s3_file.getObjectContent();

        Resource resource = new InputStreamResource(file_stream);
        if (resource.exists() || resource.isReadable()) {
            Application.logger.info("Loaded "+local_path + "/" + filename+" into S3");
            return resource;
        }
        else {
            throw new S3FileNotFoundException(
                    "Could not read file: " + filename);
        }
    }

    @Override
    public String path(String filename, String local_path) {
        String root_location = this.storageProperties.getLocation();
        return root_location + "/" + this.propertiesS3.getBucketName() + "/" + local_path + "/" + filename;
    }

    public static File convert(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

}
