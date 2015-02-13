package com.eiffelyk.www.systemdownloader;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

/**
 * 下载进度查询
 * create by trinea
 */
public class MyDownloadManager {

    public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    /**
     * represents downloaded file above api 11 *
     */
    public static final String COLUMN_LOCAL_FILENAME = "local_filename";
    /**
     * represents downloaded file below api 11 *
     */
    public static final String COLUMN_LOCAL_URI = "local_uri";

    private DownloadManager downloadManager;

    public MyDownloadManager(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }
    public long getDownloadId(DownloadManager.Request request){
       return downloadManager.enqueue(request);
    }
    /**
     * get download status
     *
     * @param downloadId long
     * @return 返回下载状态
     */
    public int getStatusById(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_STATUS);
    }

    /**
     * 删除某个下载任务
     * @param ids long
     * @return the number of downloads actually removed
     */
    public int remove(long... ids){
       return downloadManager.remove(ids);
    }

    /**
     * get downloaded byte, total byte
     *
     * @param downloadId long
     * @return a int array with two elements
     * <ul>
     * <li>result[0] represents downloaded bytes, This will initially be -1.</li>
     * <li>result[1] represents total bytes, This will initially be -1.</li>
     * </ul>
     */
    public int[] getDownloadBytes(long downloadId) {
        int[] bytesAndStatus = getBytesAndStatus(downloadId);
        return new int[]{bytesAndStatus[0], bytesAndStatus[1]};
    }

    /**
     * get download status {@link DownloadManager}
     * @param downloadId  ID(long)
     * @return 返回下载状态
     */
    public int getDownloadStatus(long downloadId) {
        int[] bytesAndStatus = getBytesAndStatus(downloadId);
        return bytesAndStatus[2];
    }

    /**
     * get downloaded byte, total byte and download status
     *
     * @param downloadId long
     * @return a int array with three elements
     * <ul>
     * <li>result[0] represents downloaded bytes, This will initially be -1.</li>
     * <li>result[1] represents total bytes, This will initially be -1.</li>
     * <li>result[2] represents download status, This will initially be 0.</li>
     * </ul>
     */
    public int[] getBytesAndStatus(long downloadId) {
        int[] bytesAndStatus = new int[]{-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return bytesAndStatus;
    }



    /**
     * get download file name
     *
     * @param downloadId 文件id
     * @return 文件名（应该是包含文件名的）
     */
    public String getFileName(long downloadId) {
        return getString(downloadId, (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? COLUMN_LOCAL_URI
                : COLUMN_LOCAL_FILENAME));
    }

    /**
     * get download uri
     *
     * @param downloadId 文件id
     * @return get uri maybe startwith("content:")
     */
    public String getUri(long downloadId) {
        return getString(downloadId, DownloadManager.COLUMN_URI);
    }
    /**
     * get failed code or paused reason
     *
     * @param downloadId 文件id
     * @return <ul>
     * <li>if status of downloadId is {@link DownloadManager#STATUS_PAUSED}, return
     * {@link #getPausedReason(long)}</li>
     * <li>if status of downloadId is {@link DownloadManager#STATUS_FAILED}, return {@link #getErrorCode(long)}</li>
     * <li>if status of downloadId is neither {@link DownloadManager#STATUS_PAUSED} nor
     * {@link DownloadManager#STATUS_FAILED}, return 0</li>
     * </ul>
     */
    public int getReason(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_REASON);
    }

    /**
     * get paused reason
     * @param downloadId 文件id
     * @return <ul>
     * <li>if status of downloadId is {@link DownloadManager#STATUS_PAUSED}, return one of
     * {@link DownloadManager#PAUSED_WAITING_TO_RETRY}<br/>
     * {@link DownloadManager#PAUSED_WAITING_FOR_NETWORK}<br/>
     * {@link DownloadManager#PAUSED_QUEUED_FOR_WIFI}<br/>
     * {@link DownloadManager#PAUSED_UNKNOWN}</li>
     * <li>else return {@link DownloadManager#PAUSED_UNKNOWN}</li>
     * </ul>
     */
    public int getPausedReason(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_REASON);
    }

    /**
     * get failed error code
     *
     * @param downloadId 下载文件的唯一标示
     * @return one of {@link DownloadManager#*}
     */
    public int getErrorCode(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_REASON);
    }

    

    /**
     * get string column
     *
     * @param downloadId 下载文件的唯一标示
     * @param columnName 列名
     * @return 查询结果
     */
    private String getString(long downloadId, String columnName) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        String result = null;
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                result = c.getString(c.getColumnIndex(columnName));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    /**
     * get int column
     *
     * @param downloadId 现在文件的唯一标示
     * @param columnName 列名
     * @return  查询结果
     */
    private int getInt(long downloadId, String columnName) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        int result = -1;
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                result = c.getInt(c.getColumnIndex(columnName));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }
}