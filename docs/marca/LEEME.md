# Los originales de la marca

Aquí están los dos PNG del símbolo tal como salieron del diseño: el de color y su versión
en blanco. **No se empaquetan en el APK**: la app dibuja el símbolo desde
`UniStackBrandMark.Pills`, en código, para que el color de marca se defina una sola vez y lo
sigan por igual la pantalla de arranque y Acerca de.

Vivieron en `app/src/main/res/drawable-nodpi/` hasta el 21 ago 2026. Se movieron aquí, y no
se borraron, porque de `simbolo-original.png` salieron —escaneando su canal alfa— las medidas
exactas que usa el dibujo: si alguna vez hay que rehacer ese cálculo, este es el archivo del
que se sacó.
