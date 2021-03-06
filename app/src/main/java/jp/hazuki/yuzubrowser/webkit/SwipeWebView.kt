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

package jp.hazuki.yuzubrowser.webkit

import android.content.Context
import android.graphics.Paint
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import android.view.ViewGroup
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.theme.ThemeData
import jp.hazuki.yuzubrowser.utils.ThemeUtils

class SwipeWebView private constructor(context: Context, override val webView: NormalWebView) : SwipeRefreshLayout(context), CustomWebView by webView, SwipeRefreshLayout.OnRefreshListener, OnSwipeableChangeListener {

    constructor(context: Context) : this(context, WebViewProvider.getInstance(context))

    private var enableSwipe = false

    private val mWebChromeClientWrapper = object : CustomWebChromeClientWrapper(this) {
        override fun onProgressChanged(web: CustomWebView, newProgress: Int) {
            if (isRefreshing && newProgress > 80)
                isRefreshing = false

            super.onProgressChanged(web, newProgress)
        }
    }

    private val mWebViewClientWrapper = CustomWebViewClientWrapper(this)

    init {
        webView.setOnScrollableChangeListener(this)
        addView(webView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        setOnRefreshListener(this)
    }

    override fun setMyWebChromeClient(client: CustomWebChromeClient?) {
        mWebChromeClientWrapper.setWebChromeClient(client)
        webView.setMyWebChromeClient(mWebChromeClientWrapper)
    }

    override fun setMyWebViewClient(client: CustomWebViewClient?) {
        mWebViewClientWrapper.setWebViewClient(client)
        webView.setMyWebViewClient(mWebViewClientWrapper)
    }

    override val view: View
        get() = this

    override var swipeEnable: Boolean
        get() = enableSwipe
        set(value) {
            enableSwipe = value
            isEnabled = value
        }

    override fun resetTheme() {
        if (ThemeData.isEnabled() && ThemeData.getInstance().progressColor != 0) {
            setColorSchemeColors(ThemeData.getInstance().progressColor)
            if (ThemeData.getInstance().refreshUseDark) {
                setProgressBackgroundColorSchemeColor(ResourcesCompat.getColor(resources, R.color.deep_gray, context.theme))
            }
        } else {
            setColorSchemeResources(ThemeUtils.getIdFromThemeRes(context, R.attr.colorAccent))
        }
    }

    override fun onRefresh() {
        webView.reload()
        postDelayed({ isRefreshing = false }, TIMEOUT.toLong())
    }

    override fun onSwipeableChanged(scrollable: Boolean) {
        super.setEnabled(enableSwipe && scrollable)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enableSwipe && isScrollable)
    }

    override fun setVerticalScrollBarEnabled(enabled: Boolean) {
        webView.isVerticalScrollBarEnabled = enabled
    }

    override fun hasFocus(): Boolean = webView.hasFocus()

    override fun scrollBy(x: Int, y: Int) = webView.scrollBy(x, y)

    override fun scrollTo(x: Int, y: Int) = webView.scrollTo(x, y)

    override fun setLayerType(layerType: Int, paint: Paint?) = webView.setLayerType(layerType, paint)

    override fun setScrollBarStyle(style: Int) {
        webView.scrollBarStyle = style
    }

    companion object {
        private const val TIMEOUT = 7500
    }
}