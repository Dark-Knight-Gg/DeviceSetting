package vn.com.vti.common.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vn.com.vti.common.model.UiText


interface IAutoCompleteFlow : IValidationFlow {

    fun getOriginalContent(): MutableStateFlow<String>

}

/**
 * Call it on [vn.com.vti.common.viewmodel.AbsViewModel.onCreate]
 * to listen a signal when user typed into your auto-complete text
 * and you need to fill some suggestions for them
 */
@Suppress("OPT_IN_USAGE")
fun <T> AutoCompleteTextFlow<T>.collectOnRequestAutoCompleteText(
    scope: CoroutineScope, onUserRequestSuggestions: suspend (query: String) -> Unit
) {
    scope.launch {
        getContent().debounce(250L).filter { it != getOriginalContent().value }
            .shareIn(scope, SharingStarted.Eagerly).collectLatest {
                onUserRequestSuggestions(it)
            }
    }
}

class AutoCompleteTextFlow<T>(
    scope: CoroutineScope,
    initVal: T,
    private val validator: (content: String, originalContent: String) -> UiText? = { _, _ -> null },
) : IAutoCompleteFlow {

    val source = MutableStateFlow(initVal)
    private val stateOfContent: MutableStateFlow<String> = MutableStateFlow("")
    private val stateOfOriginalContent: MutableStateFlow<String> = MutableStateFlow("")
    private val stateOfError = MutableStateFlow<UiText?>(null)

    init {
        scope.launch {
            source.collectLatest {
                // only auto validate to remove error
                if (stateOfError.value != null) validate()
            }
        }
    }

    override fun getContent(): MutableStateFlow<String> = stateOfContent

    override fun getOriginalContent(): MutableStateFlow<String> = stateOfOriginalContent

    fun setValue(value: T) {
        source.value = value
    }

    override fun getError(): StateFlow<UiText?> = stateOfError

    override fun validate(): Boolean {
        return validator(stateOfContent.value, stateOfOriginalContent.value).let {
            stateOfError.value = it
            it == null
        }
    }

}