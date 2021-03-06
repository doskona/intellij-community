// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ide.actions.runAnything;

import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

class RunAnythingMore extends JPanel {
  static final RunAnythingMore instance = new RunAnythingMore();
  final JLabel label = new JLabel(" load more ...");

  private RunAnythingMore() {
    super(new BorderLayout());
    add(label, BorderLayout.CENTER);
  }

  static RunAnythingMore get(boolean isSelected, boolean hasFocus) {
    instance.setBackground(UIUtil.getListBackground(isSelected, hasFocus));
    instance.label.setForeground(UIUtil.getLabelDisabledForeground());
    instance.label.setFont(RunAnythingUtil.getTitleFont());
    instance.label.setBackground(UIUtil.getListBackground(isSelected, hasFocus));
    return instance;
  }
}