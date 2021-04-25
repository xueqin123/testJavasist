package com.xue.qin.buildsrc.transform.sist;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;
import com.xue.qin.buildsrc.log.LogUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class QinUtils {
    private static final String TAG = "QinUtils";

    private static class SingleHolder {
        private static final QinUtils instance = new QinUtils();
    }

    public static QinUtils getInstance() {
        return SingleHolder.instance;
    }

    public List<CtClass> convert(Collection<TransformInput> inputs, ClassPool classPool) throws NotFoundException, IOException {
        List<String> classNameList = new ArrayList<>();
        List<CtClass> ctClassList = new ArrayList<>();
        LogUtil.i(TAG, "dir class ----------------------------------------------------------------------------------------");
        for (TransformInput input : inputs) {
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dir = directoryInput.getFile();
                classPool.insertClassPath(dir.getAbsolutePath());
                String dirPath = dir.getAbsolutePath() + File.separator;
                LogUtil.i(TAG, "dirPath: " + dirPath);
                Collection<File> files = FileUtils.listFiles(dir, null, true);
                for (File f : files) {
                    String path = f.getAbsolutePath();
                    if (path.endsWith(".class")) {
                        String className = path.replace(dirPath, "").replace(File.separator, ".");
                        LogUtil.i(TAG, "className: " + className);
                        classNameList.add(className);
                    }
                }
            }
            LogUtil.i(TAG, "jar class ----------------------------------------------------------------------------------------");
            for (JarInput jarInput : input.getJarInputs()) {
                classPool.insertClassPath(jarInput.getFile().getAbsolutePath());
                JarFile jarFile = new JarFile(jarInput.getFile());
                Enumeration<JarEntry> classes = jarFile.entries();
                LogUtil.i(TAG, "jarFile name: " + jarFile.getName());
                while (classes.hasMoreElements()) {
                    JarEntry libClass = classes.nextElement();
                    String className = libClass.getName();
                    if (className.endsWith(".class")) {
                        className = className.substring(0, className.length() - 6).replaceAll(File.separator, ".");
                        LogUtil.i(TAG, "className: " + className);
                    }
                    classNameList.add(className);
                }
            }
        }
        return ctClassList;
    }
}
