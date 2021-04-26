package com.xue.qin.testjavasist.t;

import com.xue.qin.testjavasist.ann.MyAnnotation;


public class TestBean {
    private String title = "oldTitle";

    @MyAnnotation
    public String getTitle() {
        return title;
    }
}
