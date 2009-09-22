/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.testFramework.fixtures.impl;

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.ModuleFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mike
 */
public abstract class ModuleFixtureBuilderImpl<T extends ModuleFixture> implements ModuleFixtureBuilder<T> {
  private static int ourIndex;

  private final ModuleType myModuleType;
  protected final List<String> myContentRoots = new ArrayList<String>();
  protected final List<String> mySourceRoots = new ArrayList<String>();
  protected final TestFixtureBuilder<? extends IdeaProjectTestFixture> myFixtureBuilder;
  private T myModuleFixture;
  protected String myOutputPath;
  protected String myTestOutputPath;

  public ModuleFixtureBuilderImpl(@NotNull final ModuleType moduleType, TestFixtureBuilder<? extends IdeaProjectTestFixture> fixtureBuilder) {
    myModuleType = moduleType;
    myFixtureBuilder = fixtureBuilder;
  }

  public ModuleFixtureBuilder<T> addContentRoot(final String contentRootPath) {
    myContentRoots.add(contentRootPath);
    return this;
  }

  public ModuleFixtureBuilder<T> addSourceRoot(final String sourceRootPath) {
    assert myContentRoots.size() > 0 : "content root should be added first";
    mySourceRoots.add(sourceRootPath);
    return this;
  }

  public void setOutputPath(final String outputPath) {
    myOutputPath = outputPath;
  }

  public void setTestOutputPath(String outputPath) {
    myTestOutputPath = outputPath;
  }

  protected Module createModule() {
    final Project project = myFixtureBuilder.getFixture().getProject();
    assert project != null;
    return ModuleManager.getInstance(project).newModule(getNextIndex() + ModuleFileType.DOT_DEFAULT_EXTENSION, myModuleType);
  }

  private static int getNextIndex() {
    return ourIndex++;
  }


  public synchronized T getFixture() {
    if (myModuleFixture == null) {
      myModuleFixture = instantiateFixture();
    }
    return myModuleFixture;
  }

  public void addSourceContentRoot(final String path) {
    addContentRoot(path);
    addSourceRoot(path);
  }

  protected abstract T instantiateFixture();

  Module buildModule() {
    final Module[] module = new Module[]{null};

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        module[0] = createModule();
        initModule(module[0]);
      }
    });

    return module[0];
  }

  protected void initModule(Module module) {
    final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    final ModifiableRootModel rootModel = rootManager.getModifiableModel();

    for (String contentRoot : myContentRoots) {
      final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(contentRoot);
      assert virtualFile != null : "cannot find content root: " + contentRoot;
      final ContentEntry contentEntry = rootModel.addContentEntry(virtualFile);

      for (String sourceRoot: mySourceRoots) {
        String s = contentRoot + "/" + sourceRoot;
        VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(s);
        if (vf == null) {
          final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(sourceRoot);
          if (file != null && VfsUtil.isAncestor(virtualFile, file, false)) vf = file;
        }
        //assert vf != null : "cannot find source root: " + sourceRoot;
        if (vf != null) {
          contentEntry.addSourceFolder(vf, false);
        }
      }
    }
    setupRootModel(rootModel);
    rootModel.commit();
  }

  protected void setupRootModel(ModifiableRootModel rootModel) {
  }

}
