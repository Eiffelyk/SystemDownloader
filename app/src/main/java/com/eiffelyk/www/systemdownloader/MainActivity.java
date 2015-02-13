package com.eiffelyk.www.systemdownloader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;


/**
 * DownloadManagerDemo
 * create by 馋猫 at 2015年2月13日11:01:55
 */
public class MainActivity extends Activity {

    public static final String DOWNLOAD_FOLDER_NAME = "QQ下载";
    public static final String DOWNLOAD_FILE_NAME = "QQ.apk";

    public static final String APK_URL = "http://gdown.baidu.com/data/wisegame/2c6a60c5cb96c593/QQ_182.apk";
    public static final String KEY_NAME_DOWNLOAD_ID = "downloadId";

    private Button downloadButton;
    private ProgressBar downloadProgress;
    private TextView downloadTip;
    private TextView downloadSize;
    private TextView downloadPercent;
    private Button downloadCancel;

    private MyDownloadManager downloadManagerPro;
    private long downloadId = 0;

    private static MyHandler handler;

    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver completeReceiver;
    private Context context;
    private HashMap<String, String> map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context =this;
        setContentView(R.layout.activity_main);
        handler = new MyHandler();
        downloadManagerPro = new MyDownloadManager((DownloadManager) getSystemService(DOWNLOAD_SERVICE));

        // see android mainfest.xml, accept minetype of cn.trinea.download.file
        Intent intent = getIntent();
        if (intent != null) {
            /**
             * below android 4.2, intent.getDataString() is file:///storage/sdcard1/Trinea/MeLiShuo.apk<br/>
             * equal or above 4.2 intent.getDataString() is content://media/external/file/29669
             */
            Uri data = intent.getData();
            if (data != null) {
                Toast.makeText(context, data.toString(), Toast.LENGTH_LONG).show();
            }
        }

        initView();
        initData();

        downloadObserver = new DownloadChangeObserver();
        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        /** observer download change **/
        getContentResolver().registerContentObserver(MyDownloadManager.CONTENT_URI, true, downloadObserver);
        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(downloadObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(completeReceiver);
    }

    private void initView() {
        downloadButton = (Button) findViewById(R.id.download_button);
        downloadCancel = (Button) findViewById(R.id.download_cancel);
        downloadProgress = (ProgressBar) findViewById(R.id.download_progress);
        downloadTip = (TextView) findViewById(R.id.download_tip);
        downloadSize = (TextView) findViewById(R.id.download_size);
        downloadPercent = (TextView) findViewById(R.id.download_precent);
    }

    private void initData() {
        downloadTip.setText(getString(R.string.tip_download_file) + Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME));
        /**
         * get download id from preferences.<br/>
         * if download id bigger than 0, means it has been downloaded, then query status and show right text;
         */
        downloadId = DownloadingSHaredPreference.getInstance(context).getLong(KEY_NAME_DOWNLOAD_ID);
        updateView();
        downloadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                File folder = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
                if (!folder.exists() || !folder.isDirectory()) {
                    folder.mkdirs();
                }
                if (DownloadingSHaredPreference.getInstance(context).getAllSharedPreferences().isEmpty()) {//判断你是否有正在现在的
                    /**
                     * 如果对下载参数不了解，或者是理解上有问题 详情请见原文链接：http://www.trinea.cn/android/android-downloadmanager/
                     */
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(APK_URL));
                    request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, DOWNLOAD_FILE_NAME);//存储位置、目录、文件名
                    request.setTitle(getString(R.string.download_notification_title));//通知栏标题
                    request.setDescription(getString(R.string.download_notification_description));//通知栏描述
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);//notify的显示形式，可以全部隐藏，可以下载时显示，可以下载完显示，可以下载时和下载完都显示
                    request.setVisibleInDownloadsUi(true);//是否显示当前下载 在系统的下载界面上
                    // request.allowScanningByMediaScanner();//允许媒体抓取
                    // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);//下载网络形式限定
                    //request.setShowRunningNotification(false);//废弃的方法
                    request.setAllowedOverRoaming(true);//移动网络情况下是否允许漫游
                    request.setMimeType("application/cn.trinea.download.file");//设置打开的类型
                    downloadId = downloadManagerPro.getDownloadId(request);
                    /** save download id to preferences **/
                    DownloadingSHaredPreference.getInstance(context).putExtra(KEY_NAME_DOWNLOAD_ID, downloadId);//放入到正在下载中
                    updateView();
                }else{
                    DownloadPrepareSHaredPreference.getInstance(context).putExtra(System.currentTimeMillis()+"", APK_URL);//放入等待中
                    updateView();//更新状态为等待中
                }
                
            }
        });
        downloadCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //查看删除项目 是否正在下载 ,删除
                //查看删除项目 是否在等待下载中 ，删除
                //遍历等待列表去除第一项 加入下载中 开启下载，在等待中删除
                downloadManagerPro.remove(downloadId);
                DownloadingSHaredPreference.getInstance(context).remove(KEY_NAME_DOWNLOAD_ID);
                if(!DownloadPrepareSHaredPreference.getInstance(context).getAllSharedPreferences().isEmpty()){
                    //遍历取出所有，然后比较key大小，最小的在前边
                }
                //重新编译android源码后才能使暂停和恢复下载生效 参考 http://www.trinea.cn/android/android-downloadmanager-pro/
               /* if (downloadCancel.getText().toString().trim().equals("暂停")){
                    downloadManagerPro.pauseDownload(downloadId);
                    downloadCancel.setText("继续");
                }else{
                    downloadManagerPro.resumeDownload(downloadId);
                    downloadCancel.setText("暂停");
                }*/
                updateView();
            }
        });
    }

    /**
     * install app
     * @param context 上下文环境
     * @param filePath 文件路径
     * @return whether apk exist
     */
    public static boolean install(Context context, String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        if (file.length() > 0 && file.exists() && file.isFile()) {
            i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return true;
        }
        return false;
    }

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateView();
        }

    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /**
             * get the id of download which have download success, if the id is my id and it's status is successful,
             * then install it
             **/
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (completeDownloadId == downloadId) {
                switch (action){
                    case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                        initData();
                        updateView();
                        // if download successful, install apk
                        if (downloadManagerPro.getStatusById(downloadId) == DownloadManager.STATUS_SUCCESSFUL) {
                            //加入完成列表，在下载中删除，开始等待中的下载(加入下载中，启动下载，在等待列表中删除)
                            String apkFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DOWNLOAD_FOLDER_NAME + File.separator + DOWNLOAD_FILE_NAME;
                            install(context, apkFilePath);
                        }
                        break;
                    case DownloadManager.ACTION_NOTIFICATION_CLICKED://单击跳转到下载列表页面
                        Intent intent1 = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                        startActivity(intent1);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    public void updateView() {
        int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
        handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
    }

    /**
     * MyHandler
     *
     * @author Trinea 2012-12-18
     */
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    int status = (Integer) msg.obj;
                    if (isDownloading(status)) {
                        downloadProgress.setVisibility(View.VISIBLE);
                        downloadProgress.setMax(0);
                        downloadProgress.setProgress(0);
                        downloadButton.setVisibility(View.GONE);
                        downloadSize.setVisibility(View.VISIBLE);
                        downloadPercent.setVisibility(View.VISIBLE);
                        downloadCancel.setVisibility(View.VISIBLE);
                        if (msg.arg2 < 0) {
                            downloadProgress.setIndeterminate(true);
                            downloadPercent.setText("0%");
                            downloadSize.setText("0M/0M");
                        } else {
                            downloadProgress.setIndeterminate(false);
                            downloadProgress.setMax(msg.arg2);
                            downloadProgress.setProgress(msg.arg1);
                            downloadPercent.setText(getNotiPercent(msg.arg1, msg.arg2));
                            downloadSize.setText(getAppSize(msg.arg1) + "/" + getAppSize(msg.arg2));
                        }
                        if (status == DownloadManager.STATUS_RUNNING) {
                            downloadButton.setText(getString(R.string.app_status_running));
                        } else if (status == DownloadManager.STATUS_PAUSED) {
                            downloadButton.setText(getString(R.string.app_status_paused)); 
                        } else if (status == DownloadManager.STATUS_PENDING) {
                            downloadButton.setText(getString(R.string.等待中));
                        } else {
                            downloadButton.setText(getString(R.string.app_status_unknown));
                        }
                    } else {
                        downloadProgress.setVisibility(View.GONE);
                        downloadProgress.setMax(0);
                        downloadProgress.setProgress(0);
                        downloadButton.setVisibility(View.VISIBLE);
                        downloadSize.setVisibility(View.GONE);
                        downloadPercent.setVisibility(View.GONE);
                        downloadCancel.setVisibility(View.GONE);
                        if (status == DownloadManager.STATUS_FAILED) {
                            downloadButton.setText(getString(R.string.app_status_download_fail));
                        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            downloadButton.setText(getString(R.string.app_status_downloaded));
                        } else {
                            downloadButton.setText(getString(R.string.app_status_download));
                        }
                    }
                    break;
            }
        }
    }

    static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

    public static final int MB_2_BYTE = 1024 * 1024;
    public static final int KB_2_BYTE = 1024;

    /**
     * @param size 文件大小
     * @return  返回转换后的大小
     */
    public static CharSequence getAppSize(long size) {
        if (size <= 0) {
            return "0M";
        }

        if (size >= MB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double) size / MB_2_BYTE)).append("M");
        } else if (size >= KB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double) size / KB_2_BYTE)).append("K");
        } else {
            return size + "B";
        }
    }

    public static String getNotiPercent(long progress, long max) {
        int rate;
        if (progress <= 0 || max <= 0) {
            rate = 0;
        } else if (progress > max) {
            rate = 100;
        } else {
            rate = (int) ((double) progress / max * 100);
        }
        return String.valueOf(rate) + "%";
    }

    public static boolean isDownloading(int downloadManagerStatus) {
        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
    }
}
