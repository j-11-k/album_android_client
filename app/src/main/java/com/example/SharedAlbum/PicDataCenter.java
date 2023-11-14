package com.example.SharedAlbum;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.MediaStore;

import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PicDataCenter {
    public static class PicData {
        public final String name;
        public final String path;
        public final Date createTime;
        public final int size;
        public final int width;
        public final int height;
        public final String description;

        public PicData(String name, String path, Date createTime, int size, int width, int height) {
            this.name = name;
            this.path = path;
            this.createTime = createTime;
            this.size = size;
            this.width = width;
            this.height = height;
            description = String.format("\t%s  %s  %d x %d", DateFormat.getDateTimeInstance().format(createTime), humanReadableByteCountSI(size), width, height);
        }
    }

    static private List<PicData> localPicMetaList;

    static public List<PicData> picToImageViewer;

    public static List<PicData> GetAllPicData(Context context) {
        if (localPicMetaList == null) {
            getAllPhotoPaths(context);
        }
        return localPicMetaList;
    }

    static void getAllPhotoPaths(Context context) {
        localPicMetaList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
        };
        try (ContentProviderClient client = resolver.acquireContentProviderClient(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)) {
            Cursor cursor = client.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            while (cursor.moveToNext()) {
                PicData picData = new PicData(cursor.getString(0), cursor.getString(1),
                        new Date(cursor.getInt(2) * 1000L), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5));
                localPicMetaList.add(picData);
            }
            cursor.close();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static String humanReadableByteCountSI(int bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMG");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}
