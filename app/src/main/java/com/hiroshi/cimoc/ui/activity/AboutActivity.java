package com.hiroshi.cimoc.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.azhon.appupdate.manager.DownloadManager;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Update;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.AboutPresenter;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.ui.view.AboutView;
import com.hiroshi.cimoc.ui.widget.CustomToast;
import com.hiroshi.cimoc.utils.StringUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class AboutActivity extends BackActivity implements AboutView {

    @BindView(R.id.about_update_summary) TextView mUpdateText;
    @BindView(R.id.about_version_name) TextView mVersionName;
    @BindView(R.id.about_layout) View mLayoutView;
    @BindView(R.id.about_update_auto_switch) SwitchCompat aboutAutoUpdateSwitch;

    private AboutPresenter mPresenter;
    private boolean update = false;
    private boolean checking = false;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new AboutPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersionName.setText(StringUtils.format("version: %s", info.versionName));
            autoUpdateSwitch();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //设置是否自动检查更新App
    private void autoUpdateSwitch() {
        aboutAutoUpdateSwitch.setChecked(mPreference.getBoolean(PreferenceManager.PREF_UPDATE_APP_AUTO, true));
        aboutAutoUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //存放数据
                mPreference.putBoolean(PreferenceManager.PREF_UPDATE_APP_AUTO, isChecked);
                if (isChecked) {
//                    Toast.makeText(AboutActivity.this, "已开启自动检查App更新", Toast.LENGTH_SHORT).show();
                    CustomToast.showToast(AboutActivity.this, "已开启自动检查App更新", 2000);
                }else {
//                    Toast.makeText(AboutActivity.this, "已关闭自动检查App更新", Toast.LENGTH_SHORT).show();
                    CustomToast.showToast(AboutActivity.this, "已关闭自动检查App更新", 2000);
                }
            }
        });
    }

    @OnClick(R.id.about_support_btn) void onSupportClick() {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(null, getString(R.string.about_support_email)));
        showSnackbar(R.string.about_already_clip);
    }

    @OnClick(R.id.about_resource_btn) void onResourceClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_resource_url)));
        try {
            startActivity(intent);
        } catch (Exception e) {
            showSnackbar(R.string.about_resource_fail);
        }
    }

    @OnClick(R.id.about_update_btn) void onUpdateClick() {
        if (update) {
            update();
        } else if (!checking) {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                mUpdateText.setText(R.string.about_update_doing);
                checking = true;
                mPresenter.checkUpdate(info.versionName);
            } catch (Exception e){
                mUpdateText.setText(R.string.about_update_fail);
                checking = false;
            }
        }
    }

    @Override
    public void onUpdateNone() {
        mUpdateText.setText(R.string.about_update_latest);
        checking = false;
    }

    @Override
    public void onUpdateReady() {
//        mUpdateText.setText(R.string.about_update_download);
        update();
        checking = false;
        update = true;
    }

    @Override
    public void onCheckError() {
        mUpdateText.setText(R.string.about_update_fail);
        checking = false;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_about);
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_about;
    }

    private void update() {
        if (Update.update(this)) {
            mUpdateText.setText(R.string.about_update_summary);
        } else {
            showSnackbar(R.string.about_resource_fail);
        }
    }

}
