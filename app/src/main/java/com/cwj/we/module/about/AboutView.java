package com.cwj.we.module.about;

import com.cwj.we.base.BaseView;
import com.cwj.we.bean.LatestBean;

public interface AboutView extends BaseView {

    void latestData(LatestBean bean);

    void onError(String msg);
}
