package com.xue.qin.testjavasist.t;


import com.xue.qin.common.TimeStamp;

public class TestBean {
    private String title = "oldTitle";

    @TimeStamp
    public String getTitle() {
        return title;
    }
}
