package com.xue.qin.buildsrc;

import com.android.build.gradle.AppExtension;
import com.xue.qin.buildsrc.transform.QinTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class QinPlugin implements Plugin<Project> {
    private static final String TAG = "QinPlugin";
    public static Logger logger;

    @Override
    public void apply(Project project) {
        logger = project.getLogger();
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        android.registerTransform(new QinTransform(android));
    }
}
