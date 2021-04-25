package com.xue.qin.buildsrc.transform;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.xue.qin.buildsrc.log.LogUtil;
import com.xue.qin.buildsrc.transform.sist.QinUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class QinTransform extends Transform {
    private static final String TAG = "QinTransform";
    private AppExtension android;

    public QinTransform(AppExtension android) {
        this.android = android;
    }

    @Override
    public String getName() {
        return "QinTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) {
        LogUtil.i(TAG, "transform() start");
        try {


            ClassPool classPool = new ClassPool();
            for (File file : android.getBootClasspath()) {
                LogUtil.i(TAG, "file: " + file.getAbsolutePath());
                classPool.appendClassPath(file.getAbsolutePath());
            }
            List<CtClass> ctClassList = QinUtils.getInstance().convert(transformInvocation.getInputs(), classPool);
            QinUtils.getInstance().addHook(ctClassList, initOutPutTargetFile(transformInvocation));
        } catch (NotFoundException | CannotCompileException | IOException e) {
            e.printStackTrace();
        }
    }

    private File initOutPutTargetFile(TransformInvocation transformInvocation) throws IOException {
        transformInvocation.getOutputProvider().deleteAll();
        File targetFile = transformInvocation.getOutputProvider().getContentLocation("main", getOutputTypes(), getScopes(), Format.JAR);
        File parentFile = targetFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (targetFile.exists()) {
            targetFile.delete();
        }
        return targetFile;
    }
}
