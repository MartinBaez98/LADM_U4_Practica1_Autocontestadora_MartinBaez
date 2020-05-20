package mx.edu.ittepic.ladm_u4_prctica1_autocontestadora_martnbez

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_actualizar.*
import kotlinx.android.synthetic.main.insertar.*
import kotlinx.android.synthetic.main.insertar.deseado
import kotlinx.android.synthetic.main.insertar.nombre
import kotlinx.android.synthetic.main.insertar.regresar
import kotlinx.android.synthetic.main.insertar.telefono

class Actualizar : AppCompatActivity() {

    var id = ""
    var baseDatos = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar)

        var extras = intent.extras

        id = extras!!.getString("id").toString()

        nombre.setText(extras.getString("nombre"))
        telefono.setText(extras.getString("telefono"))
        if(extras.getBoolean("deseado") == true){
            deseado.isChecked = true
        }

        actualizar.setOnClickListener {
            baseDatos.collection("contactos").document(id)
                .update("nombre", nombre.text.toString(),
                    "telefono", telefono.text.toString(),
                    "deseado", deseado.isChecked() )
        }

        regresar.setOnClickListener {
            finish()
        }
    }
}
