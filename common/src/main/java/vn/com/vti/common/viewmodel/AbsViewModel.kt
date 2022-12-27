package vn.com.vti.common.viewmodel

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import vn.com.vti.common.model.ConfirmRequest
import vn.com.vti.common.model.UiText

interface AbsViewModel {

    /**
     * Call only one time after viewmodel is created
     */
    fun onCreate()

    /**
     * Called when viewmodel binds to scene
     */
    fun onBind(args: Bundle?)

    /**
     * Called when viewmodel binds to scene
     */
    fun onNewArguments(action: String?, args: Bundle?): Boolean

    /**
     * Call when viewmodel attach to scene's lifecycle
     *
     * @param scene
     */
    fun onAttachScene(scene: Scene)

    /**
     * Call when viewmodel is completely created, This can be used for fetching initial-data, initial request from scene, etc
     */
    fun onReady()

    /**
     * Call when viewmodel detach to scene's lifecycle
     *
     * @param scene
     */
    fun onDetachScene(scene: Scene)

    /**
     * Call when the viewmodel is unbound from scene
     */
    fun onUnbind()

    fun getDirections(): Flow<Direction>

    fun getRunningTaskCount(): StateFlow<Int>

    fun getConfirmEvent(): Flow<ConfirmRequest>

    fun getToastEvent(): Flow<UiText>
}

interface Scene {

    fun getSceneResource(): Resources

    fun lifecycleOwner(): LifecycleOwner

    fun <I, O> registerForResultCallback(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I>
}