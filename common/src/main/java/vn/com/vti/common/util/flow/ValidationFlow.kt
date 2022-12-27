package vn.com.vti.common.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import vn.com.vti.common.R
import vn.com.vti.common.model.UiText

interface IValidationFlow {

    fun validate(): Boolean

    fun getContent(): MutableStateFlow<String>

    fun getError(): StateFlow<UiText?>

}

fun validate(vararg items: IValidationFlow): Boolean = items.map { it.validate() }.all { it }

class ValidationTextFlow(
    scope: CoroutineScope, private val validator: (content: String) -> UiText? = { null }
) : IValidationFlow {

    private var initValue: String = ""
    private val stateOfContent: MutableStateFlow<String> = MutableStateFlow("")
    private val stateOfError: MutableStateFlow<UiText?> = MutableStateFlow(null)
    private var flagValidationEnabled = false
    private val stateOfChange: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        scope.launch {
            stateOfContent.collectLatest {
                stateOfChange.value = (it != initValue)
                if (flagValidationEnabled) validate()
            }
        }
    }

    override fun getContent(): MutableStateFlow<String> = stateOfContent

    fun setValue(value: String) {
        initValue = value
        stateOfContent.value = value
    }

    fun getChangeState(): StateFlow<Boolean> = stateOfChange

    override fun getError(): StateFlow<UiText?> = stateOfError

    fun setError(errorValue: UiText?) {
        stateOfError.value = errorValue
    }

    fun markValidationEnabled() {
        flagValidationEnabled = true
    }

    override fun validate(): Boolean {
        return validator(stateOfContent.value).let {
            stateOfError.value = it
            it == null
        }
    }
}

fun nonNullOrEmptyValidationTextFlow(
    scope: CoroutineScope, errorMessage: UiText = UiText.of(R.string.msg_nonnull_or_empty_field)
) = ValidationTextFlow(scope, validator = {
    if (it.isEmpty()) errorMessage
    else null
})

class ValidationFlow<T>(
    scope: CoroutineScope,
    initVal: T,
    private val validator: (value: T) -> UiText? = { _ -> null },
) : IValidationFlow {

    val source = MutableStateFlow(initVal)
    private var initValue: T? = null
    private val stateOfContent: MutableStateFlow<String> = MutableStateFlow("")
    private val stateOfError = MutableStateFlow<UiText?>(null)
    private val stateOfChange: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        scope.launch {
            source.collectLatest {
                stateOfChange.value = (it != initValue)
                // only auto validate to remove error
                if (stateOfError.value != null) validate()
            }
        }
    }

    override fun getContent(): MutableStateFlow<String> = stateOfContent

    fun setValue(value: T) {
        initValue = value
        source.value = value
    }

    fun getChangeState(): StateFlow<Boolean> = stateOfChange

    override fun getError(): StateFlow<UiText?> = stateOfError

    override fun validate(): Boolean {
        return validator(source.value).let {
            stateOfError.value = it
            it == null
        }
    }

}

inline fun <reified T> nonNullOrEmptyValidationFlow(
    scope: CoroutineScope,
    initVal: T,
    errorMessage: UiText = UiText.of(R.string.msg_nonnull_or_empty_field)
) = ValidationFlow(scope, initVal = initVal, validator = {
    if (it == null) errorMessage
    else null
})
