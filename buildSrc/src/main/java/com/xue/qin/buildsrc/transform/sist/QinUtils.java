package com.xue.qin.buildsrc.transform.sist;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;
import com.google.common.io.FileBackedOutputStream;
import com.xue.qin.buildsrc.log.LogUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class QinUtils {
    private static final String TAG = "QinUtils";
    private static final String SUFFIX = ".class";

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
                    if (path.endsWith(SUFFIX)) {
                        String className = path.substring(0, path.length() - SUFFIX.length()).replace(dirPath, "").replace(File.separator, ".");
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
                    if (className.endsWith(SUFFIX)) {
                        className = className.substring(0, className.length() - SUFFIX.length()).replaceAll(File.separator, ".");
                        LogUtil.i(TAG, "className: " + className);
                        classNameList.add(className);
                    }
                }
            }
        }

        for (String className : classNameList) {
            CtClass ctClass = classPool.get(className);
            ctClassList.add(ctClass);
        }

        return ctClassList;
    }

    public void addHook(List<CtClass> ctClassList, File target) throws CannotCompileException, IOException {
        ZipOutputStream outputStream = new JarOutputStream(new FileOutputStream(target));
        for (CtClass ctClass : ctClassList) {
            if (isValid(ctClass)) {
                LogUtil.i(TAG, "addHook() ctClass: " + ctClass.getName());
                CtMethod[] ctMethods = ctClass.getDeclaredMethods();
                for (CtMethod ctMethod : ctMethods) {
                    LogUtil.i(TAG, "addHook() ctMethod: " + ctMethod.getName());
                    ctMethod.insertAfter("System.out.println(\"hahahahha\");");
                }
            }
            zipFile(ctClass.toBytecode(), outputStream, ctClass.getName().replace(".", File.separator) + ".class");
        }
        outputStream.close();
    }

    private void zipFile(byte[] classBytesArray, ZipOutputStream zos, String entryName) {
        try {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(classBytesArray, 0, classBytesArray.length);
            zos.closeEntry();
            zos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValid(CtClass ctClass) {
        String className = ctClass.getName();
        return !(className.endsWith(".R") || className.contains(".R$") || className.endsWith(".BuildConfig")) && !ctClass.isInterface();

    }
}
