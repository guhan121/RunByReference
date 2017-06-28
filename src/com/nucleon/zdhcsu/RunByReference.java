package com.nucleon.zdhcsu;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.intellij.testFramework.TeamCityLogger.info;
import static org.apache.log4j.helpers.LogLog.error;

public class RunByReference extends AnAction {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("AdbIdea-%d").build());
    private Project project;
    private Object pathReference;
    private String packagebase = "";
    private List<MethodPart> methodPartList = new ArrayList();

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Project project = (Project) e.getData(PlatformDataKeys.PROJECT);

//        initSelectView();
//        project.getProjectFilePath();

//        pathReference =e.getData(PlatformDataKeys.SELECTED_ITEM );
//        String txt= Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
//        String txt= "info";
//        String txt1 = pathReference!=null?pathReference.toString():"pathReference is null";
//        Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.",
//                "Information "+packagebase +"\r\n"+ txt1, Messages.getInformationIcon());

        final VirtualFile file1 = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file1 == null) {
            setStatusBarText(project, "No active file");
            return;
        }

        final String path = file1.getCanonicalPath();
        if (path == null) {
            setStatusBarText(project, "No path for the current file");
            return;
        }

        setStatusBarText(project, file1.getCanonicalPath());

        //获得editor -可以得到文档的路径
        final Editor editor = e.getRequiredData(PlatformDataKeys.EDITOR);
        //获得编辑器中的文档对象 --获取文档的所有内容
        final Document document = editor.getDocument();
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        //获得编辑器内容
        String mytext = document.getText();
        // 获取当前文件行数
        int len = editor.getDocument().getTextLength();
        PsiElement referenceAt = file.findElementAt(editor.getCaretModel().getOffset()); //当前的方法名
        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(file.getContainingDirectory()); //当前的包名
        PsiClass parentOfType = PsiTreeUtil.getParentOfType(referenceAt, PsiClass.class);//当前的类名
        PsiReference reference = referenceAt.getReference();

        String ret = "";
        if (psiPackage != null && parentOfType != null) {
            ret = psiPackage.getQualifiedName() + "." + parentOfType.getName();
            if (referenceAt != null) {
                ret += "#" + referenceAt.getText();
            }
        }
        Process pro = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            pro = Runtime.getRuntime().exec("cmd /c adb devices");
            is = pro.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String msg = null;
            String line;
            while ((line = br.readLine()) != null) {
                info(line);
            }
            pro.waitFor();
            is.close();
            br.close();
            pro.destroy();
        } catch (IOException exception) {
        } catch (InterruptedException exception2) {
        } finally {
            if (pro != null) {
                pro.destroy();
                pro = null;
            }
            try {
                if (is != null) is.close();
                if (br != null) br.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        String cmd = "adb shell am instrument -w -r   -e debug false -e class " + ret + " com.duowan.mobile.test/android.support.test.runner.AndroidJUnitRunner";
//        Messages.showDialog(cmd,"当前editor中的内容",new String[]{"OK"}, -1, null);
        try

        {
            setStatusBarText(project, "adb run...");
            Runtime.getRuntime().exec(cmd);
            info("run successfully");
        } catch (
                IOException e1)

        {
            e1.printStackTrace();
            error(e1.getMessage());
        }


        // 获取元素操作的工厂类
//        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

//        Messages.showDialog(mytext,"当前editor中的内容" + len,new String[]{"OK"}, -1, null);
    }

    private void setStatusBarText(Project project, String s) {
        WindowManager.getInstance().getStatusBar(project).setInfo(s);
    }
}
