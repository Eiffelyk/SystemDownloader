package gridview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import com.eiffelyk.www.systemdownloader.R;

import java.util.ArrayList;


public class GridViewActivity extends Activity {
    private GridView gridView;
    private GridViewAdapter adapter;
    private ArrayList<GridViewBean> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        gridView = (GridView) this.findViewById(R.id.gridView);
        list = new ArrayList<>();
        adapter = new GridViewAdapter(GridViewActivity.this,gridView,list);
        gridView.setAdapter(adapter);
        adapter.updateView(12,list.get(12));//更新当前条目数据
    }
   private ArrayList<GridViewBean> setTestDate(){
        //设置假数据
       return null;
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
