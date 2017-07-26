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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.testFramework.TeamCityLogger.info;
import static org.apache.log4j.helpers.LogLog.error;

public class RunByReference extends AnAction implements ListSelectionListener {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("AdbIdea-%d").build());
    private Project project;
    private Object pathReference;
    private String packagebase = "";
    private List<MethodPart> methodPartList = new ArrayList();
    private JDialog jFrame;
    JTextField desc;
    JList list;
    private DefaultListModel listModel = new DefaultListModel();
    private boolean choosedDevice = false;


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


        String retClazzOrCase = "";
        if (psiPackage != null && parentOfType != null) {
            retClazzOrCase = psiPackage.getQualifiedName() + "." + parentOfType.getName();
            if (!referenceAt.getText().equals(parentOfType.getName())) {
                retClazzOrCase += "#" + referenceAt.getText();
            }
        }
        Process pro = null;
        InputStream is = null;
        BufferedReader br = null;
        String allDevicesInfo = "";
        try {
            pro = Runtime.getRuntime().exec("cmd /c adb devices -l");
            is = pro.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                info(line);
                allDevicesInfo += (line + System.getProperty("line.separator"));
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
        Pattern pattern = Pattern.compile("(.*product:.*model:.*device:.*)");// 匹配的模式
        // DataKeys 事件触发是的上下文变量
//        Project project = e.getData(DataKeys.PROJECT);

        Matcher matcher = pattern.matcher(allDevicesInfo);
        listModel.clear();
        while (matcher.find()) {
            System.out.println(matcher.group(1)); // 每次返回第一个即可
            // System.out.println(matcher.groupCount()); //
            ADBDeviceInfo d = new ADBDeviceInfo(matcher.group(1));
            listModel.addElement(d);
            // 可用groupcount()方法来查看捕获的组数个数
            // brand = matcher.group(1);
//            System.out.println(d.getBrand());
        }

        String target = "";
        if (listModel.size() == 0) {
            System.out.println("No connected devices!");
            setStatusBarText(project, "No connected devices!");
            return;
        } else if (listModel.size() == 1) {
            choosedDevice = true;
        } else {
            initSelectView();
            target = "-s " + ((ADBDeviceInfo) listModel.get(list.getSelectedIndex())).id;
        }

        if (!choosedDevice) {
            setStatusBarText(project, "No choose devices!");
            return;
        }
        // VCS 相关上下文
//        VcsContext vcsContext = VcsContextFactory.SERVICE.getInstance().createContextOn(e);
//        ChangeListManager changeListManager = ChangeListManager.getInstance(e.getProject());
//        // 显示当前选中的 changes
//        ExampleDialog dialog = new ExampleDialog(project,  changeListManager.getChangeListsCopy(),
//                Lists.newArrayList(vcsContext.getSelectedChanges()));
//        dialog.show();

        String cmd = "adb " + target + " shell am instrument -w -r -e debug false -e class " + retClazzOrCase + " com.duowan.mobile.test/android.support.test.runner.AndroidJUnitRunner";
//        Messages.showDialog(cmd,"当前editor中的内容",new String[]{"OK"}, -1, null);
        System.out.println(cmd);
        try {
            setStatusBarText(project, "adb run...");
            Runtime.getRuntime().exec(cmd);
            info("run successfully");
        } catch (IOException e1) {
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

    private void initSelectView() {
        jFrame = new JDialog();// 定义一个窗体Container container = getContentPane();
        jFrame.setModal(true);
        Container container = jFrame.getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel panelname = new JPanel();// /定义一个面板
        panelname.setLayout(new GridLayout(1, 2));
        panelname.setBorder(BorderFactory.createTitledBorder("命名"));

        desc = new JTextField();
        desc.setText("choose connected device");
        panelname.add(desc);

        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(8);
        JScrollPane listScrollPane = new JScrollPane(list);
        container.add(listScrollPane, BorderLayout.CENTER);
        JPanel menu = new JPanel();
        menu.setLayout(new FlowLayout());

        Button cancle = new Button();
        cancle.setLabel("取消");
        cancle.addActionListener(actionListener);

        Button ok = new Button();
        ok.setLabel("确定");
        ok.addActionListener(actionListener);
        menu.add(cancle);
        menu.add(ok);
        container.add(menu);


        jFrame.setSize(400, 200);
        jFrame.setLocationRelativeTo(null);

        jFrame.setVisible(true);
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("取消")) {
                jFrame.dispose();
                choosedDevice = false;
            } else {
                jFrame.dispose();
                choosedDevice = true;
//                Messages.showInfoMessage(project, list.getSelectedValue().toString(), "提示");
            }
        }
    };


    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            if (list.getSelectedIndex() == -1) {
                //No selection, disable fire button.
                setStatusBarText(project, "No active file");

            } else {
                //Selection, enable the fire button.
                setStatusBarText(project, list.getSelectedValue().toString());
            }
        }
    }
}