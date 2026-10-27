# Revisión inicial para portafolio

Fecha: 27 de octubre de 2026.

## Autoría

Se conservan los créditos de Francisco Vallverdú y Diego Ramos. Diego identificó como su aporte los microservicios rutasMS y flotaMS. No se atribuyeron individualmente los demás servicios. El usuario frvallverdu se obtuvo de la dirección remota y del historial de autores; la disponibilidad pública del perfil y repositorio de origen no pudo confirmarse con las herramientas de consulta.

## Copia

Se copió el estado de trabajo actual, incluidos dos archivos Java con modificaciones locales pendientes en el original. No se copiaron .git, .env, configuraciones de editor, resultados de compilación ni documentos Word sin revisar. Se incluyó el diagrama PNG revisado visualmente como diseño propuesto, diferenciándolo de los componentes presentes en el código.

El archivo .env aparece en el historial del repositorio original; por ello la copia no arrastra ese historial. Esto no establece si los valores históricos siguen vigentes ni si el repositorio remoto es público. No se leyeron ni publicaron sus valores en la documentación de la copia.

## Validaciones realizadas

- Se verificaron huellas de los archivos Java, XML y properties del original antes y después: no cambiaron.
- Los cuatro servicios compilaron y generaron JAR con Java 21 y Maven, sin descargar dependencias nuevas y con -DskipTests.
- Docker Compose pasó la validación de configuración. Después se construyeron las cuatro imágenes y arrancaron los ocho contenedores del proyecto aislado `rutalimpia-portafolio`, con cuatro bases MySQL saludables.
- El generador de .env se probó en una carpeta temporal. Produce una clave JWT Base64 de 32 bytes y una contraseña aleatoria, crea el archivo con permisos 0600 y rechaza sobrescribir uno existente. No muestra los valores generados.
- Para ejecutar las pruebas se generó un .env local con claves nuevas, excluido por .gitignore. .env.example contiene únicamente marcadores. La copia no incorpora el historial Git original. También se excluyeron .continue y las cachés de Python.

## Primera prueba integrada

Las cuatro pruebas contextLoads se ejecutaron con Maven y MySQL: 4 casos, 0 fallos, 0 errores y 0 omitidos. Los registros locales están en /tmp/rutalimpia-<servicio>-contexto.log.

El script herramientas/probar_flujo.py terminó con 13 comprobaciones aprobadas:

1. Alta de vecino y rechazo de correo duplicado.
2. Inicio de sesión y consulta del perfil.
3. Rechazo del acceso sin token a perfil y solicitudes.
4. Creación de solicitud PENDIENTE.
5. Rechazo de asignación con rol VECINO (HTTP 401 observado).
6. Alta de camión y rechazo de patente duplicada y capacidad negativa.
7. Alta de ruta y rechazo de camión y fecha duplicados.
8. Reversión de la parada local si Solicitudes rechaza el rol.
9. Asignación comunicada mediante OpenFeign y estado ASIGNADA comprobado en Solicitudes.
10. Rechazo de eliminación de ruta con paradas.
11. Resultado EXITOSO comunicado a Solicitudes y estado REALIZADA comprobado.
12. Rechazo de resultado duplicado.
13. Rechazo de eliminación de camión EN_RUTA.

No se crearon tokens artificiales: el script cambia por SQL el rol de su propio usuario ficticio en la base separada de demostración y vuelve a iniciar sesión. No existe aún un endpoint de alta de conductores. Las ejecuciones conservan sus datos ficticios; hubo intentos iniciales detenidos durante el arranque y al descubrir la respuesta HTTP 401.

## Límites conocidos

- Flota y Rutas aún no tienen control de acceso propio. El aislamiento entre vecinos en Solicitudes se corrigió y comprobó en la mejora posterior descrita al final.
- La primera prueba detectó HTTP 401 para rol insuficiente. La corrección posterior conserva HTTP 403; también se comprobaron HTTP 400 para datos inválidos y HTTP 404 para recursos inexistentes.
- Las referencias de camión y conductor de Rutas no se verifican contra los otros servicios.
- Se probaron después los retiros fallidos, con motivo obligatorio. No se probaron concurrencia, caídas de servicios ni recuperación ante una respuesta remota perdida. La reversión local comprobada no demuestra atomicidad entre las dos bases.
- El flujo es una demostración backend local con datos ficticios; no acredita una solución lista para producción ni cobertura completa de reglas de negocio.

No se publicó la copia ni se creó un repositorio remoto. La licencia de todo el trabajo compartido debe acordarse con sus integrantes; no se añadió una automáticamente.


## Mejora de privacidad del 27/10/2026

Autenticación incluye usuarioId en el JWT firmado, obtenido del usuario almacenado al iniciar sesión. Solicitudes verifica esa firma y compara el identificador con vecinoId al crear o consultar solicitudes. Un acceso a las solicitudes de otro vecino recibe HTTP 403. Las credenciales anteriores sin usuarioId reciben HTTP 401 en Solicitudes y requieren iniciar sesión de nuevo.

Resultado del flujo ampliado: **22 comprobaciones aprobadas**. Incluye dos vecinos ficticios, rechazo de creación y consulta ajenas, consulta de la lista propia y rechazo de un JWT cuyo identificador fue alterado sin una firma válida. También terminaron los recorridos de retiro exitoso y fallido y las restricciones anteriores. Se ejecutaron además las dos pruebas contextLoads de los servicios modificados: sin fallos ni errores.

La reconstrucción normal de Docker se interrumpió por descargas muy lentas. Para esta comprobación se empaquetaron los dos servicios con Maven y las dependencias locales disponibles, y se construyeron sus imágenes usando esos JAR y eclipse-temurin:21-jre. Los Dockerfile temporales están en /tmp; no se cambió la construcción del repositorio. Esta ejecución no acredita una nueva construcción completa de Maven dentro de Docker para estos cambios.

No se modificó el README ni se publicaron estos cambios automáticamente.
