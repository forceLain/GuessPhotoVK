package com.forcelain.android.guessphotovk.api;

import java.util.List;

public class PhotoListEntity {
    public PhotoListResponse response;

    public class PhotoListResponse {
        public List<PhotoEntity> items;
    }

    public class PhotoEntity {
        public List<PhotoSizeEntity> sizes;
    }

    public class PhotoSizeEntity{
        public String src;
        public int width;
        public int height;
    }
}
