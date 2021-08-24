package org.kpu.retrofit_example.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

// 문자열이 제이슨 형태인지, 제이슨 배열 형태인지
fun String?.isJsonObject() : Boolean = this?.startsWith("{") == true && this.endsWith("}")

fun String?.isJsonArray() : Boolean = this?.startsWith("[") == true && this.endsWith("]")

fun EditText.onMyTextChanged(completion: (Editable?) -> Unit) {
    this.addTextChangedListener(object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(editable: Editable?) {
            completion(editable)
        }

    })
}