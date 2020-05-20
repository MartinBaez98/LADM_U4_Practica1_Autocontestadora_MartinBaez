package mx.edu.ittepic.ladm_u4_prctica1_autocontestadora_martnbez

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class LlamadaEntrante : BroadcastReceiver() {
    private val TAG = "PhoneStatReceiver"
    private var entrando = false
    private var incoming_number: String? = null
    var msjDes = ""
    var msjNDes = ""

    var baseRemota = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            entrando = false
            val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        } else {

            val tm = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager

            when (tm.callState) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    entrando = true
                    incoming_number = intent.getStringExtra("incoming_number")
                    if(incoming_number!=null){

                        enviarMensaje(""+incoming_number,context)

                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> if (entrando) {

                }
                TelephonyManager.CALL_STATE_IDLE -> if (entrando) {

                }
            }
        }
    }

    private fun enviarMensaje(numero: String, context: Context) {
        var deseado = true
        var mensaje = ""
        msjDes = MainActivity().msjDeseado
        msjNDes = MainActivity().msjNoDeseado

        baseRemota.collection("contactos").whereEqualTo("numero", numero)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                for(document in querySnapshot!!){
                    deseado = document.getBoolean("deseado")!!
                }
            }

        if(deseado){
            mensaje = msjDes
        }else{
            mensaje = msjNDes
        }

        SmsManager.getDefault().sendTextMessage(numero, null, mensaje, null, null)
    }



}