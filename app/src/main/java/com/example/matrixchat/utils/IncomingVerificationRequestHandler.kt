package com.example.matrixchat.utils

import com.example.matrixchat.Matrix
import com.example.matrixchat.MyApplication
import org.matrix.androidsdk.crypto.verification.SASVerificationTransaction
import org.matrix.androidsdk.crypto.verification.VerificationManager
import org.matrix.androidsdk.crypto.verification.VerificationTransaction

/**
 * Listens to the VerificationManager and a new notification when an incoming request is detected.
 */
object IncomingVerificationRequestHandler : VerificationManager.VerificationManagerListener {


    override fun transactionCreated(tx: VerificationTransaction) {}

    override fun transactionUpdated(tx: VerificationTransaction) {
        if (tx is SASVerificationTransaction) {
            when (tx.state) {
                SASVerificationTransaction.SASVerificationTxState.OnStarted -> {
                    //Add a notification for every incoming request
                    val context = MyApplication.getInstance()
                    val session = Matrix.getInstance(context).defaultSession
                    val name = session.dataHandler.getUser(tx.otherUserId)?.displayname
                        ?: tx.otherUserId
                }

                            /*

                    val alert = PopupAlertManager.VectorAlert(
                        "kvr_${tx.transactionId}",
                        context.getString(R.string.sas_incoming_request_notif_title),
                        context.getString(R.string.sas_incoming_request_notif_content, name),
                        R.drawable.shield
                    ).apply {
                        contentAction = Runnable {
                            val intent = SASVerificationActivity.incomingIntent(context,
                                session.myUserId,
                                tx.otherUserId,
                                tx.transactionId)
                            weakCurrentActivity?.get()?.startActivity(intent)
                        }
                        dismissedAction = Runnable {
                            tx.cancel(session, CancelCode.User)
                        }
                        addButton(
                            context.getString(R.string.ignore),
                            Runnable {
                                tx.cancel(session, CancelCode.User)
                            }
                        )
                        addButton(
                            context.getString(R.string.action_open),
                            Runnable {
                                val intent = SASVerificationActivity.incomingIntent(context,
                                    session.myUserId,
                                    tx.otherUserId,
                                    tx.transactionId)
                                weakCurrentActivity?.get()?.startActivity(intent)
                            }
                        )
                        //10mn expiration
                        expirationTimestamp = System.currentTimeMillis() + (10 * 60 * 1000L)

                    }
                    PopupAlertManager.postVectorAlert(alert)
                }
                SASVerificationTransaction.SASVerificationTxState.Cancelled,
                SASVerificationTransaction.SASVerificationTxState.OnCancelled,
                SASVerificationTransaction.SASVerificationTxState.Verified -> {
                    //cancel related notification
                    PopupAlertManager.cancelAlert("kvr_${tx.transactionId}")
                }
                */
                else -> Unit

            }

        }

    }

    fun initialize(verificationManager: VerificationManager) {
        verificationManager.addListener(this)
    }

    override fun markedAsManuallyVerified(userId: String, deviceId: String) {

    }
}
