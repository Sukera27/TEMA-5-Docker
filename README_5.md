# Tema 5 · Docker — de un Dockerfile a docker-compose (API Spring Boot + MySQL)

Apuntes prácticos de la asignatura **Despliegue de Aplicaciones Web (DAW)**. Este documento recoge, en orden cronológico, todos los comandos de la sesión de Docker, **en local**: primero se construye la imagen de la API `simpsons` con un `Dockerfile`, se arranca su contenedor y se conecta al MySQL instalado en el equipo; después se hace con un `docker-compose.yml` que levanta la API y un MySQL en contenedor con un solo comando.

Los apuntes en formato web (con la teoría y las tablas de comandos) están en [`index.html`](./index.html).

## Contenido de esta carpeta

| Archivo / carpeta | Descripción |
|---|---|
| `index.html` | Apuntes del Tema 5 (qué es Docker, comandos básicos, práctica con Dockerfile, práctica con Compose) |
| `README_5.md` | Este archivo: comandos en orden cronológico |
| `ficheros-docker/` | Copia de los ficheros que se añaden a la raíz del proyecto `simpsons`: `Dockerfile`, `docker-compose.yml`, `.dockerignore` y `.env.example` |
| `img/` | Logos y favicon usados por `index.html` |

## Requisitos previos

- **Docker Desktop**, abierto y en marcha.
- **Java 17** para compilar el proyecto (se usa el `./mvnw` que ya trae; no hace falta instalar Maven). Para la práctica 1 hace falta el **MySQL instalado en el equipo**, arrancado en el puerto 3306; en la práctica 2 lo pone Docker.
- El proyecto **`simpsons`** (API Spring Boot 3.5 + MySQL) con los ficheros de `ficheros-docker/` en su raíz.

## La API de ejemplo: simpsons

Una sola tabla, `simpsons`, en MySQL. Escucha en el puerto **8080**.

| Método | Ruta | Qué hace |
|---|---|---|
| `GET` | `/api/v1/simpson` | Lista los registros (el último primero) |
| `POST` | `/api/v1/simpson` | Guarda uno nuevo (`{"name": "...", "picUrl": "..."}`) |

> **Cómo se conecta a la base de datos (y por qué no hay que tocar su código):** su `application.yaml` apunta a `jdbc:mysql://localhost:3306/simpsonsaudit` con usuario y contraseña fijos. Dentro de un contenedor, *localhost* es el propio contenedor, así que ahí no hay MySQL. Spring permite **pisar cualquier propiedad con una variable de entorno**: `SPRING_DATASOURCE_URL` sustituye a `spring.datasource.url`, y lo mismo con `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`. Además tiene `ddl-auto: update`, así que Hibernate crea la tabla solo y no hace falta ningún script SQL.

---

## 0. Comprobar que Docker funciona

```bash
docker --version
docker compose version
docker run hello-world
```

Si el último comando imprime *"Hello from Docker!"*, todo está bien. Si dice `Cannot connect to the Docker daemon`, Docker Desktop no está abierto.

Ir a la carpeta del proyecto:

```bash
cd simpsons
ls
```

Debes ver `pom.xml`, `src/`, `Dockerfile`, `docker-compose.yml` y `.env.example`.

---

# Práctica 1 · Dockerfile y MySQL local

## 1. Compilar el .jar

```bash
./mvnw clean package -DskipTests
ls target/*.jar
```

Genera `target/simpsons-0.0.1-SNAPSHOT.jar`, el programa empaquetado. `-DskipTests` evita ejecutar el test de Spring, que necesita una base de datos MySQL arrancada y todavía no la tenemos.

## 2. El Dockerfile

Está en la raíz del proyecto. Parte de una imagen con Java, copia el `.jar` y lo ejecuta.

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

| Instrucción | Qué hace |
|---|---|
| `FROM` | Imagen base sobre la que se construye. `17-jre` lleva solo el Java necesario para *ejecutar* (no compilar) |
| `WORKDIR` | Carpeta de trabajo dentro de la imagen |
| `COPY` | Copia el `.jar` del equipo a la imagen, con el nombre `app.jar` |
| `EXPOSE` | Documenta el puerto de la aplicación (no lo publica: eso lo hace `-p`) |
| `ENTRYPOINT` | Comando que se ejecuta **al arrancar** el contenedor |

Junto al Dockerfile hay un `.dockerignore` (como un `.gitignore`): dice qué no se envía a Docker al construir. Entre otras cosas deja fuera `.env`, para que los secretos nunca acaben dentro de una imagen.

## 3. Construir la imagen

```bash
docker build -t simpsons-api:1.0 .
docker images
```

`-t` le pone nombre y etiqueta; el punto final es el *contexto* (la carpeta cuyos archivos puede copiar `COPY`). En `docker images` debe aparecer `simpsons-api` con la etiqueta `1.0`.

## 4. Preparar la base de datos local

En esta práctica la API se ejecuta en un contenedor, pero la base de datos es el **MySQL instalado en el equipo**, que debe estar arrancado (puerto 3306). Solo hace falta que exista la base de datos; las tablas las crea la propia API al arrancar (`ddl-auto: update`):

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS simpsonsaudit;"
```

Si no se tiene el cliente `mysql` en la terminal, se crea la base de datos `simpsonsaudit` desde MySQL Workbench.

## 5. Arrancar el contenedor de la API

```bash
docker run -d --name simpsons-api -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/simpsonsaudit?useSSL=false&allowPublicKeyRetrieval=true" \
  simpsons-api:1.0
```

- `-d` lo ejecuta en segundo plano y `--name` le da nombre.
- `-p 8080:8080` publica el puerto: lo que llegue al 8080 del equipo va al 8080 del contenedor.
- Dentro del contenedor, `localhost` es el propio contenedor, no el equipo, y ahí no hay MySQL. Para llegar al MySQL del equipo se usa el nombre especial **`host.docker.internal`**, que Docker Desktop resuelve como "la máquina anfitriona" (en Linux hay que añadir `--add-host=host.docker.internal:host-gateway`).
- El usuario y la contraseña son los del `application.yaml` (`root` / `root`). Si el MySQL local tiene otra contraseña, añadir `-e SPRING_DATASOURCE_USERNAME=...` y `-e SPRING_DATASOURCE_PASSWORD=...`.

## 6. Ver el arranque y probar la API

```bash
docker logs -f simpsons-api
```

Cuando aparezca `Started SimpsonsApplication`, salir con `Ctrl+C` (el contenedor sigue funcionando) y probar:

```bash
curl http://localhost:8080/api/v1/simpson

curl -X POST http://localhost:8080/api/v1/simpson \
  -H "Content-Type: application/json" \
  -d '{"name":"Homer Simpson","picUrl":"https://example.com/homer.png"}'

curl http://localhost:8080/api/v1/simpson
```

Después del `POST`, el segundo `GET` devuelve a Homer. El dato está en el **MySQL local**, no dentro del contenedor, y se puede ver con:

```bash
mysql -uroot -p simpsonsaudit -e "SELECT * FROM simpsons;"
```

## 7. Gestionar el contenedor y limpiar

```bash
docker ps                  # en ejecución
docker stop simpsons-api   # parar
docker start simpsons-api  # volver a arrancar

# Limpiar antes de la práctica 2:
docker rm -f simpsons-api
```

Borrar el contenedor no borra los datos: siguen en el MySQL local. Limpiar es necesario porque la práctica 2 usa el mismo nombre de contenedor y el mismo puerto 8080.

> **Si la API se cae al arrancar**, ver el motivo con `docker logs simpsons-api`:
> - `Communications link failure` / `Connection refused`: MySQL no está arrancado en el equipo, o la URL usa `localhost` en vez de `host.docker.internal`.
> - `Access denied`: usuario o contraseña distintos de `root` / `root`.
> - `Unknown database`: falta crear `simpsonsaudit` (paso 4).
> - `Host ... is not allowed to connect`: MySQL rechaza a `root` por venir "desde otra máquina". Crear un usuario que acepte cualquier origen y pasarlo con `-e`:
>   ```sql
>   CREATE USER 'docker'@'%' IDENTIFIED BY 'docker1234';
>   GRANT ALL ON simpsonsaudit.* TO 'docker'@'%';
>   ```

> **Qué se ha conseguido y qué falta:** la API ya va dentro de un contenedor, empaquetada con su Java y lista para llevarla a cualquier sitio, pero la base de datos sigue fuera, en el equipo. En la práctica 2 se pone **también MySQL en un contenedor** y Docker Compose se encarga de levantar y conectar los dos.

---

# Práctica 2 · docker-compose

## 8. El archivo `.env`

Compose lee automáticamente el `.env` de la carpeta del proyecto. Ahí van los datos de la base de datos, para no escribirlos en el `docker-compose.yml`. Se crea copiando el de ejemplo:

```bash
cp .env.example .env
```

```
DB_USERNAME=simpsons
DB_PASSWORD=cambia_esta_clave
DB_NAME=simpsonsaudit
```

Cambiar la contraseña por la propia. `DB_USERNAME` **no puede ser** `root`: MySQL reserva ese usuario y no deja crearlo con esta variable.

## 9. El docker-compose.yml

```yaml
services:

  db:
    image: mysql:8.4
    container_name: simpsons-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME}
      MYSQL_USER: ${DB_USERNAME}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    ports:
      - "3307:3306"

  api:
    build: .
    container_name: simpsons-api
    restart: unless-stopped
    depends_on:
      - db
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
```

| Clave | Qué hace |
|---|---|
| `services` | Lista de contenedores: `db` y `api`. Compose crea una red y cada servicio se encuentra por su nombre (por eso la URL usa `db`) |
| `image` / `build` | Usar una imagen ya hecha (`mysql:8.4`) / construirla con el `Dockerfile` de esta carpeta (`.`) |
| `container_name` | Nombre fijo del contenedor (si no, Compose inventa uno) |
| `restart` | `unless-stopped`: el contenedor se reinicia solo si se cae, salvo que lo pares tú |
| `environment` | Variables de entorno (equivale a `-e`). `${DB_PASSWORD}` se rellena con el valor del `.env` |
| `ports` | Puertos publicados (equivale a `-p`). El `3307:3306` permite conectar desde Workbench u otro cliente al MySQL del contenedor sin chocar con un MySQL local en el 3306 |
| `depends_on` | Orden de arranque: la API se arranca después del contenedor de MySQL |

> **¿Y si la API arranca antes de que MySQL esté listo?** `depends_on` solo espera a que el contenedor de MySQL *arranque*, no a que la base de datos esté lista (tarda unos 20-30 segundos). Por eso la API puede fallar al principio con `Communications link failure`. No pasa nada: gracias a `restart: unless-stopped` Docker la reinicia sola hasta que MySQL responde. Es normal verla en estado *Restarting* un par de veces.

## 10. Levantar la aplicación

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f api
```

`--build` construye la imagen de la API con el `Dockerfile` y `-d` lo deja en segundo plano. Compose crea la red, arranca MySQL y después la API. Recuerda que el `.jar` de `target/` debe existir (paso 1 de la práctica 1). Esperar a que en el log aparezca `Started SimpsonsApplication` (si antes salen errores de conexión, es la API reintentando mientras MySQL arranca). Salir de los logs con `Ctrl+C`.

## 11. Probar la API

```bash
curl -X POST http://localhost:8080/api/v1/simpson \
  -H "Content-Type: application/json" \
  -d '{"name":"Marge Simpson","picUrl":"https://example.com/marge.png"}'

curl http://localhost:8080/api/v1/simpson
```

## 12. Parar la aplicación y qué pasa con los datos

Aquí los datos de MySQL viven **dentro del contenedor**. Según cómo se pare:

Parar y volver a arrancar: los datos se conservan.

```bash
docker compose stop
docker compose start
curl http://localhost:8080/api/v1/simpson
```

`stop` solo apaga los contenedores, no los borra: Marge sigue ahí.

Bajar todo: los datos se pierden.

```bash
docker compose down
docker compose up -d
curl http://localhost:8080/api/v1/simpson
```

`down` **borra los contenedores y la red**, y con ellos la base de datos. Al volver a levantar, MySQL empieza de cero y la lista sale vacía (`[]`). Hay que esperar a que la API termine de arrancar antes del `curl`.

> Para que los datos sobrevivan a un `down` habría que guardarlos fuera del contenedor con un **volumen**. En esta práctica no se usa para mantener el fichero lo más sencillo posible.

## 13. Cambiar el código y reconstruir solo la API

```bash
./mvnw clean package -DskipTests
docker compose up -d --build api
```

Primero se recompila el `.jar` (el Dockerfile solo lo copia) y después se reconstruye únicamente el servicio `api`.

> **Si cambias la contraseña del `.env`:** MySQL guarda la contraseña al crear la base de datos. Si la cambias en el `.env` y vuelves a hacer `up` sin parar antes, la API dará `Access denied`. Haz primero `docker compose down -v` y levanta de nuevo.

---

## 14. Comandos de gestión — referencia rápida

```bash
docker compose up -d --build        # construir y arrancar todo en segundo plano
docker compose ps                   # estado de los servicios
docker compose logs -f api          # log de un servicio en tiempo real
docker compose stop | start         # parar / arrancar sin borrar nada
docker compose down                 # borrar contenedores y red (los datos de MySQL se pierden)
docker compose down -v              # ... y borrar también los volúmenes asociados
docker compose up -d --build api    # reconstruir y reiniciar solo la API

docker build -t nombre:etiqueta .   # construir una imagen
docker images                       # imágenes del equipo
docker ps -a                        # todos los contenedores
docker logs -f nombre               # log de un contenedor
docker exec -it nombre comando      # ejecutar un comando dentro de un contenedor
docker rm -f nombre                 # borrar un contenedor
```

## Solución de problemas habituales

- **`Cannot connect to the Docker daemon`**: Docker Desktop no está abierto; abrirlo y esperar a que indique que está en marcha.
- **`COPY failed: no source files` en el build**: no existe el `.jar` en `target/`. Ejecutar `./mvnw clean package -DskipTests`.
- **`port is already allocated` / `Bind for 0.0.0.0:8080 failed`**: otro programa o contenedor usa el 8080 (la API en local, o el contenedor de la práctica 1). Pararlo con `docker ps` y `docker rm -f`, o cerrar la API local.
- **`container name ... is already in use`**: queda el contenedor de la práctica 1. `docker rm -f simpsons-api`.
- **La API da `Communications link failure`**: En Compose, usar el nombre del servicio (`db`); se arregla solo gracias a `restart`. En la práctica 1, comprobar que MySQL está arrancado en el equipo y que la URL usa `host.docker.internal`.
- **`Access denied for user`**: la contraseña del `.env` no es la que MySQL guardó al crear la base de datos. `docker compose down -v` y volver a levantar.
- **`variable is not set` / MySQL no arranca**: falta el `.env` o alguna variable. `cp .env.example .env`.

---

**Idea clave:** una **imagen** es la aplicación empaquetada y un **contenedor** es esa imagen en ejecución. El `Dockerfile` describe cómo construir la imagen y `docker-compose.yml` cómo se ejecutan juntos varios contenedores. La misma imagen funciona en tu ordenador y en cualquier servidor: solo cambia la configuración que le pasas.

**Próxima sesión:** llevaremos esta misma idea a Azure, con una arquitectura más completa (front, API y base de datos).

---

Los temas siguientes se enlazan desde el menú lateral de `index.html`.
