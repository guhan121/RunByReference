/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.nucleon.zdhcsu.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowser;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * dialog 示例
 */
public class ExampleDialog extends DialogWrapper {
    private ChangesBrowser changesBrowser;

    public ExampleDialog(Project project, List<? extends ChangeList> changeLists, List<Change> changes) {
        super(project);
        // 未 commit 的 changes
//        this.changesBrowser = new MultipleChangeListBrowser(project, changeLists, changes,
//                Disposer.newDisposable(), null, true, true, null, null);

        // 已经 commit 的 changes
        //        this.changesBrowser = new RepositoryChangesBrowser(project, changeLists, changes, null);
        //        changesBrowser.setChangesToDisplay(changes);

        // 要有这行才能显示
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(changesBrowser);
        return panel;
    }
}
