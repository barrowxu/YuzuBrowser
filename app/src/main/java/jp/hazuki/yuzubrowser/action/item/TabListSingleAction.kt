/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.action.item

import android.app.AlertDialog
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.Spinner
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.SingleAction
import jp.hazuki.yuzubrowser.action.view.ActionActivity
import jp.hazuki.yuzubrowser.utils.Logger
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo
import java.io.IOException

class TabListSingleAction : SingleAction, Parcelable {
    var mode = MODE_HORIZONTAL
        private set
    var isLeftButton: Boolean = false
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (FIELD_NAME_REVERSE == parser.currentName) {
                    when (parser.nextToken()) {
                        JsonToken.VALUE_TRUE -> mode = MODE_REVERSE
                        JsonToken.VALUE_FALSE -> mode = MODE_NORMAL
                        else -> Logger.w(TAG, "current token is not boolean value : " + parser.currentToken.toString())
                    }
                } else if (FIELD_NAME_MODE == parser.currentName) {
                    if (parser.nextValue().isNumeric)
                        mode = parser.intValue
                } else if (FIELD_NAME_LEFT_BUTTON == parser.currentName) {
                    if (parser.nextValue().isBoolean)
                        isLeftButton = parser.booleanValue
                } else {
                    parser.skipChildren()
                }

            }
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(generator: JsonGenerator) {
        generator.writeNumber(id)
        generator.writeStartObject()
        generator.writeNumberField(FIELD_NAME_MODE, mode)
        generator.writeBooleanField(FIELD_NAME_LEFT_BUTTON, isLeftButton)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(mode)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        mode = source.readInt()
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_tab_list, null)
        val modeSpinner = view.findViewById<Spinner>(R.id.modeSpinner)
        val buttonSpinner = view.findViewById<Spinner>(R.id.buttonSpinner)

        modeSpinner.setSelection(mode)
        buttonSpinner.setSelection(if (isLeftButton) 1 else 0)
        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    mode = modeSpinner.selectedItemPosition
                    isLeftButton = buttonSpinner.selectedItemPosition > 0
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    companion object {
        const val MODE_NORMAL = 0
        const val MODE_REVERSE = 1
        const val MODE_HORIZONTAL = 2

        private const val TAG = "TabListSingleAction"
        private const val FIELD_NAME_REVERSE = "0"
        private const val FIELD_NAME_MODE = "1"
        private const val FIELD_NAME_LEFT_BUTTON = "2"

        @JvmField
        val CREATOR: Parcelable.Creator<TabListSingleAction> = object : Parcelable.Creator<TabListSingleAction> {
            override fun createFromParcel(source: Parcel): TabListSingleAction {
                return TabListSingleAction(source)
            }

            override fun newArray(size: Int): Array<TabListSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
