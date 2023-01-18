package org.sunbird.incredible.processor.store;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import java.io.File;

/**
 * used to upload or downloads files to oci
 */
public class OciStore extends CloudStore {

    private StoreConfig ociStoreConfig;

    private Logger logger = LoggerFactory.getLogger(OciStore.class);

    private BaseStorageService storageService = null;

    private CloudStorage cloudStorage = null;

    private int retryCount = 0;

    public OciStore(StoreConfig ociStoreConfig) {
        this.ociStoreConfig = ociStoreConfig;
        retryCount = Integer.parseInt(ociStoreConfig.getCloudRetryCount());
        init();
    }


    @Override
    public String upload(File file, String path) {
        String uploadPath = getPath(path);
        return cloudStorage.uploadFile(ociStoreConfig.getOciStoreConfig().getContainerName(), uploadPath, file, false, retryCount);
    }

    @Override
    public void download(String fileName, String localPath) {
        cloudStorage.downloadFile(ociStoreConfig.getOciStoreConfig().getContainerName(), fileName, localPath, false);
    }

    private String getPath(String path) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(path);
        if (StringUtils.isNotBlank(ociStoreConfig.getOciStoreConfig().getPath())) {
            stringBuilder.append(ociStoreConfig.getOciStoreConfig().getPath() + "/");
        }
        return stringBuilder.toString();
    }

    @Override
    public String getPublicLink(File file, String uploadPath) {
        String path = getPath(uploadPath);
        return cloudStorage.upload(ociStoreConfig.getOciStoreConfig().getContainerName(), path, file, false, retryCount);
    }

    @Override
    public void init() {
        if (StringUtils.isNotBlank(ociStoreConfig.getType())) {
            String storageKey = ociStoreConfig.getOciStoreConfig().getAccount();
            String storageSecret = ociStoreConfig.getOciStoreConfig().getKey();
            scala.Option<String> storageEndpoint = scala.Option.apply(ociStoreConfig.getOciStoreConfig().getEndpoint());
            scala.Option<String> storageRegion = scala.Option.apply("");
            StorageConfig storageConfig = new StorageConfig(ociStoreConfig.getType(), storageKey, storageSecret,storageEndpoint,storageRegion);
            logger.info("StorageParams:init:all storage params initialized for oci block");
            storageService = StorageServiceFactory.getStorageService(storageConfig);
            cloudStorage = new CloudStorage(storageService);
        } else {
            logger.error("StorageParams:init:provided cloud store type doesn't match supported storage devices:".concat(ociStoreConfig.getType()));
        }

    }

    @Override
    public void close(){
        cloudStorage.closeConnection();
    }
}
