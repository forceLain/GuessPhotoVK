package com.forcelain.android.guessphotovk.api;

import java.util.List;

public class PhotoListEntity {
    public PhotoListResponse response;

    public class PhotoListResponse {
        public List<PhotoEntity> items;
    }

}
