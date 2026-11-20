package com.stupidtree.hitax.plugin

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stupidtree.component.data.DataState
import com.stupidtree.hita.api.ExamService
import com.stupidtree.hita.api.HostEnvironmentService
import com.stupidtree.hita.api.Plugin
import com.stupidtree.hita.api.PluginContext
import com.stupidtree.hitax.data.source.local.ExamMemoStore

/** T1 EAS exam memo core; login/scores remain legacy. */
class EasCorePlugin : Plugin {
    override val id = "com.stupidtree.hitax.eas"
    override val displayName = "教务备忘录核心"
    override val requiresApi = 11

    override fun install(ctx: PluginContext) {
        val app = (ctx.service(HostEnvironmentService.KEY)?.applicationContext as? Application)
            ?: error("缺少宿主 applicationContext")
        ctx.provide(ExamService.KEY, object : ExamService {
            override fun exams(): LiveData<DataState<List<com.stupidtree.hitax.data.model.eas.ExamItem>>> =
                MutableLiveData(DataState(ExamMemoStore(app).load(), DataState.STATE.SUCCESS))
        })
    }

    override fun purgeData() = Unit
}
