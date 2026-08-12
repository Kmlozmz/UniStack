package com.unistack.app.core.utils

/**
 * Lo que se muestra donde debería ir una cifra que todavía no existe.
 *
 * La distinción importa y la app ya la tenía a medias: un `0` es un dato —«no tienes
 * pendientes» informa— mientras que la raya dice «aún no hay nada que calcular», como
 * un promedio sin notas registradas. Poner `0` donde no hay dato inventa información;
 * poner la raya donde el cero es real la esconde.
 *
 * Existe como constante porque el glifo estaba escrito a mano en trece sitios con dos
 * formas distintas: unos usaban dos guiones y otros una raya, y al navegar entre Inicio
 * y Académico se veían las dos seguidas para decir exactamente lo mismo.
 */
const val NO_DATA = "—"
