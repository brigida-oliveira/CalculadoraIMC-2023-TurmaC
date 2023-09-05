package com.nnt.calculadoraimc_turmac

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nnt.calculadoraimc_turmac.databinding.ActivityListaDeCalculosBinding
import com.nnt.calculadoraimc_turmac.model.Calculo
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.Locale

class ListaDeCalculosActivity : AppCompatActivity(), OnListClickListener {

    private lateinit var binding: ActivityListaDeCalculosBinding
    private lateinit var resultado: MutableList<Calculo>
    private lateinit var adapter: ListaDeCalculosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaDeCalculosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resultado = mutableListOf<Calculo>()
        adapter = ListaDeCalculosAdapter(resultado, this)

        binding.recyclerViewCalculos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCalculos.adapter = adapter

        val tipo = intent?.extras?.getString("tipo") ?: throw IllegalStateException("Tipo não encontrado!")

        Thread {
            val app = application as App
            val dao = app.db.calculoDao()
            val resposta = dao.buscarRegistroPorTipo(tipo)

            runOnUiThread {
                resultado.addAll(resposta)
                adapter.notifyDataSetChanged()
            }
        }.start()
    }

    private inner class ListaDeCalculosAdapter(
        private val listaCalculo: List<Calculo>,
        private val listener: OnListClickListener
        ): RecyclerView.Adapter<ListaDeCalculosAdapter.ListaCalculosViewHolder>(){

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ListaDeCalculosAdapter.ListaCalculosViewHolder {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            return  ListaCalculosViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ListaDeCalculosAdapter.ListaCalculosViewHolder,
            position: Int
        ) {
            val itemAtual = listaCalculo[position]
            holder.bind(itemAtual)
        }

        override fun getItemCount(): Int {
            return listaCalculo.size
        }

        private inner class ListaCalculosViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun bind(item: Calculo) {
                val textView = itemView as TextView
                val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

                val resultado = item.resultado //calculo de IMC ou TMB
                val data = simpleDateFormat.format(item.data) //data formatada

                textView.text = "Resultado: $resultado     Data: $data"

                textView.setOnClickListener {
                    listener.onClick(item.id, item.tipo)
                }

                textView.setOnLongClickListener {
                    listener.onLongClick(adapterPosition, item)
                    true
                }
            }
        }
    }

    override fun onClick(id: Int, tipo: String) {
        when(tipo){
            "imc" -> {
                val intent = Intent(this, ImcActivity::class.java)
                intent.putExtra("atualizarId", id)
                startActivity(intent)
            }
            "tmb" -> {
                val intent = Intent(this, TMBActivity::class.java)
                intent.putExtra("atualizarId", id)
                startActivity(intent)
            }
        }
    }

    override fun onLongClick(position: Int, calculo: Calculo) {
        AlertDialog.Builder(this)
            .setMessage("Você quer mesmo apagar esse registro?")
            .setNegativeButton("Não") { dialog, wich ->
                dialog.dismiss()
            }
            .setPositiveButton("Sim"){ dialog, wich ->
                Thread {
                    val app = application as App
                    val dao = app.db.calculoDao()
                    val resposta = dao.apagar(calculo)

                    if(resposta > 0) {
                        runOnUiThread {
                            resultado.removeAt(position)
                            adapter.notifyItemRemoved(position)
                        }
                    }
                }.start()
            }
            .create()
            .show()
    }
}