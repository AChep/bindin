package com.artemchep.bindin.common

import android.widget.EditText
import androidx.annotation.UiThread
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import com.artemchep.bindin.InBinding
import com.artemchep.bindin.bindIn
import com.artemchep.bindin.bindOut
import kotlinx.coroutines.flow.Flow

@UiThread
fun LifecycleOwner.bind(
    flow: Flow<String>,
    view: EditText,
    pipe: (String) -> Unit,
) = bindIn(flow, pipe = view::setText).bindOut(view, pipe)

@UiThread
fun InBinding<String>.bindOut(
    editText: EditText,
    pipe: (String) -> Unit,
) = bindOut(
    observe = { observer ->
        val listener = editText.addTextChangedListener {
            observer(it?.toString().orEmpty())
        }

        fun() {
            editText.removeTextChangedListener(listener)
        }
    },
    pipe = pipe
)
