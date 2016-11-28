package com.pichler.gitroochangelistplugin

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.{ChangeListManager, LocalChangeList}

import scala.collection.JavaConverters._

/**
  * Created by Patrick on 28.11.2016.
  */
class DoChangeListMagicAction extends AnAction("ChangeList Magic") {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val project = event.getProject

    val changeListManager = ChangeListManager.getInstance(project)
    val projectLevelVcsManager = ProjectLevelVcsManager.getInstance(project)

    val changeLists = getChangeLists(changeListManager, projectLevelVcsManager)

    changeListManager.getAllChanges.asScala
      .map({ change => (change, projectLevelVcsManager.getVcsRootFor(change.getVirtualFile)) })
      .map({ pair => (pair._1, changeLists(pair._2.getName)) })
      .foreach({ pair => changeListManager.moveChangesTo(pair._2, pair._1) })
  }

  def getChangeLists(changeListManager: ChangeListManager, projectLevelVcsManager: ProjectLevelVcsManager): Map[String, LocalChangeList] = {
    val changeListMap = changeListManager.getChangeLists.asScala map { changeList => changeList.getName -> changeList } toMap

    val map = changeListManager.getAllChanges.asScala.toStream
      .map { change => projectLevelVcsManager.getVcsRootFor(change.getVirtualFile) }
      .map { vcsRoot => vcsRoot.getName }
      .distinct
      .filter(!changeListMap.contains(_))
      .map({ name => name -> changeListManager.addChangeList(name, "") })
      .toMap

    changeListMap ++ map
  }
}
