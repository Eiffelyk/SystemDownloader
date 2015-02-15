package gridview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eiffelyk.www.systemdownloader.DownloadedSHaredPreference;
import com.eiffelyk.www.systemdownloader.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *下载展示类
 * Created by 猫 on 2015/2/13.
 */
public class GridViewAdapter extends BaseAdapter{
    private ArrayList<GridViewBean> list;
    private Context context;
    private LayoutInflater inflater;
    private ViewHolder viewHolder;
    private GridView gridView;
    public GridViewAdapter(Context context,GridView gridView,ArrayList<GridViewBean> list) {
        this.list = list;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.gridView =gridView;
        Log.e("馋猫","list.size=="+this.list.size());
    }
    
    @Override
    public int getCount() {
        return list.size()!=0 ? list.size():0;
    }

    @Override
    public Object getItem(int position) {
            return list.get(position) != null ? list.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    /**
     * 更新listView单条数据
     *
     * @param index 当前条目的索引值
     */
    public void updateView(int index,GridViewBean gridViewBean) {
        int visiblePos = gridView.getFirstVisiblePosition();
        int offset = index - visiblePos;
        if (offset < 0) return;
        View view = gridView.getChildAt(offset);
        ViewHolder holder = (ViewHolder) view.getTag();
        setDate(holder, gridViewBean);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.act_gridview_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final GridViewBean gridViewBean = list.get(position);
        setDate(viewHolder, gridViewBean);
        return convertView;
    }

    private void setDate(ViewHolder viewHolder, GridViewBean gridViewBean) {
        viewHolder.download_image.setImageResource(R.mipmap.ic_launcher);
        //未下载
        if (gridViewBean.getStatus()== 0){
            viewHolder.download_size.setVisibility(View.GONE);
            viewHolder.download_percent.setVisibility(View.GONE);
            viewHolder.progressBar_show.setVisibility(View.GONE);
            //DownloadedSHaredPreference.getInstance(context).getAllSharedPreferences();
            GridViewBean gridViewBean1 = DownloadedSHaredPreference.getInstance(context).getObj(gridViewBean.getId()+"");
            if (gridViewBean1!=null&&gridViewBean.getVersion() > gridViewBean1.getVersion()) {
                viewHolder.download_button.setText("更新");
            } else {
                viewHolder.download_button.setText("下载");
            }
            viewHolder.download_progress.setBackgroundColor(context.getResources().getColor(R.color.act_login_btn_press));
        }else if(gridViewBean.getStatus() == 1){
            //下载中
            viewHolder.download_size.setVisibility(View.VISIBLE);
            viewHolder.download_percent.setVisibility(View.VISIBLE);
            viewHolder.download_size.setText(getAppSize(gridViewBean.getDownSize()) + "/" + getAppSize(gridViewBean.getTotalSize()));
            viewHolder.download_percent.setText(getNotiPercent(gridViewBean.getDownSize(), gridViewBean.getTotalSize()));
            viewHolder.progressBar_show.setVisibility(View.VISIBLE);
            viewHolder.download_progress.setIndeterminate(true);
            viewHolder.download_button.setText(getNotiPercent(gridViewBean.getDownSize(), gridViewBean.getDownSize()));
            viewHolder.download_progress.setBackgroundColor(context.getResources().getColor(R.color.act_login_btn_press));
            if (gridViewBean.getTotalSize()>0){
             //正在下载有进度
                viewHolder.download_progress.setIndeterminate(false);
                viewHolder.download_progress.setMax(gridViewBean.getDownSize());
                viewHolder.download_progress.setProgress(gridViewBean.getTotalSize());
            }else{
                viewHolder.progressBar_show.setIndeterminate(true);
                viewHolder.download_progress.setIndeterminate(true);
                viewHolder.download_size.setText("0%");
                viewHolder.download_percent.setText("0KB/KB");
            }
        }else if(gridViewBean.getStatus()==2){
            //下载完成
            viewHolder.download_size.setVisibility(View.GONE);
            viewHolder.download_percent.setVisibility(View.GONE);
            viewHolder.progressBar_show.setVisibility(View.GONE);
            viewHolder.download_button.setText("已下载");
            viewHolder.download_progress.setBackgroundColor(context.getResources().getColor(R.color.act_good_btn_unpress));
        }else if(gridViewBean.getStatus() == 3){
           //暂停中 
            viewHolder.download_button.setText("暂停中");
            viewHolder.download_progress.setIndeterminate(false);
            viewHolder.progressBar_show.setIndeterminate(true);
            viewHolder.download_progress.setMax(gridViewBean.getDownSize());
            viewHolder.download_progress.setProgress(gridViewBean.getTotalSize());
        }else if (gridViewBean.getStatus()==4){
            //等待下载
            viewHolder.download_button.setText("等待...");
            viewHolder.download_size.setVisibility(View.GONE);
            viewHolder.download_percent.setVisibility(View.GONE);
            viewHolder.progressBar_show.setVisibility(View.GONE);
            viewHolder.download_progress.setBackgroundColor(context.getResources().getColor(R.color.act_login_btn_press));
        }else {
            viewHolder.download_button.setText("下载失败");
            viewHolder.download_size.setVisibility(View.GONE);
            viewHolder.download_percent.setVisibility(View.GONE);
            viewHolder.progressBar_show.setVisibility(View.GONE);
        }
        
           
        
        
    }
    private class ViewHolder {
        private ImageView download_image;
        private TextView download_percent;
        private TextView download_size;
        private ProgressBar download_progress;
        private ProgressBar progressBar_show;
        private Button download_button;

        public ViewHolder(View view) {
            download_image = (ImageView) view.findViewById(R.id.download_image);
            download_percent = (TextView) view.findViewById(R.id.download_precent);
            download_size = (TextView) view.findViewById(R.id.download_size);
            download_progress = (ProgressBar) view.findViewById(R.id.download_progress);
            progressBar_show = (ProgressBar) view.findViewById(R.id.progressBar_show);
            download_button = (Button) view.findViewById(R.id.download_button);
            download_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("馋猫","按钮别点击了");
                }
            });
        }
    }

    static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

    public static final int MB_2_BYTE = 1024 * 1024;
    public static final int KB_2_BYTE = 1024;
    /**
     * @param size 文件大小
     * @return 返回转换后的大小
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
}
