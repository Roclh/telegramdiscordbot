package com.roclh.bot.objectstorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yandex.cloud.api.storage.v1.BucketServiceGrpc;

import java.io.File;

@Service
@Slf4j
public class ObjectStorageSevice {

    private final String url = "https://storage.yandexcloud.net/teleg-disco-storage/";


    public void getBuckets(){
    }

    public void save(File file, String uuid) {
        BucketServiceGrpc.getCreateMethod();

    }

//    public File load(String uuid) {
//
//    }
//
//    public void delete(String uuid) {
//
//    }
}
