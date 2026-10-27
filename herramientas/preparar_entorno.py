"""Genera secretos locales nuevos sin mostrarlos ni sobrescribir un .env existente."""
import base64
import os
from pathlib import Path
import secrets


def preparar(raiz):
    archivo = Path(raiz) / '.env'
    datos = ('MYSQL_ROOT_PASSWORD=' + secrets.token_urlsafe(32) + '\n'
             'JWT_SECRET=' + base64.b64encode(secrets.token_bytes(32)).decode('ascii') + '\n')
    fd = os.open(archivo, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(fd, 'w', encoding='utf-8') as stream:
        stream.write(datos)
    return archivo


if __name__ == '__main__':
    try:
        preparar(Path(__file__).resolve().parents[1])
    except FileExistsError:
        print('Ya existe .env. Se conserva sin cambios.')
    else:
        print('Archivo .env creado con valores locales nuevos. No lo subas a GitHub.')
