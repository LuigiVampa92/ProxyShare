package com.luigivampa92.nfcshare.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.luigivampa92.nfcshare.*
import com.luigivampa92.nfcshare.data.ProxyRecordEntity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import androidx.recyclerview.widget.ItemTouchHelper

class MainActivity : BaseActivity(), RecyclerViewItemTouchHelper.RecyclerItemTouchHelperListener {

    companion object {

        private const val EXTRA_URI = "com.luigivampa92.nfcshare.ui.MainActivity.uri"

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)

        fun newIntent(context: Context, uri: Uri? = null): Intent {
            val intent = newIntent(context)
            if (uri != null) {
                intent.putExtra(EXTRA_URI, uri.toString())
            }
            return intent
        }
    }

    private lateinit var bannerEnableNfc: ViewGroup
    private lateinit var bannerEnableNfcButton: MaterialButton
    private lateinit var fab: FloatingActionButton
    private lateinit var recyclerViewRecords: RecyclerView
    private lateinit var textNoRecords: TextView
    private lateinit var adapter: RecordAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private val compositeDisposable = CompositeDisposable()
    private lateinit var nfcAdapter: NfcAdapter

    private lateinit var dataStorage: DataStorage
    private var recordHce: Record? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bannerEnableNfc = findViewById(R.id.banner_enable_nfc)
        bannerEnableNfcButton = findViewById(R.id.banner_enable_nfc_button)
        bannerEnableNfcButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }

        fab = findViewById(R.id.fab)
        fab.setColorFilter(Color.WHITE)
        fab.setOnClickListener { openDialogWithProxyRecord(null) }

        textNoRecords = findViewById(R.id.text_no_records)

        recyclerViewRecords = findViewById(R.id.recycler_view_records)
        adapter = RecordAdapter(this::recordClicked)
        layoutManager = LinearLayoutManager(this)
        recyclerViewRecords.adapter = adapter
        recyclerViewRecords.layoutManager = layoutManager
        recyclerViewRecords.itemAnimator = DefaultItemAnimator()
        val itemTouchHelperCallback = RecyclerViewItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewRecords)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        intent.getStringExtra(EXTRA_URI)?.let {
            try {
                val uri = Uri.parse(it)
                handleUri(uri)
            }
            catch (e: Throwable) {
                log("ERROR: ${e.message.toString()}")
            }
        }

        reloadRecords()
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter.isEnabled) {
            bannerEnableNfc.setVisibility(false)
            val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
        }
        else {
            bannerEnableNfc.setVisibility(true)
        }
    }

    override fun onPause() {
        if (nfcAdapter.isEnabled) {
            nfcAdapter.disableForegroundDispatch(this)
        }
        super.onPause()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        val record = adapter.getItem(position)
        if (record.selectedForHce) {
            dataStorage.setHceEnabled(false)
        }
        deleteRecord(record.value)
        adapter.removeItem(position)
        showNoErrors()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                try {
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                        val ndefMessages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                        val receivedNdefRecord = ndefMessages[0].records[0]
                        if (
                            receivedNdefRecord.tnf == 1.toShort()
                            && receivedNdefRecord.type != null && receivedNdefRecord.type.isNotEmpty() && receivedNdefRecord.type[0] == 85.toByte()
                        ) {
                            val receivedUri = receivedNdefRecord.toUri()
                            handleUri(receivedUri)
                        }
                    }
                }
                catch (e: Throwable) {
                    log("ERROR: ${e.message.toString()}")
                }
            }
        }
    }

    private fun handleUri(receivedUri: Uri) {
        if (
            (receivedUri.scheme == "tg" && receivedUri.host == "proxy")
            || (receivedUri.scheme == "http" && receivedUri.host == "t.me" && receivedUri.path == "proxy")
            || (receivedUri.scheme == "https" && receivedUri.host == "t.me" && receivedUri.path == "proxy")
            || (receivedUri.scheme == "http" && receivedUri.host == "telegram.me" && receivedUri.path == "proxy")
            || (receivedUri.scheme == "https" && receivedUri.host == "telegram.me" && receivedUri.path == "proxy")
            || (receivedUri.scheme == "http" && receivedUri.host == "telegram.dog" && receivedUri.path == "proxy")
            || (receivedUri.scheme == "https" && receivedUri.host == "telegram.dog" && receivedUri.path == "proxy")
        ) {
            val queryParameterNames = receivedUri.queryParameterNames
            if (
                queryParameterNames.size == 3
                && queryParameterNames.contains("server")
                && queryParameterNames.contains("port")
                && queryParameterNames.contains("secret")
            ) {
                val server = receivedUri.getQueryParameter("server")!!
                val port = receivedUri.getQueryParameter("port")!!
                val secret = receivedUri.getQueryParameter("secret")!!

                val record = ProxyRecord(server, port, secret)
                openDialogWithProxyRecord(record)
            }
        }
    }

    private fun reloadRecords() {
        var persistedMessage: String? = null
        dataStorage = DataStorage(this)
        if (dataStorage.isHceEnabled() && dataStorage.isMessagePersisted()) {
            persistedMessage = dataStorage.getMessage()
        }
        else {
            dataStorage.setHceEnabled(false)
        }

        compositeDisposable.add(
            NfcShareApplication.getDatabase().proxyDao().all
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        val records = it
                            .map { it.toProxyRecord() }
                            .map { Record(it, it.asTgUri() == persistedMessage) }
                        adapter.setRecords(records)
                        showNoErrors()
                    },
                    {
                        log("ERROR: ${it.message.toString()}")
                    }
                )
        )
    }

    private fun insertRecord(proxyRecord: ProxyRecord) {
        compositeDisposable.add(
            NfcShareApplication.getDatabase().proxyDao()
                .insert(ProxyRecordEntity(proxyRecord.address, proxyRecord.port, proxyRecord.secret))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        reloadRecords()
                    },
                    {
                        log("ERROR: ${it.message.toString()}")
                    }
                )
        )
    }

    private fun deleteRecord(proxyRecord: ProxyRecord) {
        compositeDisposable.add(
            NfcShareApplication.getDatabase().proxyDao()
                .delete(ProxyRecordEntity(proxyRecord.address, proxyRecord.port, proxyRecord.secret))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        reloadRecords()
                    },
                    {
                        log("ERROR: ${it.message.toString()}")
                    }
                )
        )
    }

    private fun recordClicked(record: Record) {
        adapter.changeRecordHceEnabled(record)

        if (record.selectedForHce) {
            recordHce = null
            dataStorage.setHceEnabled(false)
        }
        else {
            recordHce = record
            dataStorage.setHceEnabled(true)
            dataStorage.setMessage(record.value.asTgUri())
        }
    }

    private fun openDialogWithProxyRecord(record: ProxyRecord?) {
        val dialog = if (record != null)
            AddRecordFragment.newInstance(record)
        else
            AddRecordFragment.newInstance()
        dialog.isCancelable = false
        dialog.setCallbacks(
            {
                dialog.removeCallbacks()
                insertRecord(it)
            }
        )
        val existingFragment = supportFragmentManager.findFragmentByTag(AddRecordFragment.FRAGMENT_TAG)
        if (existingFragment != null) {
            (existingFragment as AppCompatDialogFragment).dismiss()
        }
        dialog.show(supportFragmentManager, AddRecordFragment.FRAGMENT_TAG)
    }

    private fun showNoErrors() {
        textNoRecords.setVisibility(adapter.itemCount == 0)
    }
}
