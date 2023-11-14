package com.example.SharedAlbum;

import java.util.Date;
import java.util.List;

public class AlbumData {
    public final int albumId;
    public final String albumName;
    public final String coverThumbnailUrl;
    public List<RemotePicData> containedPics;

    public AlbumData(int albumId, String albumName, String coverThumbnailUrl) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.coverThumbnailUrl = coverThumbnailUrl;
    }

    public static class RemotePicData extends PicDataCenter.PicData {
        public final String thumbPath;
        public final String superThumbPath;
        public final String uploaderName;

        public RemotePicData(String name, String path, Date createTime, int size, int width, int height,
                             String thumbPath, String superThumbPath, String uploaderName) {
            super(name, path, createTime, size, width, height);
            this.thumbPath = thumbPath;
            this.superThumbPath = superThumbPath;
            this.uploaderName = uploaderName;
        }
    }

    public static List<AlbumData> getAlbumList(){
        AlbumData data1,data2,data3;
        data1 = new AlbumData(1,"相册1","/storage/emulated/0/DCIM/Screenshots/Screenshot_2023-06-30-12-24-08-056_com.example.homenetserver.jpg");
        data2 = new AlbumData(2,"相册2","/storage/emulated/0/Pictures/Twitter/20211111_081307.jpg");
        data3 = new AlbumData(3,"相册3","/storage/emulated/0/DCIM/Camera/IMG_20211005_104656.jpg");
        return List.of(data1,data2,data3);
    }
}
