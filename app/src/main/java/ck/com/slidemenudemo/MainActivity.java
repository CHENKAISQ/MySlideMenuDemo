package ck.com.slidemenudemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {


    private ListView menuListview;
    private ImageView ivHead;
    private ListView mainListview;
    private SlideMenu slideMenu;
    public static boolean stopChild = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        menuListview = (ListView) findViewById(R.id.menu_listview);
        mainListview = (ListView) findViewById(R.id.main_listview);
        ivHead = (ImageView) findViewById(R.id.iv_head);
        slideMenu = (SlideMenu) findViewById(R.id.slideMenu);

        //填充数据
        menuListview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1
                , Constant.sCheeseStrings) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                //偷梁换柱
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                return view;
            }
        });

//        mainListview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1
//                , Constant.NAMES));
        mainListview.setAdapter(new MyAdapter());

        //设置滑动改变的监听器
        slideMenu.setOnSlideChangeListener(new SlideMenu.OnSlideChangeListener() {
            @Override
            public void onOpen() {
                Toast.makeText(MainActivity.this, "芝麻开门", Toast.LENGTH_SHORT).show();
                stopChild = true;
            }

            @Override
            public void onClose() {
                stopChild = false;
                Toast.makeText(MainActivity.this, "关门大吉", Toast.LENGTH_SHORT).show();
                //使用平移的属性动画
                ViewCompat.animate(ivHead)
                        .translationXBy(100)
                        .setDuration(1000)
//                          .setInterpolator(new CycleInterpolator(14))
//                          .setInterpolator(new OvershootInterpolator(4))
                        .setInterpolator(new BounceInterpolator())//乒乓球落地
                        .start();
            }

            @Override
            public void onDraging(float fraction) {
//                Log.e("tag","fraction: "+fraction);
                ivHead.setRotation(720 * fraction);
            }
        });

    }
    SwipeLayout currentLayout = null;//用来记录当前打开的SwipeLayout
    class MyAdapter extends BaseAdapter implements SwipeLayout.OnSwipeListener{
        @Override
        public int getCount() {
            return Constant.NAMES.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView== null){
                convertView = View.inflate(parent.getContext(), R.layout.adapter_list, null);
                holder = new ViewHolder();
                holder.swipeLayout = (SwipeLayout)convertView.findViewById(R.id.swipeLayout);
                holder.tvDelete = (TextView) convertView.findViewById(R.id.tv_delete);
                holder.tvName = (TextView)convertView.findViewById(R.id.tv_name);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvName.setText(Constant.NAMES[position]);
            //给SwipeLayout添加打开关闭的监听器
            holder.swipeLayout.setOnSwipeListener(this);

            return convertView;
        }
        @Override
        public void onOpen(SwipeLayout layout) {
            //先关闭之间已经打开的
            if(currentLayout!=null && currentLayout!=layout){
                currentLayout.close();
            }
            Toast.makeText(MainActivity.this, "打开", Toast.LENGTH_SHORT).show();
            //记录一下
            currentLayout = layout;
            layout.changeState(true);
        }
        @Override
        public void onClose(SwipeLayout layout) {
            layout.changeState(false);
            //关闭的时候清除一下
            if(currentLayout==layout){
                currentLayout = null;
            }
            Toast.makeText(MainActivity.this, "关闭", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onTouchDown(SwipeLayout swipeLayout) {
            if(currentLayout!=null&&currentLayout!=swipeLayout){
                currentLayout.close();
            }
        }
    }
    static class ViewHolder {
        TextView tvName;
        TextView tvDelete;
        SwipeLayout swipeLayout;
    }

    public  boolean isStopChild() {
        return stopChild;
    }
}
