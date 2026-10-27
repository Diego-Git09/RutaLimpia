"""Prueba local con datos ficticios. Requiere el Compose de portafolio encendido.

Crea registros nuevos en cada ejecución; conserva los datos para inspeccionarlos.
Promueve exclusivamente su usuario ficticio a CONDUCTOR mediante SQL porque
no existe un endpoint de alta de conductores. Las contraseñas y JWT no se imprimen.
"""
import json
from pathlib import Path
import secrets
import subprocess
import sys
import time
import urllib.error
import urllib.request
from datetime import date, timedelta

RAIZ = Path(__file__).resolve().parents[1]
COMPROBACIONES = []


def verificar(condicion, mensaje):
    if not condicion:
        raise RuntimeError(mensaje)
    COMPROBACIONES.append(mensaje)
    print('OK · ' + mensaje, flush=True)


def pedir(puerto, metodo, ruta, datos=None, token=None, esperado=200):
    contenido = None if datos is None else json.dumps(datos).encode()
    cabeceras = {'Content-Type': 'application/json'}
    if token:
        cabeceras['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request(f'http://127.0.0.1:{puerto}{ruta}',
                                 contenido, cabeceras, method=metodo)
    try:
        respuesta = urllib.request.urlopen(req, timeout=25)
    except urllib.error.HTTPError as exc:
        respuesta = exc
    with respuesta:
        estado = respuesta.code
        cuerpo = respuesta.read()
    if estado != esperado:
        raise RuntimeError(f'{metodo} {ruta}: HTTP {estado}; se esperaba {esperado}')
    return json.loads(cuerpo) if cuerpo else None


def esperar_servicios():
    print('Esperando que arranquen los cuatro servicios...', flush=True)
    for puerto, ruta in [(8085, '/api/camiones'), (8087, '/api/rutas'),
                         (8086, '/api/usuarios/perfil'),
                         (8084, '/api/solicitudes/tipos-residuo')]:
        limite = time.monotonic() + 120
        while True:
            try:
                with urllib.request.urlopen(f'http://127.0.0.1:{puerto}{ruta}', timeout=3):
                    break
            except urllib.error.HTTPError as exc:
                if exc.code == 401:
                    break
            except OSError:
                pass
            if time.monotonic() >= limite:
                raise RuntimeError(f'El servicio del puerto {puerto} no está listo')
            time.sleep(2)


def ejecutar():
    esperar_servicios()
    sufijo = secrets.token_hex(5)
    clave = secrets.token_urlsafe(24)
    vecino = {'nombre': 'Vecino', 'apellido': 'Demo', 'rut': 'D' + sufijo,
              'email': f'vecino-{sufijo}@example.invalid', 'telefono': '000000000',
              'contrasena': clave}
    creado = pedir(8086, 'POST', '/api/usuarios/vecinos', vecino, esperado=201)
    pedir(8086, 'POST', '/api/usuarios/vecinos', vecino, esperado=409)
    verificar(creado['rol'] == 'VECINO', 'Alta de vecino y rechazo de correo duplicado')
    token = pedir(8086, 'POST', '/api/auth/login',
                  {'email': vecino['email'], 'contrasena': clave})['token']
    perfil = pedir(8086, 'GET', '/api/usuarios/perfil', token=token)
    verificar(perfil['id'] == creado['id'], 'Inicio de sesión y consulta del perfil')
    pedir(8086, 'GET', '/api/usuarios/perfil', esperado=401)
    pedir(8084, 'GET', '/api/solicitudes/tipos-residuo', esperado=401)
    verificar(True, 'Perfil y solicitudes rechazan peticiones sin token')
    tipos = pedir(8084, 'GET', '/api/solicitudes/tipos-residuo', token=token)
    tipo_id = tipos[0].get('id', tipos[0].get('idTipoResiduo'))
    if tipo_id is None:
        raise RuntimeError('No se reconoce el identificador del tipo de residuo')
    cuerpo = {'vecinoId': str(creado['id']), 'direccion': 'Calle Ficticia 123',
              'tipoResiduoId': tipo_id,
              'horarioPreferido': str(date.today() + timedelta(days=1)) + 'T10:00:00',
              'cantidadKg': 10}
    solicitud = pedir(8084, 'POST', '/api/solicitudes', cuerpo, token, 201)
    verificar(solicitud['estado'] == 'PENDIENTE', 'Solicitud creada en estado PENDIENTE')
    folio = solicitud['folio']
    pedir(8084, 'POST', f'/api/solicitudes/{folio}/asignacion', token=token, esperado=401)
    verificar(True, 'Un vecino no puede asignar retiros (respuesta actual: HTTP 401)')
    camion = pedir(8085, 'POST', '/api/camiones',
                   {'patente': 'DEMO-' + sufijo, 'capacidad': 1000, 'estado': 'DISPONIBLE'},
                   esperado=201)
    cid = camion['idCamion']
    pedir(8085, 'POST', '/api/camiones',
          {'patente': camion['patente'], 'capacidad': 1000, 'estado': 'DISPONIBLE'}, esperado=409)
    pedir(8085, 'POST', '/api/camiones',
          {'patente': 'INVALIDA', 'capacidad': -1, 'estado': 'DISPONIBLE'}, esperado=400)
    verificar(True, 'Flota registra camión y rechaza patente duplicada y capacidad negativa')
    ruta_datos = {'fecha': str(date.today() + timedelta(days=1)),
                  'idCamion': cid, 'idConductor': creado['id']}
    ruta = pedir(8087, 'POST', '/api/rutas', ruta_datos, esperado=201)
    rid = ruta['idRuta']
    pedir(8087, 'POST', '/api/rutas', ruta_datos, esperado=409)
    verificar(True, 'Ruta creada; se impide repetir camión y fecha')
    # Solo cambia el rol del usuario creado por ESTA ejecución en el proyecto aislado.
    sql = ("UPDATE usuario SET id_rol=(SELECT id_rol FROM rol WHERE nombre_rol='CONDUCTOR') "
           f"WHERE email='{vecino['email']}';\n")
    subprocess.run(['docker', 'compose', '-p', 'rutalimpia-portafolio', 'exec', '-T',
                    'mysql-autenticacion', 'sh', '-c',
                    'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot rutalimpia_autenticacion_db'],
                   input=sql, text=True, cwd=RAIZ, check=True, stdout=subprocess.DEVNULL)
    conductor = pedir(8086, 'POST', '/api/auth/login',
                      {'email': vecino['email'], 'contrasena': clave})['token']
    parada_datos = {'folio': folio, 'direccion': cuerpo['direccion'], 'orden': 1}
    pedir(8087, 'POST', f'/api/rutas/{rid}/paradas', parada_datos, token, 401)
    verificar(pedir(8087, 'GET', f'/api/rutas/{rid}/paradas') == [],
              'Asignación con rol incorrecto revierte la parada local')
    parada = pedir(8087, 'POST', f'/api/rutas/{rid}/paradas', parada_datos, conductor, 201)
    listado = pedir(8084, 'GET', f"/api/solicitudes/vecino/{creado['id']}", token=token)
    verificar(next(x for x in listado if x['folio'] == folio)['estado'] == 'ASIGNADA',
              'Rutas comunica la asignación a Solicitudes mediante OpenFeign')
    pedir(8087, 'DELETE', f'/api/rutas/{rid}', esperado=409)
    verificar(True, 'Se impide eliminar una ruta que tiene paradas')
    resultado_url = f"/api/rutas/{rid}/paradas/{parada['idParada']}/resultado"
    final = pedir(8087, 'PATCH', resultado_url,
                  {'estado': 'EXITOSO', 'observacion': 'Retiro ficticio de prueba'}, conductor)
    listado = pedir(8084, 'GET', f"/api/solicitudes/vecino/{creado['id']}", token=token)
    verificar(final['estado'] == 'EXITOSO' and
              next(x for x in listado if x['folio'] == folio)['estado'] == 'REALIZADA',
              'Retiro EXITOSO actualiza la solicitud a REALIZADA')
    pedir(8087, 'PATCH', resultado_url, {'estado': 'EXITOSO'}, conductor, 409)
    verificar(True, 'Se rechaza registrar dos veces el resultado')
    pedir(8085, 'PATCH', f'/api/camiones/{cid}/estado', {'estado': 'EN_RUTA'})
    pedir(8085, 'DELETE', f'/api/camiones/{cid}', esperado=409)
    verificar(True, 'Se impide eliminar un camión EN_RUTA')
    print(f'\n{len(COMPROBACIONES)} comprobaciones aprobadas. Datos ficticios conservados.')
    print(f'Camión: {cid} · Ruta: {rid} · Folio: {folio}')


if __name__ == '__main__':
    try:
        ejecutar()
    except (RuntimeError, OSError, subprocess.SubprocessError) as exc:
        print(f'PRUEBA DETENIDA: {exc}', file=sys.stderr)
        sys.exit(1)
