package com.xue.qin.buildsrc.transform.sist;

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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class QinUtils {
    private static final String TAG = "QinUtils";
    private static final String SUFFIX_CLASS = ".class";
    private static final String SUFFIX_JAR = ".jar";

    private static class SingleHolder {
        private static final QinUtils instance = new QinUtils();
    }

    public static QinUtils getInstance() {
        return SingleHolder.instance;
    }

    public void convert(TransformInvocation transformInvocation, ClassPool classPool) throws NotFoundException, IOException, CannotCompileException, ClassNotFoundException {
        LogUtil.i(TAG, "convert()");
        LogUtil.i(TAG, "destDir class ----------------------------------------------------------------------------------------");
        for (TransformInput input : transformInvocation.getInputs()) {
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dir = directoryInput.getFile();
                classPool.insertClassPath(dir.getAbsolutePath());
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
                        CtClass mCtClass = addHook(classPool.get(className));
                        mCtClass.writeFile(destDir.getAbsolutePath());
                    }
                }
            }
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
        }
    }

    private CtClass addHook(CtClass ctClass) throws CannotCompileException, IOException, ClassNotFoundException {
        String className = ctClass.getName();
        if (!(className.contains("JavaSsistUtils") || className.endsWith(".R") || className.contains(".R$") || className.endsWith(".BuildConfig")) && !ctClass.isInterface()) {
            LogUtil.i(TAG, "addHook() ctClass: " + ctClass.getName());
            CtMethod[] ctMethods = ctClass.getDeclaredMethods();
            for (CtMethod ctMethod : ctMethods) {
                LogUtil.i(TAG, "addHook() ctMethod: " + ctMethod.getName());
                ctMethod.insertAfter("android.util.Log.i(\"[" + ctClass.getSimpleName() + "]\",\"" + ctMethod.getName() + "()\"\" time: \" + com.xue.qin.testjavasist.utils.JavaSsistUtils.getInstance().getCurTime());");
            }
        }
        return ctClass;
    }


    private CtClass createNewClass(ClassPool classPool) throws NotFoundException, CannotCompileException {
        CtClass ctClass = classPool.makeClass("com.xue.qin.testjavasist.Person");
        CtField ctField = new CtField(classPool.get("java.lang.String"), "name", ctClass);
        ctField.setModifiers(Modifier.PRIVATE);
        ctClass.addField(ctField);
        ctClass.addMethod(CtNewMethod.setter("setName", ctField));
        ctClass.addMethod(CtNewMethod.getter("getName", ctField));

        CtConstructor ctConstructor = new CtConstructor(new CtClass[]{classPool.get("java.lang.String")}, ctClass);
        ctConstructor.setBody("{$0.name = $1;}");
        ctClass.addConstructor(ctConstructor);

        CtConstructor cons = new CtConstructor(new CtClass[]{}, ctClass);
        cons.setBody("{name = \"qinxue\";}");
        ctClass.addConstructor(cons);

        CtMethod ctMethod = new CtMethod(CtClass.voidType, "printName", new CtClass[]{}, ctClass);
        ctMethod.setModifiers(Modifier.PUBLIC);
        ctMethod.setBody("{System.out.println(name);}");
        ctClass.addMethod(ctMethod);
        return ctClass;
    }

}
