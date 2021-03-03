package com.zj.play.project

import android.app.Application
import com.zj.core.util.DataStoreUtils
import com.zj.model.pojo.QueryArticle
import com.zj.model.room.PlayDatabase
import com.zj.model.room.entity.PROJECT
import com.zj.network.base.PlayAndroidNetwork
import com.zj.play.home.DOWN_PROJECT_ARTICLE_TIME
import com.zj.play.home.FOUR_HOUR
import com.zj.play.main.login.composeFire
import com.zj.play.main.login.fire
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 版权：Zhujiang 个人版权
 *
 * @author zhujiang
 * 创建日期：2020/9/10
 * 描述：PlayAndroid
 *
 */

class ProjectRepository constructor(val application: Application) {

    private val projectClassifyDao = PlayDatabase.getDatabase(application).projectClassifyDao()
    private val articleListDao = PlayDatabase.getDatabase(application).browseHistoryDao()

    /**
     * 获取项目标题列表
     */
    fun getProjectTree(isRefresh: Boolean) = composeFire {
        val projectClassifyLists = projectClassifyDao.getAllProject()
        if (projectClassifyLists.isNotEmpty() && !isRefresh) {
            projectClassifyLists
        } else {
            val projectTree = PlayAndroidNetwork.getProjectTree()
            if (projectTree.errorCode == 0) {
                val projectList = projectTree.data
                projectClassifyDao.insertList(projectList)
                projectList
            } else {
                null
            }
        }
    }

    /**
     * 获取项目具体文章列表
     * @param query 查询类
     */
    fun getProject(query: QueryArticle) = composeFire {
        if (query.page == 1) {
            val dataStore = DataStoreUtils
            val articleListForChapterId =
                articleListDao.getArticleListForChapterId(PROJECT, query.cid)
            var downArticleTime = 0L
            dataStore.readLongFlow(DOWN_PROJECT_ARTICLE_TIME, System.currentTimeMillis()).first {
                downArticleTime = it
                true
            }
            if (articleListForChapterId.isNotEmpty() && downArticleTime > 0 && downArticleTime - System.currentTimeMillis() < FOUR_HOUR && !query.isRefresh) {
                articleListForChapterId
            } else {
                val projectTree = PlayAndroidNetwork.getProject(query.page, query.cid)
                if (projectTree.errorCode == 0) {
                    if (articleListForChapterId.isNotEmpty() && articleListForChapterId[0].link == projectTree.data.datas[0].link && !query.isRefresh) {
                        articleListForChapterId
                    } else {
                        projectTree.data.datas.forEach {
                            it.localType = PROJECT
                        }
                        dataStore.saveLongData(
                            DOWN_PROJECT_ARTICLE_TIME,
                            System.currentTimeMillis()
                        )
                        if (query.isRefresh) {
                            articleListDao.deleteAll(PROJECT, query.cid)
                        }
                        articleListDao.insertList(projectTree.data.datas)
                        projectTree.data.datas
                    }
                } else {
                    null
                }
            }
        } else {
            val projectTree = PlayAndroidNetwork.getProject(query.page, query.cid)
            if (projectTree.errorCode == 0) {
                projectTree.data.datas
            } else {
                null
            }
        }
    }

}