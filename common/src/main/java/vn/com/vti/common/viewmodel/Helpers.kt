@file:Suppress("unused")

package vn.com.vti.common.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import vn.com.vti.common.appInstance
import vn.com.vti.common.scene.Stage
import vn.com.vti.common.util.livedata.BlockingTaskCountObserver

/**
 * Default implementation for handle direction event from [Fragment] && [Stage]
 */
fun <T> T.defaultHandleDirection(direction: Direction) where T : Stage, T : Fragment {
    if (onDispatchDirectionEvent(direction)) {
        return
    }
    when (direction) {
        is Backward -> {
            if (this is DialogFragment) this.dismissAllowingStateLoss()
            else requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        is Finish -> {
        }
        is SetResult -> activity?.run {
            setResult(direction.resultCode, direction.data)
        }
        is IntentDirection -> startActivity(direction.intent)
        is NavGraphDirection -> findNavController().navigate(
            direction.actionId, direction.args, direction.options
        )
        is NavActionDirection -> findNavController().navigate(
            direction.direction, direction.options
        )
        is NavUriDirection -> findNavController().navigate(
            direction.uri
        )
        else -> {
        }
    }
    if (direction.finish) {
        if (this is DialogFragment) {
            this.dismissAllowingStateLoss()
        } else {
            activity?.finish()
        }
    }
}

/**
 * Default implementation for handle direction event from [Activity] && [Stage]
 */
fun <T> T.defaultHandleDirection(
    direction: Direction, @IdRes navHostId: Int? = null
) where T : Stage, T : AppCompatActivity {
    if (onDispatchDirectionEvent(direction)) {
        return
    }
    when (direction) {
        is Backward -> onBackPressedDispatcher.onBackPressed()
        is Finish -> {
        }
        is SetResult -> {
            setResult(direction.resultCode, direction.data)
        }
        is IntentDirection -> startActivity(direction.intent)
        is NavGraphDirection -> navHostId?.let {
            findNavController(it).navigate(
                direction.actionId, direction.args, direction.options
            )
        }
        is NavActionDirection -> navHostId?.let {
            findNavController(navHostId).navigate(
                direction.direction, direction.options
            )
        }
        is NavUriDirection -> navHostId?.let {
            findNavController(navHostId).navigate(
                direction.uri
            )
        }
        else -> {
        }
    }
    if (direction.finish) finish()
}

/**
 * Utility method to observe viewModel direction
 */
fun <T> T.observeViewModelDirection(viewModel: AbsViewModel) where T : Stage, T : Fragment {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.getDirections().collect {
                defaultHandleDirection(it)
            }
        }
    }
}

/**
 * Utility method to observe viewModel direction
 */
fun <T> T.observeViewModelDirection(
    viewModel: AbsViewModel, @IdRes navHostId: Int? = null
) where T : Stage, T : AppCompatActivity {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.getDirections().collect {
                defaultHandleDirection(it, navHostId)
            }
        }
    }
}

/**
 * Utility method to observe viewModel event
 */
fun Stage.observeViewModelEvent(viewModel: AbsViewModel, lifecycleOwner: LifecycleOwner) {
    viewModel.getRunningTaskCount().collectOnLifecycle(
        lifecycleOwner, repeatOnState = Lifecycle.State.CREATED, BlockingTaskCountObserver(
            name = this::class.java.simpleName,
            onBlocking = this@observeViewModelEvent::notifyBlockingTaskStart,
            onUnblocking = this@observeViewModelEvent::notifyBlockingTaskFinish
        )
    )
    viewModel.getConfirmEvent().collectOnLifecycle(lifecycleOwner) {
        showMessage(it)
    }
    viewModel.getToastEvent().collectOnLifecycle(lifecycleOwner) {
        toast(it)
    }
}

fun <T> Class<T>.toIntentDirection(
    args: Bundle? = null,
    finish: Boolean = false,
): IntentDirection where T : AppCompatActivity {
    return Intent(appInstance(), this@toIntentDirection).apply {
        args?.let { this.putExtras(it) }
    }.let {
        IntentDirection(it, finish)
    }
}

fun <T> Class<T>.toIntentDirection(
    uri: Uri,
    finish: Boolean = false,
): IntentDirection where T : AppCompatActivity {
    return Intent(appInstance(), this@toIntentDirection).apply {
        data = uri
    }.let {
        IntentDirection(it, finish)
    }
}