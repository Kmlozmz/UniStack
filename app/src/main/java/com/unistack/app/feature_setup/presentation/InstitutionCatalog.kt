package com.unistack.app.feature_setup.presentation

import java.text.Normalizer
import java.util.Locale

/**
 * Lista semilla de instituciones para sugerir mientras el usuario escribe.
 *
 * NO es un catálogo cerrado ni pretende ser exhaustivo: es un atajo para los casos más
 * frecuentes. El campo siempre acepta texto libre, y lo que se guarda en el perfil es
 * exactamente lo que la persona escribió, sin normalizar.
 *
 * Su razón de ser es la calidad del dato en origen: si la mayoría elige de la lista, se
 * evita acumular «U. Nacional», «Uni Nacional» y «Universidad Nacional» como entradas
 * distintas. Conviene revisarla y ampliarla; está orientada a Colombia, que es la
 * configuración regional que usa el resto de la app.
 */
object InstitutionCatalog {

    private val universities = listOf(
        "Universidad Nacional de Colombia",
        "Universidad de Antioquia",
        "Universidad de los Andes",
        "Pontificia Universidad Javeriana",
        "Universidad del Valle",
        "Universidad Industrial de Santander",
        "Universidad del Rosario",
        "Universidad del Norte",
        "Universidad EAFIT",
        "Universidad Externado de Colombia",
        "Universidad Pedagógica Nacional",
        "Universidad Tecnológica de Pereira",
        "Universidad de Caldas",
        "Universidad de Cartagena",
        "Universidad de Nariño",
        "Universidad del Atlántico",
        "Universidad del Cauca",
        "Universidad de Córdoba",
        "Universidad Distrital Francisco José de Caldas",
        "Universidad Militar Nueva Granada",
        "Universidad Pontificia Bolivariana",
        "Universidad Santo Tomás",
        "Universidad de La Sabana",
        "Universidad Sergio Arboleda",
        "Universidad EAN",
        "Universidad Central",
        "Universidad Libre",
        "Universidad Autónoma de Occidente",
        "Universidad Autónoma de Bucaramanga",
        "Universidad ICESI",
        "Universidad de Medellín",
        "Universidad de La Salle",
        "Universidad Católica de Colombia",
        "Universidad Manuela Beltrán",
        "Universidad Cooperativa de Colombia",
        
/*      "Servicio Nacional de Aprendizaje (SENA)",
        "Politécnico Grancolombiano",
        "Fundación Universitaria del Área Andina",
        "Corporación Universitaria Minuto de Dios (UNIMINUTO)",
        "Escuela Colombiana de Ingeniería Julio Garavito"
*/        
    )

    /**
     * Sugerencias para [query], ignorando mayúsculas y tildes para que «javeriana» y
     * «Javeriana» encuentren lo mismo. Con la consulta vacía no sugiere nada: el campo es
     * opcional y no conviene empujar a rellenarlo.
     */
    fun suggestionsFor(query: String, limit: Int = 5): List<String> {
        val needle = query.normalizeForSearch()
        if (needle.length < 2) return emptyList()
        return universities
            .filter { it.normalizeForSearch().contains(needle) }
            .take(limit)
    }

    private fun String.normalizeForSearch(): String =
        Normalizer.normalize(trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
}
