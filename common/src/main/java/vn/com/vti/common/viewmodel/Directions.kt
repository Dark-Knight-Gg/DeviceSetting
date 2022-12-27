package vn.com.vti.common.viewmodel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions

open class Direction(val finish: Boolean = false)

/**
 * Dedicate for a back navigate direction (simply like back pressed)
 */
object Backward : Direction()

/**
 * Dedicate for a finish (close activity, dismiss dialog)
 */
object Finish : Direction(true)

/**
 * Dedicate for a finish (close activity, dismiss dialog) with result
 */
class SetResult(val resultCode: Int, val data: Intent? = null, finish: Boolean = false) :
    Direction(finish)

/**
 * Dedicate for an intent navigation
 */
class IntentDirection(val intent: Intent, finish: Boolean = false) : Direction(finish)

/**
 * Dedicate for an customize navigation which cannot be created from inside of viewmodel (Google SignIn,...)
 */
@Suppress("unused")
class ActionDirection(val action: String, val args: Bundle? = null, finish: Boolean = false) :
    Direction(finish)

/**
 * Dedicate for navigation via nav-graph action
 */
class NavGraphDirection(
    @IdRes val actionId: Int,
    val args: Bundle? = null,
    val options: NavOptions? = null,
    finish: Boolean = false
) : Direction(finish)

/**
 * Dedicate for navigation via nav-graph [NavDirections]
 */
class NavActionDirection(
    val direction: NavDirections,
    val options: NavOptions? = null,
    finish: Boolean = false
) : Direction(finish)

/**
 * Dedicate for navigation via nav-graph [NavDirections]
 */
class NavUriDirection(
    val uri: Uri,
    finish: Boolean = false,
) : Direction(finish)