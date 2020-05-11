package com.luigivampa92.nfcshare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class AddRecordFragment : AppCompatDialogFragment() {

    companion object {

        const val FRAGMENT_TAG = "com.luigivampa92.nfcshare.AddRecordFragment"

        private const val EXTRA_PROXY_HOST = "com.luigivampa92.nfcshare.proxy.host"
        private const val EXTRA_PROXY_PORT = "com.luigivampa92.nfcshare.proxy.port"
        private const val EXTRA_PROXY_SECRET = "com.luigivampa92.nfcshare.proxy.secret"

        private val REGEX_HOST = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\$").toRegex()
        private val REGEX_PORT = Pattern.compile("^([0-9]{1,5})$").toRegex()
        private val REGEX_SECRET = Pattern.compile("^[a-fA-F0-9]+\$").toRegex()

        fun newInstance() = AddRecordFragment()

        fun newInstance(proxyRecord: ProxyRecord): AddRecordFragment {
            val fragment = newInstance()
            val bundle = Bundle()
            bundle.putString(EXTRA_PROXY_HOST, proxyRecord.address)
            bundle.putString(EXTRA_PROXY_PORT, proxyRecord.port)
            bundle.putString(EXTRA_PROXY_SECRET, proxyRecord.secret)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var textInputLayoutHost: TextInputLayout
    private lateinit var textInputLayoutPort: TextInputLayout
    private lateinit var textInputLayoutSecret: TextInputLayout
    private lateinit var editTextHost: TextInputEditText
    private lateinit var editTextPort: TextInputEditText
    private lateinit var editTextSecret: TextInputEditText
    private lateinit var buttonAdd: AppCompatButton
    private lateinit var buttonCancel: AppCompatButton
    private lateinit var errorStringInvalidValue: String

    private var onSuccess: ((ProxyRecord) -> Unit)? = null
    private var onDismiss: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_add_record, container, false).also { view ->
                initUi(view)
                removeErrorHighlights()
                getPassedProxyRecord()?.let {
                    editTextHost.setText(it.address)
                    editTextPort.setText(it.port)
                    editTextSecret.setText(it.secret)
                }
                buttonAdd.setOnClickListener {
                    removeErrorHighlights()
                    val insertedProxyRecord = getInsertedProxyRecord()
                    insertedProxyRecord?.let {
                        dismiss()
                        onSuccess?.invoke(it)
                    }
                }
                buttonCancel.setOnClickListener {
                    removeErrorHighlights()
                    dismiss()
                    onDismiss?.invoke()
                }
            }

    fun setCallbacks(onSuccess: (ProxyRecord) -> Unit, onDismiss: (() -> Unit)? = null) {
        this.onSuccess = onSuccess
        this.onDismiss = onDismiss
    }

    fun removeCallbacks() {
        onSuccess = null
        onDismiss = null
    }

    private fun initUi(rootView: View) {
        textInputLayoutHost = rootView.findViewById(R.id.input_layout_host)
        textInputLayoutPort = rootView.findViewById(R.id.input_layout_port)
        textInputLayoutSecret = rootView.findViewById(R.id.input_layout_secret)
        editTextHost = rootView.findViewById(R.id.edit_text_host)
        editTextPort = rootView.findViewById(R.id.edit_text_port)
        editTextSecret = rootView.findViewById(R.id.edit_text_secret)
        buttonAdd = rootView.findViewById(R.id.button_add)
        buttonCancel = rootView.findViewById(R.id.button_cancel)
        errorStringInvalidValue = getString(R.string.text_error_invalid_value)
    }

    private fun getPassedProxyRecord(): ProxyRecord? {
        val host = arguments?.getString(EXTRA_PROXY_HOST)
        val port = arguments?.getString(EXTRA_PROXY_PORT)
        val secret = arguments?.getString(EXTRA_PROXY_SECRET)

        if (
            host.isNullOrBlank() || !host.matches(REGEX_HOST)
            || port.isNullOrBlank() || !port.matches(REGEX_PORT)
            || secret.isNullOrBlank() || !secret.matches(REGEX_SECRET)
        ) {
            return null
        }

        return ProxyRecord(host, port, secret)
    }

    private fun removeErrorHighlights() {
        textInputLayoutHost.error = ""
        textInputLayoutPort.error = ""
        textInputLayoutSecret.error = ""
    }

    private fun getInsertedProxyRecord(): ProxyRecord? {
        val host = editTextHost.text.toString()
        if (host.isBlank() || !host.matches(REGEX_HOST)) {
            textInputLayoutHost.error = errorStringInvalidValue
            return null
        }

        val port = editTextPort.text.toString()
        if (port.isBlank() || !port.matches(REGEX_PORT)) {
            textInputLayoutPort.error = errorStringInvalidValue
            return null
        }

        val secret = editTextSecret.text.toString()
        if (secret.isBlank() || !secret.matches(REGEX_SECRET)) {
            textInputLayoutSecret.error = errorStringInvalidValue
            return null
        }

        return ProxyRecord(host, port, secret)
    }
}