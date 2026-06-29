package com.pingbridge.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.pingbridge.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BridgeAccessibilityService : AccessibilityService() {

    private lateinit var prefsManager: PreferencesManager
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var targetPackage: String? = null
    private var replyText: String? = null
    private var attempts = 0
    private val MAX_ATTEMPTS = 10

    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString() ?: return

                scope.launch {
                    val pendingTarget = prefsManager.getPendingReplyTarget()
                    if (pendingTarget != null && packageName == pendingTarget) {
                        targetPackage = pendingTarget
                        replyText = prefsManager.getPendingReplyText()
                        attempts = 0
                        mainHandler.postDelayed({ attemptInject() }, 800)
                    }
                }
            }
        }
    }

    private fun attemptInject() {
        if (targetPackage == null || replyText == null) return

        val rootNode = rootInActiveWindow ?: run {
            if (attempts < MAX_ATTEMPTS) {
                attempts++
                mainHandler.postDelayed({ attemptInject() }, 500)
            } else {
                clearPending()
            }
            return
        }

        val textField = findTextField(rootNode)
        if (textField == null) {
            if (attempts < MAX_ATTEMPTS) {
                attempts++
                mainHandler.postDelayed({ attemptInject() }, 500)
            } else {
                clearPending()
            }
            rootNode.recycle()
            return
        }

        val sendButton = findSendButton(rootNode)

        injectReply(textField, sendButton)
        rootNode.recycle()
    }

    private fun findTextField(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val textFields = mutableListOf<AccessibilityNodeInfo>()
        findNodesByClass(root, "android.widget.EditText", textFields)

        if (textFields.isEmpty()) {
            findNodesByClass(root, "android.widget.MultiAutoCompleteTextView", textFields)
        }

        val best = textFields.maxByOrNull { it.isEnabled && it.isVisibleToUser }
        return best?.also { node ->
            textFields.filter { it != node }.forEach { it.recycle() }
        }
    }

    private fun findSendButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val clickableNodes = mutableListOf<AccessibilityNodeInfo>()
        collectClickableNodes(root, clickableNodes)

        val sendDescriptions = setOf(
            "send", "Send", "SEND",
            "envoyer", "Envoyer", "ENVOYER",
            "\uD83D\uDE80", "send message", "kirim", "verzenden",
            "invia", "enviar", "env\u00EDar", "senden",
            "\uF52C\u000A", "\uE725"
        )

        val matching = clickableNodes.filter { node ->
            val cd = node.contentDescription?.toString() ?: ""
            val txt = node.text?.toString() ?: ""
            val viewId = node.viewIdResourceName?.substringAfterLast("/") ?: ""

            sendDescriptions.any { desc ->
                cd.equals(desc, ignoreCase = true) ||
                txt.equals(desc, ignoreCase = true) ||
                viewId.equals(desc, ignoreCase = true) ||
                viewId.contains("send", ignoreCase = true) ||
                viewId.contains("btn_send", ignoreCase = true) ||
                viewId.contains("composer_send", ignoreCase = true)
            }
        }

        val sorted = matching.sortedByDescending { it.isEnabled && it.isVisibleToUser }
        return sorted.firstOrNull()?.also { result ->
            matching.filter { it != result }.forEach { it.recycle() }
            clickableNodes.filter { it != result && it !in matching }.forEach { it.recycle() }
        }
    }

    private fun injectReply(textField: AccessibilityNodeInfo, sendButton: AccessibilityNodeInfo?) {
        val textToSend = replyText ?: return

        val bundle = Bundle()
        bundle.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            textToSend
        )
        textField.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        textField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)

        if (sendButton != null) {
            mainHandler.postDelayed({
                sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                sendButton.recycle()
                clearPending()
            }, 400)
        } else {
            mainHandler.postDelayed({
                clearPending()
            }, 300)
        }

        textField.recycle()
    }

    private fun clearPending() {
        targetPackage = null
        replyText = null
        attempts = 0
        scope.launch {
            prefsManager.clearPendingReply()
        }
    }

    private fun findNodesByClass(
        node: AccessibilityNodeInfo,
        className: String,
        results: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.className?.toString() == className) {
            val clone = AccessibilityNodeInfo.obtain(node)
            results.add(clone)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findNodesByClass(child, className, results)
            child.recycle()
        }
    }

    private fun collectClickableNodes(
        node: AccessibilityNodeInfo,
        results: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.isClickable) {
            val clone = AccessibilityNodeInfo.obtain(node)
            results.add(clone)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectClickableNodes(child, results)
            child.recycle()
        }
    }

    override fun onInterrupt() {}
}
