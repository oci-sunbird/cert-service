package org.sunbird.incredible.processor.store;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import java.io.File;

/**
 * used to upload or downloads files to aws
 */
public class AwsStore extends CloudStore {

    private StoreConfig awsStoreConfig;

    private Logger logger = LoggerFactory.getLogger(AwsStore.class);

    private BaseStorageService storageService = null;

    private CloudStorage cloudStorage = null;

    private int retryCount = 0;

    public AwsStore(StoreConfig awsStoreConfig) {
        this.awsStoreConfig = awsStoreConfig;
        retryCount = Integer.parseInt(awsStoreConfig.getCloudRetryCount());
        init();
    }


    @Override
    public String upload(File file, String path) {
        String uploadPath = getPath(path);
        return cloudStorage.uploadFile(awsStoreConfig.getAwsStoreConfig().getContainerName(), uploadPath, file, false, retryCount);
    }

    @Override
    public void download(String fileName, String localPath) {
        cloudStorage.downloadFile(awsStoreConfig.getAwsStoreConfig().getContainerName(), fileName, localPath, false);
    }

    private String getPath(String path) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(path);
        if (StringUtils.isNotBlank(awsStoreConfig.getAwsStoreConfig().getPath())) {
            stringBuilder.append(awsStoreConfig.getAwsStoreConfig().getPath() + "/");
        }
        return stringBuilder.toString();
    }

    @Override
    public String getPublicLink(File file, String uploadPath) {
        String path = getPath(uploadPath);
        return cloudStorage.upload(awsStoreConfig.getAwsStoreConfig().getContainerName(), path, file, false, retryCount);
    }

    @Override
    public void init() {
        if (StringUtils.isNotBlank(awsStoreConfig.getType())) {
            String storageKey = awsStoreConfig.getAwsStoreConfig().getAccount();
            String storageSecret = awsStoreConfig.getAwsStoreConfig().getKey();
            scala.Option<String> storageEndpoint = scala.Option.apply("");
            scala.Option<String> storageRegion = scala.Option.apply("");
            StorageConfig storageConfig = new StorageConfig(awsStoreConfig.getType(), storageKey, storageSecret,storageEndpoint,storageRegion);
            logger.info("StorageParams:init:all storage params initialized for aws block");
            storageService = StorageServiceFactory.getStorageService(storageConfig);
            cloudStorage = new CloudStorage(storageService);
        } else {
            logger.error("StorageParams:init:provided cloud store type doesn't match supported storage devices:".concat(awsStoreConfig.getType()));
        }

    }

    @Override
    public void close(){
        cloudStorage.closeConnection();
    }
}
