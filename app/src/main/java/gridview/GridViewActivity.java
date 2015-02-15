package gridview;

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
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.eiffelyk.www.systemdownloader.DownloadPrepareSHaredPreference;
import com.eiffelyk.www.systemdownloader.DownloadedSHaredPreference;
import com.eiffelyk.www.systemdownloader.DownloadingSHaredPreference;
import com.eiffelyk.www.systemdownloader.MyDownloadManager;
import com.eiffelyk.www.systemdownloader.R;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;


public class GridViewActivity extends Activity {
    private GridView gridView;
    private GridViewAdapter adapter;
    private ArrayList<GridViewBean> list;
    private Context context;
    private MyDownloadManager downloadManagerPro;
    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver completeReceiver;
    public static final String DOWNLOAD_FOLDER_NAME = "测试下载";
    public static final String KEY_NAME_DOWNLOAD_ID = "downloadId";  //正在下载的sharedpreference的存储ID
    public static final String KEY_NAME_DOWNLOAD_OBJ = "downloadOBJ";  //正在下载的sharedpreference的存储对象
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        context = getApplicationContext();
        downloadManagerPro = new MyDownloadManager((DownloadManager) getSystemService(DOWNLOAD_SERVICE));
        downloadObserver = new DownloadChangeObserver();
        gridView = (GridView) this.findViewById(R.id.gridView);
        list = new ArrayList<>();
        setTestDate();
        adapter = new GridViewAdapter(GridViewActivity.this,gridView,list);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("馋猫", "点击效果实现==" + position);
                downLoadStart(list.get(position));
            }
        });
        //adapter.updateView(12,list.get(12));//更新当前条目数据
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        /** observer download change **/
        getContentResolver().registerContentObserver(MyDownloadManager.CONTENT_URI, true, downloadObserver);
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
    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(mHandler);
        }

        @Override
        public void onChange(boolean selfChange) {
                updateView();
        }
    }
    private boolean downLoadStart(GridViewBean gridViewBean){
        File folder = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        if (DownloadingSHaredPreference.getInstance(GridViewActivity.this).getAllSharedPreferences().isEmpty()) {//判断你是否有正在现在的
            /**
             * 如果对下载参数不了解，或者是理解上有问题 详情请见原文链接：http://www.trinea.cn/android/android-downloadmanager/
             */
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(gridViewBean.getDownloadUrl()));
            request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, gridViewBean.getTitle());//存储位置、目录、文件名
            //request.setTitle(getString(R.string.app_name));//通知栏标题
            request.setTitle(gridViewBean.getTitle());//通知栏标题
            request.setDescription(gridViewBean.getTitle());//通知栏描述
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);//notify的显示形式，可以全部隐藏，可以下载时显示，可以下载完显示，可以下载时和下载完都显示
            request.setVisibleInDownloadsUi(true);//是否显示当前下载 在系统的下载界面上
            // request.allowScanningByMediaScanner();//允许媒体抓取
            // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);//下载网络形式限定
            //request.setShowRunningNotification(false);//废弃的方法
            request.setAllowedOverRoaming(true);//移动网络情况下是否允许漫游
            request.setMimeType("application/cn.trinea.download.file");//设置打开的类型
            //downloadId = downloadManagerPro.getDownloadId(request);
            /** save download id to preferences **/
            DownloadingSHaredPreference.getInstance(GridViewActivity.this).putExtra(KEY_NAME_DOWNLOAD_ID, downloadManagerPro.getDownloadId(request));//放入到正在下载中
            gridViewBean.setStatus(1);
            DownloadingSHaredPreference.getInstance(GridViewActivity.this).putExtraObj(KEY_NAME_DOWNLOAD_OBJ, gridViewBean);//放入到正在下载中
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == gridViewBean.getId()) {
                    adapter.updateView(i, gridViewBean);
                }
            }
            return true;
        } else {
            gridViewBean.setStatus(4);
            DownloadPrepareSHaredPreference.getInstance(GridViewActivity.this).putExtraObj(System.currentTimeMillis() + "", gridViewBean);//放入等待中
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == gridViewBean.getId()) {
                    adapter.updateView(i, gridViewBean);
                }
            }
            return false;
        }
    }

    private void downLoadComplete() {
        // TODO: 2015/2/15 解压缩 
        //加入完成
        GridViewBean gridViewBean = DownloadingSHaredPreference.getInstance(GridViewActivity.this).getObj(KEY_NAME_DOWNLOAD_OBJ);
        DownloadedSHaredPreference.getInstance(GridViewActivity.this).putExtraObj(gridViewBean.getId() + "", gridViewBean);
        //删除正在下载中的
        DownloadingSHaredPreference.getInstance(GridViewActivity.this).clearAllSharedPreferences();
        gridViewBean.setStatus(2);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == gridViewBean.getId()) {
                adapter.updateView(i, gridViewBean);
            }
        }
        //从等待中拿去最开始加入的，放入下载中
        if (!DownloadPrepareSHaredPreference.getInstance(GridViewActivity.this).getAllSharedPreferences().isEmpty()){
            String key = (String) getMinKey(DownloadPrepareSHaredPreference.getInstance(GridViewActivity.this).getAllSharedPreferences());
            GridViewBean gridViewBean1 =  DownloadPrepareSHaredPreference.getInstance(GridViewActivity.this).getObj(key);
            if(downLoadStart(gridViewBean1)){
                DownloadPrepareSHaredPreference.getInstance(GridViewActivity.this).remove(key);
            }
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
            if (completeDownloadId == DownloadingSHaredPreference.getInstance(context).getLong(KEY_NAME_DOWNLOAD_ID)) {
                switch (action) {
                    case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                        //initData();
                        //updateView();
                        // if download successful, install apk
                        if (downloadManagerPro.getStatusById(DownloadingSHaredPreference.getInstance(context).getLong(KEY_NAME_DOWNLOAD_ID)) == DownloadManager.STATUS_SUCCESSFUL) {
                            //加入完成列表，在下载中删除，开始等待中的下载(加入下载中，启动下载，在等待列表中删除)
                            //String apkFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DOWNLOAD_FOLDER_NAME + File.separator + DOWNLOAD_FILE_NAME;
                           downLoadComplete();
                            
                            //install(context, apkFilePath);
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
    public static Object getMinKey(Map<String, ?> map) {
        if (map == null) return null;
        Set<String> set = map.keySet();
        Object[] obj = set.toArray();
        Arrays.sort(obj);
        return obj[0];
    }
    public void updateView() {
        int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(DownloadingSHaredPreference.getInstance(GridViewActivity.this).getLong(KEY_NAME_DOWNLOAD_ID));
        mHandler.sendMessage(mHandler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
    }
    private android.os.Handler mHandler = new MyHandler(this);
    private static class MyHandler extends android.os.Handler {
        private final WeakReference<Activity> myactivity;

        public MyHandler(Activity activity) {
            myactivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (myactivity == null) {
                return;
            }
            GridViewActivity activity = (GridViewActivity) myactivity.get();
            switch (msg.what) {
                case 0:
                    GridViewBean gridViewBean = (GridViewBean)DownloadingSHaredPreference.getInstance(activity.context).getObj(KEY_NAME_DOWNLOAD_OBJ);
                    Log.e("馋猫","gridViewBean=handleMessage="+gridViewBean.toString());
                    if (gridViewBean != null) {
                        Log.e("馋猫", "下载ing中存储的bean==" + gridViewBean.toString());
                        //0 未下载（包括更新）1 正在下载，2 下载完成，3 暂停中，4 等待下载
                        int status = (Integer) msg.obj;
                        //if (status)
                        switch (status){
                            case DownloadManager.STATUS_PAUSED:
                                gridViewBean.setDownSize(msg.arg1);
                                gridViewBean.setTotalSize(msg.arg2);
                                gridViewBean.setStatus(3);
                                break;
                            case DownloadManager.STATUS_FAILED:
                                // TODO: 2015/2/15 下载失败 
                                gridViewBean.setStatus(-1);
                                break;
                            case DownloadManager.STATUS_PENDING:
                                gridViewBean.setStatus(4);
                                break;
                            case DownloadManager.STATUS_RUNNING:
                                gridViewBean.setStatus(3);
                                gridViewBean.setDownSize(msg.arg1);
                                gridViewBean.setTotalSize(msg.arg2);
                                break;
                            case DownloadManager.STATUS_SUCCESSFUL:
                                gridViewBean.setStatus(2);
                                break;
                            default:
                                gridViewBean.setStatus(-1);
                                break;
                        }
                        gridViewBean.setDownSize(msg.arg1);
                        gridViewBean.setTotalSize(msg.arg2);
                    }
                    for (int i = 0; i < activity.list.size(); i++) {
                        if (activity.list.get(i).getId() == gridViewBean.getId()) {
                            activity.adapter.updateView(i, gridViewBean);
                        }
                    }
                    break;
            }
        }
    }
   private void setTestDate(){
        //设置假数据
       GridViewBean gridViewBean1 =new GridViewBean(1,"a1","","http://gdown.baidu.com/data/wisegame/4f9b25fb0e093ac6/QQ_220.apk",11);
       GridViewBean gridViewBean2 =new GridViewBean(2,"a2","","http://gdown.baidu.com/data/wisegame/c98912e6d2015aa1/baiduditu_620.apk",11);
       GridViewBean gridViewBean3 =new GridViewBean(3,"a3","","http://gdown.baidu.com/data/wisegame/067375b3571d3ecf/aiqiyishipin_80610.apk",11);
       GridViewBean gridViewBean4 =new GridViewBean(4,"a4","","http://gdown.baidu.com/data/wisegame/d1f3879f50dfc991/wenguozhuomian_4011002.apk",11);
       GridViewBean gridViewBean5 =new GridViewBean(5,"a5","","http://gdown.baidu.com/data/wisegame/5eb8e9609424f1a3/yiqibazisuanmingdashi_22.apk",11);
       GridViewBean gridViewBean6 =new GridViewBean(6,"a6","","http://gdown.baidu.com/data/wisegame/f8c80f7da168e32e/chongwushuo_9.apk",11);
       list.add(gridViewBean1);
       list.add(gridViewBean2);
       list.add(gridViewBean3);
       list.add(gridViewBean4);
       list.add(gridViewBean5);
       list.add(gridViewBean6);
    }
    
    // TODO: 2015/2/13  接收数据变化的DownloadChangeObserver
    // TODO: 2015/2/13  接收系统发出的下载更提示进度的BroadcastReceiver
    // TODO: 2015/2/13  BroadcastReceiver 注册成长期后台运行的
    // TODO: 2015/2/13  下载准备判断，下载中判断，现在完成判断
    // TODO: 2015/2/13  测试sharedPreference存储 Object 是否可行
    // TODO: 2015/2/13  需要注意的是准备下载的sharedPreference key 使用的是时间毫秒值，之后片段要用
    // TODO: 2015/2/13  需要注意的是完成下载的sharedPreference key 使用的是数据的id，在判断是否下载已经下载完成可以根据key直接判断，然后获取 version去判断是否更新
    // TODO: 2015/2/13  正在进行的key使用的是一个固定值，这样可以保证是唯一值而且获取下载进度状态的时候判断更加简单
    // TODO: 2015/2/13  完成后可以之存储id和版本号。其他数据可以不用存储
    // TODO: 2015/2/13  准备下载的数据需要存储整个Object
    // TODO: 2015/2/13  准备下载中需要存储的是id和longId
}
