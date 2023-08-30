package com.nnt.calculadoraimc_turmac.model

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface CalculoDao {
    @Insert
    fun inserir(calculo: Calculo)

    //@Query - busca
    //@Delete - deletar
    //@Update - atualizar
}