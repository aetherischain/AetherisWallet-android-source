package com.breadwallet.presenter.terminal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.breadwallet.R
import com.breadwallet.wallet.BRPeerManager
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.tools.manager.BRSharedPrefs
import java.net.InetAddress

class TerminalFragment : Fragment() {

    private lateinit var syncStatusText: TextView
    private lateinit var nodesListText: TextView
    private lateinit var syncProgressBar: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(this, 2000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)
        syncStatusText = view.findViewById(R.id.sync_status_text)
        nodesListText = view.findViewById(R.id.nodes_list_text)
        syncProgressBar = view.findViewById(R.id.sync_progress_bar)
        return view
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateStatus() {
        val context = context ?: return
        val currentHeight = BRPeerManager.getCurrentBlockHeight()
        val estimatedHeight = BRPeerManager.getEstimatedBlockHeight()
        val startHeight = BRSharedPrefs.getStartHeight(context)
        val progressRaw = BRPeerManager.syncProgress(startHeight)
        val progress = (progressRaw * 100).toInt()

        val isConnected = BRPeerManager.getInstance().isConnected

        syncStatusText.text = "Status: ${if (isConnected) "CONNECTED" else "CONNECTING..."}\n" +
                "Sync: $progress% ($progressRaw)\n" +
                "Height: $currentHeight / $estimatedHeight\n" +
                "Start Height: $startHeight"
        syncProgressBar.progress = if (progress >= 0) progress else 0

        val peers = BRPeerManager.getInstance().connectedPeers
        val nodesInfo = StringBuilder()
        if (peers != null && peers.isNotEmpty()) {
            nodesInfo.append("Total Peers: ${peers.size}\n\n")
            for (peer in peers) {
                try {
                    val addr = peer.peerAddress
                    // BR core uses 16 byte IPv6/IPv4-mapped addresses
                    val inetAddr = InetAddress.getByAddress(addr)
                    val host = inetAddr.hostAddress.replace("/", "")
                    nodesInfo.append("> $host\n")
                } catch (e: Exception) {
                    nodesInfo.append("> Unknown Peer Data\n")
                }
            }
        } else {
            nodesInfo.append("Searching for peers...")
        }
        nodesListText.text = nodesInfo.toString()
    }
}