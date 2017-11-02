package example.com.refreshl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * Created by Administrator on 2017/11/1.
 */

public class RefreshListview extends ListView {
    //包含下拉刷新菜单和顶部轮播图
    private LinearLayout headerView;
    private View ll_pull_down_refresh;
    private ImageView iv_arrow;
    private ProgressBar pb_status;
    private TextView tv_status;
    private TextView tv_time;
    //下拉刷新控件的高度
    private int pulldownrefreshHeight;
    //下拉刷新
    public static final int PULL_DOWN_REFRESH = 0;
    //手松刷新
    public static final int RELEASE_REFRESH = 1;
    //正在刷新
    public static final int REFRESHING = 2;
    //当前的状态
    private int currentStatus = PULL_DOWN_REFRESH;
    private Animation upAnimaition;
    private Animation downAnimaition;
    //加载更多的控件
    private View footview;
    //加载更多控件的高度
    private int footviewHeight;
    //是否已经加载更多
    private boolean isLoadMore = false;
    //顶部轮播图部分
    private View topNewsView;
    //ListView在Y轴上的坐标
    private int listViewOnScreeY = -1;

    public RefreshListview(Context context) {
        super(context);
    }

    public RefreshListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHeaderView(context);
        initAnimation();
        initFooterView(context);
    }

    private void initFooterView(Context context) {
        footview = View.inflate(context,R.layout.refresh_footer,null);
        footview.measure(0,0);
        footviewHeight = footview.getMeasuredHeight();
        footview.setPadding(0,-footviewHeight,0,0);
        //添加ListView
        addFooterView(footview);
        //监听ListView=的滚动
        setOnScrollListener(new MyOnScrollListener());
    }

    public void addTopNewsView(View topNewsView) {
        if (topNewsView != null){
            this.topNewsView = topNewsView;
            headerView.addView(topNewsView);
        }

    }

    

    class MyOnScrollListener implements OnScrollListener{

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            //当静止或者惯性滚动的时候
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING){
                //并且是最后一条可见的
                if (getLastVisiblePosition() == getCount() - 1 ){
                    //1.显示加载更多布局
                    footview.setPadding(8,8,8,8);
                    //2.状态改变
                    isLoadMore = true;
                    //3.回调接口
                    if (mOnRefreshListener != null){
                        mOnRefreshListener.onLoadMore();
                    }
                }
            }
        }
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    }


    private void initAnimation() {
        upAnimaition = new RotateAnimation(0,-180,RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
        upAnimaition.setDuration(500);
        upAnimaition.setFillAfter(true);

        downAnimaition = new RotateAnimation(-180,-360,RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
        downAnimaition.setDuration(500);
        downAnimaition.setFillAfter(true);
    }

    public RefreshListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


    }

    private void initHeaderView(Context context) {
         headerView = (LinearLayout) View.inflate(context, R.layout.refresh_header,null);
        //下拉刷新
        ll_pull_down_refresh = headerView.findViewById(R.id.ll_pull_down_refresh);
        iv_arrow = (ImageView)headerView.findViewById(R.id.iv_arrow);
        pb_status = (ProgressBar) headerView.findViewById(R.id.pb_status);
        tv_status = (TextView) headerView.findViewById(R.id.tv_status);
        tv_time = (TextView) headerView.findViewById(R.id.tv_time);
        //测量
        ll_pull_down_refresh.measure(0,0);
        pulldownrefreshHeight = ll_pull_down_refresh.getMeasuredHeight();
        //默认隐藏下拉刷新控件
        //View.setPadding(0,-控件高,0,0);//完全隐藏
        ll_pull_down_refresh.setPadding(0,-pulldownrefreshHeight,0,0);
        //View.setPadding(0,0,0,0);//完全显示
        //添加头
        addHeaderView(headerView);
    }

    private float startY = -1;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                //记录起始坐标
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (startY == -1){
                    startY = ev.getY();
                }
                //判断顶部轮播图是否完全显示。只有完全显示才会有下拉刷新

                boolean isDisplayTopNews = isDisplayTopNews();
                if (!isDisplayTopNews){
                    //加载更多
                    break;
                }
                //如果正在刷新，就不让再刷新了。
                if (currentStatus == REFRESHING){
                    break;
                }
                //记录新坐标
                float endY = ev.getY();
                //计算滑动的距离
                float distanceY = endY - startY;
                if (distanceY > 0){//下拉
                   // int paddingTop = -控件高 + distanceY;
                    int paddingTop = (int) (-pulldownrefreshHeight + distanceY);

                    if(paddingTop < 0 && currentStatus !=  PULL_DOWN_REFRESH){
                        //下拉刷新
                        currentStatus = PULL_DOWN_REFRESH ;
                        //更新状态
                        refreshViewState();
                    }else if(paddingTop > 0 && currentStatus != RELEASE_REFRESH ){
                        //手松刷新
                        currentStatus  = RELEASE_REFRESH;
                        //更新状态
                        refreshViewState();
                    }
                        ll_pull_down_refresh.setPadding(0,paddingTop,0,0);
                }
                break;
            case MotionEvent.ACTION_UP:
                startY = -1;
                if(currentStatus == PULL_DOWN_REFRESH){
                    ll_pull_down_refresh.setPadding(0,-pulldownrefreshHeight,0,0);//完全隐藏
                }else if(currentStatus == RELEASE_REFRESH ){
                    //设置状态为正在刷新
                    currentStatus = REFRESHING;
                    refreshViewState();
                    //完全显示
                    ll_pull_down_refresh.setPadding(0,0,0,0);
                    //回调接口
                    if(mOnRefreshListener != null){
                        mOnRefreshListener.onPullDownRefresh();
                    }
            }
                break;
        }
        return super.onTouchEvent(ev);
    }

    //判断是否完全显示顶部轮播图
    private boolean isDisplayTopNews() {
        //1.得到ListView在屏幕上的坐标
        int[] location = new int[2];
        if (listViewOnScreeY == -1){
            getLocationOnScreen(location);
            listViewOnScreeY = location [1];
        }
        //2.得到顶部轮播屯在屏幕上的坐标
        topNewsView.getLocationOnScreen(location);
        int topNewsViewOnScreenY = location[1];
        /*if (listViewOnScreeY <= topNewsViewOnScreenY){
            return true;
        }else{
            return false;
        }*/
        return listViewOnScreeY <= topNewsViewOnScreenY;
    }

    private void refreshViewState(){
        switch (currentStatus){
            case PULL_DOWN_REFRESH://下拉刷新
                iv_arrow.startAnimation(downAnimaition);
                tv_status.setText("下拉刷新...");
                pb_status.setVisibility(GONE);
                break;
            case RELEASE_REFRESH://手松刷新
                iv_arrow.startAnimation(upAnimaition);
                tv_status.setText("手松刷新...");
                pb_status.setVisibility(GONE);
                break;
            case REFRESHING://正在刷新
                tv_status.setText("正在刷新...");
                pb_status.setVisibility(VISIBLE);
                iv_arrow.clearAnimation();
                iv_arrow.setVisibility(GONE);
                break;
        }
    }


    public void onRefreshFinish(boolean sucess) {
        if (isLoadMore){
            //加载更多
            isLoadMore = false;
            //隐藏加载更多布局
            footview.setPadding(0,-footviewHeight,0,0);
        }else{
            //下拉刷新
            tv_status.setText("下拉刷新...");
            currentStatus = PULL_DOWN_REFRESH;
            iv_arrow.clearAnimation();
            pb_status.setVisibility(GONE);
            iv_arrow.setVisibility(VISIBLE);
            ll_pull_down_refresh.setPadding(0,-pulldownrefreshHeight,0,0);
        }
        if (sucess){
            //设置最新更新时间
            tv_time.setText("上次更新时间:"+getSystemTime());
        }
    }

    private String getSystemTime() {
            SimpleDateFormat format = new SimpleDateFormat("yyyyy-MM-dd HH:mm:ss");
            return format.format(new Date());
    }

    public interface OnRefreshListener{
        //当下拉刷新的时候回调这个方法
        public void onPullDownRefresh();

        public void onLoadMore();
    }

    private OnRefreshListener  mOnRefreshListener;

    public void setOnRefreshListener(OnRefreshListener l){
        this.mOnRefreshListener = l;
    }
}
