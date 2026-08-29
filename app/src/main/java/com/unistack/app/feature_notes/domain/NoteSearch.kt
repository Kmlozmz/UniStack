package com.unistack.app.feature_notes.domain

import java.text.Normalizer
import java.util.Locale

/**
 * Buscar dentro de las notas, y encontrar la materia al escribir `@`.
 *
 * Las dos cosas comparten la misma regla y por eso viven juntas: **las tildes no cuentan**.
 * Nadie escribe «Cálculo» con tilde en el buscador de su propia app, y una búsqueda que no
 * encuentra «Cálculo II» al teclear «calculo» se siente rota aunque técnicamente sea correcta.
 *
 * La comparación quita también los acentos del texto guardado, no solo los de lo que se teclea:
 * si solo se limpiara la búsqueda, escribir «cálculo» con tilde dejaría de encontrar nada.
 */
object NoteSearch {

    private val ACENTOS = Regex("\\p{Mn}+")

    /** Un texto reducido a lo que de verdad se compara: sin tildes y en minúsculas. */
    fun normalize(text: String): String {
        val descompuesto = Normalizer.normalize(text, Normalizer.Form.NFD)
        return ACENTOS.replace(descompuesto, "").lowercase(Locale.ROOT).trim()
    }

    /**
     * Si una nota responde a lo que se busca.
     *
     * Se busca en el texto **sin las marcas**: quien teclea «parcial» espera encontrar una nota
     * titulada `## Parcial 2`, y las almohadillas no son parte de lo que escribió.
     */
    fun matches(note: QuickNote, query: String): Boolean {
        val aguja = normalize(query)
        if (aguja.isEmpty()) return true
        return normalize(NoteMarkdown.strip(note.body)).contains(aguja)
    }

    fun filter(notes: List<QuickNote>, query: String): List<QuickNote> {
        if (normalize(query).isEmpty()) return notes
        return notes.filter { matches(it, query) }
    }

    /**
     * Las materias que encajan con lo tecleado tras la arroba.
     *
     * Primero las que **empiezan** por lo escrito y después las que solo lo contienen: quien
     * teclea «cal» busca «Cálculo II» antes que «Química Vertical», aunque las dos valgan.
     */
    fun matchingSubjects(names: List<Pair<String, String>>, token: String): List<Pair<String, String>> {
        val aguja = normalize(token)
        if (aguja.isEmpty()) return names
        val empiezan = names.filter { normalize(it.second).startsWith(aguja) }
        val contienen = names.filter { normalize(it.second).contains(aguja) } - empiezan.toSet()
        return empiezan + contienen
    }
}
