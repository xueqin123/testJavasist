package com.xue.qin.buildsrc.transform.sist;

import com.android.SdkConstants;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.xue.qin.buildsrc.log.LogUtil;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class QinUtils {
    private static final String TAG = "QinUtils";
    private static final String SUFFIX_CLASS = ".class";
    private static final String SUFFIX_JAR = ".jar";
    private static String timeStampClassName = "com.xue.qin.common.TimeStamp";
    private static Class stampClass = null;

    private static class SingleHolder {
        private static final QinUtils instance = new QinUtils();
    }

    public static QinUtils getInstance() {
        return SingleHolder.instance;
    }

    public void convert(TransformInvocation transformInvocation, ClassPool classPool) throws NotFoundException, IOException, CannotCompileException, ClassNotFoundException {
        LogUtil.i(TAG, "convert()");
        for (TransformInput input : transformInvocation.getInputs()) {
            for (JarInput jarInput : input.getJarInputs()) {
                classPool.insertClassPath(jarInput.getFile().getAbsolutePath());
            }
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                classPool.insertClassPath(directoryInput.getFile().getAbsolutePath());
            }
        }
        if (stampClass == null) {
            stampClass = classPool.get(timeStampClassName).toClass();
        }
        for (TransformInput input : transformInvocation.getInputs()) {
            LogUtil.i(TAG, "jar class ----------------------------------------------------------------------------------------");
            for (JarInput jarInput : input.getJarInputs()) {
                String destName = jarInput.getFile().getName();
                if (destName.endsWith(SUFFIX_JAR)) {
                    destName = destName.substring(0, destName.length() - 4);
                }
                String path = jarInput.getFile().getAbsolutePath();
                File destDir = transformInvocation.getOutputProvider().getContentLocation(destName + "_" + DigestUtils.md5(path), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                LogUtil.i(TAG, "jar destDir: " + destDir);
                FileUtils.copyFile(jarInput.getFile(), destDir);
            }
            LogUtil.i(TAG, "destDir class ----------------------------------------------------------------------------------------");
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dir = directoryInput.getFile();
                String dirPath = dir.getAbsolutePath() + File.separator;
                LogUtil.i(TAG, "dirPath: " + dirPath);
                Collection<File> files = FileUtils.listFiles(dir, null, true);
                File destDir = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                FileUtils.copyDirectory(dir, destDir);
                createNewClass(classPool).writeFile(destDir.getAbsolutePath());
                for (File f : files) {
                    String path = f.getAbsolutePath();
                    if (path.endsWith(SUFFIX_CLASS)) {
                        String className = path.substring(0, path.length() - SUFFIX_CLASS.length()).replace(dirPath, "").replace(File.separator, ".");
                        CtClass mCtClass = addHook(classPool.get(className), stampClass);
                        mCtClass.writeFile(destDir.getAbsolutePath());
                    }
                }
            }
        }
    }

    private CtClass addHook(CtClass ctClass, Class timeStampClz) throws CannotCompileException, IOException, ClassNotFoundException {
        String className = ctClass.getName();
        if (!(className.contains("JavaSsistUtils") || className.endsWith(".R") || className.contains(".R$") || className.endsWith(".BuildConfig")) && !ctClass.isInterface()) {
            LogUtil.i(TAG, "addHook() ctClass: " + ctClass.getName());
            CtMethod[] ctMethods = ctClass.getDeclaredMethods();
            for (CtMethod ctMethod : ctMethods) {
                LogUtil.i(TAG, "addHook() ctMethod: " + ctMethod.getName());
                Object an = ctMethod.getAnnotation(timeStampClz);
                if (an != null) {
                    ctMethod.insertBefore("android.util.Log.i(\"[" + ctClass.getSimpleName() + "]\",\"" + ctMethod.getName() + "() start \"\" time: \" + com.xue.qin.testjavasist.TimeUtil.getCurTime());");
                    ctMethod.insertAfter("android.util.Log.i(\"[" + ctClass.getSimpleName() + "]\",\"" + ctMethod.getName() + "() end \"\" time: \" + com.xue.qin.testjavasist.TimeUtil.getCurTime());");
                }
            }
        }
        return ctClass;
    }


    private CtClass createNewClass(ClassPool classPool) throws NotFoundException, CannotCompileException {
        CtClass ctClass = classPool.makeClass("com.xue.qin.testjavasist.TimeUtil");
        CtMethod ctMethod = new CtMethod(classPool.get("java.lang.String"), "getCurTime", new CtClass[]{}, ctClass);
        ctMethod.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
        ctMethod.setBody(
                "{\n" +
                        "        long cur = System.currentTimeMillis();\n" +
                        "        java.util.Date nowTime = new java.util.Date(cur);\n" +
                        "        java.text.SimpleDateFormat time = new java.text.SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n" +
                        "        return time.format(nowTime) + \" stamp: \" + cur;\n" +
                        "    }");
        ctClass.addMethod(ctMethod);
        return ctClass;
    }

}
