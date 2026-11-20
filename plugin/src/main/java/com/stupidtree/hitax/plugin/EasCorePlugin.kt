package com.stupidtree.hitax.plugin

import android.app.Application
import androidx.lifecycle.LiveData
import com.stupidtree.component.data.DataState
import com.stupidtree.hita.api.EasAuthService
import com.stupidtree.hita.api.EmptyClassroomService
import com.stupidtree.hita.api.ExamService
import com.stupidtree.hita.api.HostEnvironmentService
import com.stupidtree.hita.api.Plugin
import com.stupidtree.hita.api.PluginContext
import com.stupidtree.hita.api.ScoreService
import com.stupidtree.hita.api.TrainingPlanService
import com.stupidtree.hitax.data.repository.EASRepository

/**
 * T1 EAS core. Uses the headless EAS AAR (no UI classes) and exposes auth,
 * scores, exams, classrooms and training-plan services.
 */
class EasCorePlugin : Plugin {
    override val id = "com.stupidtree.hitax.eas"
    override val displayName = "教务核心"
    override val requiresApi = 11

    override fun install(ctx: PluginContext) {
        val app = (ctx.service(HostEnvironmentService.KEY)?.applicationContext as? Application)
            ?: error("缺少宿主 applicationContext")
        ctx.provide(EasAuthService.KEY, EasAuthImpl(app))
        ctx.provide(ScoreService.KEY, impl<ScoreService>("com.stupidtree.hitax.eas.ScoreServiceImpl", app))
        ctx.provide(EmptyClassroomService.KEY, impl<EmptyClassroomService>("com.stupidtree.hitax.eas.EmptyClassroomServiceImpl", app))
        ctx.provide(TrainingPlanService.KEY, impl<TrainingPlanService>("com.stupidtree.hitax.eas.TrainingPlanServiceImpl", app))
        ctx.provide(ExamService.KEY, object : ExamService {
            override fun exams(): LiveData<DataState<List<com.stupidtree.hitax.data.model.eas.ExamItem>>> =
                EASRepository.getInstance(app).getExamInfo()
        })
    }

    override fun purgeData() = Unit

    private fun <T> impl(name: String, app: Application): T =
        Class.forName(name).getConstructor(Application::class.java).newInstance(app) as T
}

private class EasAuthImpl(private val application: Application) : EasAuthService {
    private val repo get() = EASRepository.getInstance(application)
    override fun isLogin(): Boolean = repo.getEasToken().isLogin()
    override fun accountName(): String? = repo.getEasToken().name?.ifBlank { repo.getEasToken().username }
    override fun accountSubtitle(): String? = repo.getEasToken().stuId?.ifBlank { repo.getEasToken().username }
    override fun login(username: String, password: String): LiveData<DataState<Boolean>> = repo.login(username, password)
    override fun logout() = repo.logout()
}
