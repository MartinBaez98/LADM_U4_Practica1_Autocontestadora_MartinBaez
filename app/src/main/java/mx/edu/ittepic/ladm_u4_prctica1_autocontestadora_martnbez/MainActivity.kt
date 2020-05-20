package mx.edu.ittepic.ladm_u4_prctica1_autocontestadora_martnbez

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.insertar.*

class MainActivity : AppCompatActivity() {

    var activado = false
    var baseRemota = FirebaseFirestore.getInstance()
    var msjDeseado = ""
    var msjNoDeseado = ""
    var permiso = 1

    var dataLista = ArrayList<String>()
    var listaID = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.SEND_SMS
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.SEND_SMS,android.Manifest.permission.READ_PHONE_STATE,android.Manifest.permission.READ_CALL_LOG), permiso
            )
        }

        setTitle("Auto Contestadora - Martín Báez")

        baseRemota.collection("contactos").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException != null){
                //Si es diferente de null, hay un error
                mensaje("ERROR: No se puede acceder a consulta")
                return@addSnapshotListener
            }
            dataLista.clear()
            listaID.clear()
            for(document in querySnapshot!!){
                var cadena = document.getString("nombre")+("\n")+document.getString("telefono")+
                        ("\n")+document.getBoolean("deseado").toString()
                dataLista.add(cadena)
                listaID.add(document.id)
            }
            if(dataLista.size == 0){
                dataLista.add("No hay datos")
            }
            var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataLista)
            lista.adapter = adapter
        }

        lista.setOnItemClickListener { parent, view, position, id ->
            if(listaID.size == 0){
                return@setOnItemClickListener
            }
            AlertaEliminar(position)
        }

        activar.setOnClickListener {
            if(activado == false) {
                activado = true
                mensaje("Autocontestar activado")
                activar.setText("Desactivar")

            }else{
                activado = false
                mensaje("Autocontestar desactivado")
                activar.setText("Activar")
            }

        }

        cambiarMensaje.setOnClickListener {
            msjDeseado = msjAgradable.text.toString()
            msjNoDeseado = msjNoAgradable.text.toString()
            msjAgradable.setText("")
            msjNoAgradable.setText("")
            mensaje("Mensajes cambiados correctamente")
        }

        agregar.setOnClickListener {
            construirDialogoInsertar()
        }


    }

    private fun AlertaEliminar(position: Int) {
        AlertDialog.Builder(this).setTitle("ATENCIÓN")
            .setMessage("¿Qué desea hacer con el contacto?")
            .setPositiveButton("Eliminar"){d,w ->
                eliminar(listaID[position])
            }
            .setNegativeButton("Actualizar"){d,w ->
                llamarVentanaActualizar(listaID[position])
            }
            .setNeutralButton("Cancelar"){dialog, wich ->

            }.show()
    }

    private fun llamarVentanaActualizar(idActualizar: String) {
        baseRemota.collection("contactos").document(idActualizar).get()
            .addOnSuccessListener {
                var v = Intent(this, Actualizar::class.java)
                v.putExtra("id", idActualizar)
                v.putExtra("nombre", it.getString("nombre"))
                v.putExtra("telefono", it.getString("telefono"))
                v.putExtra("deseado", it.getBoolean("deseado"))

                startActivity(v)
            }.addOnFailureListener {
                mensaje("ERROR: No hay conexión de red")
            }
    }

    private fun eliminar(idEliminar: String) {
        baseRemota.collection("evento").document(idEliminar).delete()
            .addOnSuccessListener {
                mensaje("Contacto eliminado de la lista de atendidos por esta app")
            }
            .addOnFailureListener {
               mensaje("No se pudo eliminar")
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permiso) {
        }

    }

    private fun construirDialogoInsertar(){
        var dialogoInsertar = Dialog(this)
        dialogoInsertar.setContentView(R.layout.insertar)

        //Declaración de objetos
        var nombre =dialogoInsertar.findViewById<EditText>(R.id.nombre)
        var telefono = dialogoInsertar.findViewById<EditText>(R.id.telefono)
        var deseado = dialogoInsertar.findViewById<CheckBox>(R.id.deseado)
        var registrar = dialogoInsertar.findViewById<Button>(R.id.registrar)
        var regresar = dialogoInsertar.findViewById<Button>(R.id.regresar)

        dialogoInsertar.show()

        registrar.setOnClickListener {
            insertarRegistro(nombre.text.toString(), telefono.text.toString(), deseado.isChecked())
            dialogoInsertar.dismiss()
        }

        regresar.setOnClickListener {
            dialogoInsertar.dismiss()
        }
    }

    private fun insertarRegistro(nombre: String, telefono: String, deseado: Boolean) {
        var data = hashMapOf(
            "nombre" to nombre,
            "telefono" to telefono,
            "deseado" to deseado
        )

        baseRemota.collection("contactos")
            .add(data)
            .addOnSuccessListener {
                mensaje("Registrado correctamente")

            }
            .addOnFailureListener {
                mensaje("No se pudo registrar")
            }
    }

    private fun mensaje(msj: String){
        Toast.makeText(this, msj, Toast.LENGTH_LONG).show()
    }

}


