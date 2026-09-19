package com.unistack.app.feature_updates.domain

/**
 * Una escena fingida para el banco de pruebas: el estado, qué versión «llevas», el historial y
 * las publicaciones de las que salen fechas y pesos.
 *
 * Existe para comparar la app con el artifact con los mismos datos en los dos lados: la
 * comparación lado a lado no vale si en uno pone «1.0.1-alpha.6 · 12,6 MB» y en el otro
 * «1.0.0 · 23,8 MB».
 */
data class SimulatedUpdates(
    val state: UpdateState,
    val installedVersion: String,
    val history: List<InstalledVersion>,
    val releases: List<UpdateInfo>,
    val lastCheckedAt: Long
)
