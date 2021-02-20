package com.artemchep.bindin.common

import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.artemchep.bindin.InBinding
import com.artemchep.bindin.bindIn
import com.artemchep.bindin.bindOut
import kotlinx.coroutines.flow.Flow

fun Fragment.bind(
    flow: Flow<Boolean>,
    view: CompoundButton,
    pipe: (Boolean) -> Unit,
) = bindIn(flow, pipe = view::setChecked).bindOut(view, pipe)

fun InBinding<Boolean>.bindOut(
    view: CompoundButton,
    pipe: (Boolean) -> Unit,
) = bindOut(
    observe = { observer ->
        view.setOnCheckedChangeListener { _, isChecked ->
            observer(isChecked)
        }
    },
    pipe = pipe
)
