package com.cwj.we.module.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.cwj.we.BuildConfig;
import com.cwj.we.R;
import com.cwj.we.base.BaseActivity;
import com.cwj.we.bean.EventBG;
import com.cwj.we.bean.LatestBean;
import com.cwj.we.module.fragment.GamesFragment;
import com.cwj.we.module.fragment.ToolFragment;
import com.cwj.we.module.fragment.UsFragment;
import com.cwj.we.utils.ToastUtil;
import com.cwj.we.view.TabView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.ycbjie.ycupdatelib.AppUpdateUtils;
import com.ycbjie.ycupdatelib.PermissionUtils;
import com.ycbjie.ycupdatelib.UpdateFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class HomeActivity extends BaseActivity<HomePrensenter> implements HomeView {

    @BindView(R.id.tab_tool)
    TabView tabTool;
    @BindView(R.id.tab_games)
    TabView tabGames;
    @BindView(R.id.tab_us)
    TabView tabUs;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.iv_bg)
    ImageView ivBg;

    private List<TabView> mTabViews = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    private String string;
    private int REQUEST_SD = 200;
    private LatestBean updataBean;
    private static final int INDEX_US = 0;
    private static final int INDEX_GAMES = 1;
    private static final int INDEX_TOOL = 2;

    //这个是你的包名
    private static final String apkName = "yilu";
    private static final String[] mPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private BasePopupView basePopupView;
    SharedPreferences sprfMain;

    @Override
    protected HomePrensenter createPresenter() {
        return new HomePrensenter(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    public void initView() {
        EventBus.getDefault().register(this);
        sprfMain = this.getSharedPreferences("counter", Context.MODE_PRIVATE);
        //设置背景
        if (TextUtils.isEmpty(sprfMain.getString("path", ""))) {
            Glide.with(this).load(R.drawable.we_bg).into(ivBg);
        } else {
            Glide.with(this).load(Uri.fromFile(new File(sprfMain.getString("path", "")))).into(ivBg);
        }

        UsFragment usFragment = new UsFragment();
        GamesFragment gamesFragment = new GamesFragment();
        ToolFragment toolFragment = new ToolFragment();

        fragments.add(usFragment);
        fragments.add(gamesFragment);
        fragments.add(toolFragment);

        mTabViews.add(tabUs);
        mTabViews.add(tabGames);
        mTabViews.add(tabTool);

        viewpager.setOffscreenPageLimit(fragments.size() - 1);
        viewpager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), fragments));
        viewpager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 左边View进行动画
                mTabViews.get(position).setXPercentage(1 - positionOffset);
                // 如果positionOffset非0，那么就代表右边的View可见，也就说明需要对右边的View进行动画
                if (positionOffset > 0) {
                    mTabViews.get(position + 1).setXPercentage(positionOffset);
                }
            }
        });

        //底部导航背景透明度设置0~255
        llBottom.getBackground().setAlpha(100);

        //通知用户开启通知
        NotificationManagerCompat notification = NotificationManagerCompat.from(this);
        boolean isEnabled = notification.areNotificationsEnabled();
        if (!isEnabled) {
            openTongzhi();
        }

        if (basePopupView == null) {
            presenter.latest("5fc866b023389f0c69e23c24", "6570963ae9a308ca993393518f865887");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventBG eventBG) {
        switch (eventBG.getType()) {
            case "EVENT_CZ_BG":
                Glide.with(this).load(R.drawable.we_bg).into(ivBg);
                break;
            case "EVENT_SZ_BG":
                Glide.with(this).load(Uri.fromFile(new File(eventBG.getUserIconPath()))).into(ivBg);
                break;
        }
    }

    private void openTongzhi() {
        //未打开通知
        basePopupView = new XPopup.Builder(this).asConfirm("提示", "请在“通知”中打开通知权限以便观察应用更新进度",
                () -> {
                    Intent intent = new Intent();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("android.provider.extra.APP_PACKAGE", HomeActivity.this.getPackageName());
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //5.0
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("app_package", HomeActivity.this.getPackageName());
                        intent.putExtra("app_uid", HomeActivity.this.getApplicationInfo().uid);
                        startActivity(intent);
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {  //4.4
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + HomeActivity.this.getPackageName()));
                    } else if (Build.VERSION.SDK_INT >= 15) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.fromParts("package", HomeActivity.this.getPackageName(), null));
                    }
                    startActivity(intent);
                })
                .show();
    }

    /*获取本地软件版本号​*/
    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SD) {
            updata();
        }
    }

    @Override
    public void initData() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateCurrentTab(int index) {
        for (int i = 0; i < mTabViews.size(); i++) {
            if (index == i) {
                mTabViews.get(i).setXPercentage(1);
            } else {
                mTabViews.get(i).setXPercentage(0);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.tab_tool, R.id.tab_games, R.id.tab_us})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tab_tool:
                viewpager.setCurrentItem(INDEX_TOOL, false);
                updateCurrentTab(INDEX_TOOL);
                break;
            case R.id.tab_games:
                viewpager.setCurrentItem(INDEX_GAMES, false);
                updateCurrentTab(INDEX_GAMES);
                break;
            case R.id.tab_us:
                viewpager.setCurrentItem(INDEX_US, false);
                updateCurrentTab(INDEX_US);
                break;
        }
    }

    @Override
    public void latestData(LatestBean bean) {
        if (bean != null) {
            string = bean.getDirect_install_url();
            updataBean = bean;
            updata();
        }
    }

    private void updata() {
        if (getLocalVersion(HomeActivity.this) < Integer.parseInt(updataBean.getBuild())) {
            PermissionUtils.init(this);
            boolean granted = PermissionUtils.isGranted(mPermission);
            if (!granted) {
                PermissionUtils permission = PermissionUtils.permission(mPermission);
                permission.callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        if (basePopupView == null) {
                            presenter.latest("5fc866b023389f0c69e23c24", "6570963ae9a308ca993393518f865887");
                        }
                    }

                    @Override
                    public void onDenied() {
                        PermissionUtils.openAppSettings();
                        ToastUtil.showTextToast(HomeActivity.this, "请允许存储权限");
                    }
                });
                permission.request();
            } else {
                //设置自定义下载文件路径
                AppUpdateUtils.APP_UPDATE_DOWN_APK_PATH = "apk" + File.separator + "downApk";
                UpdateFragment.showFragment(this, false, string, apkName, updataBean.getChangelog(), BuildConfig.APPLICATION_ID, null);
            }
        }
    }

    @Override
    public void onError(String msg) {
        ToastUtil.showTextToast(this, msg);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> frags;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> frags) {
            super(fm);
            this.frags = frags;
        }

        @Override
        public Fragment getItem(int i) {
            return frags.get(i);
        }

        @Override
        public int getCount() {
            return frags.size();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 再按一次退出程序
     */
    private long currentBackPressedTime = 0;
    private static int BACK_PRESSED_INTERVAL = 2000;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - currentBackPressedTime > BACK_PRESSED_INTERVAL) {
                currentBackPressedTime = System.currentTimeMillis();
                ToastUtil.showTextToast(this, "再按一次退出程序");
                return true;
            } else {//退出程序
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return false;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
