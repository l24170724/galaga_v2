# Galaga en Java

Juego de naves en 2D inspirado en el clásico **Galaga**, hecho en **Java** con **Swing**.
Controla tu nave, destruye oleadas de enemigos a lo largo de **9 niveles** y derrota al **jefe final** en el nivel 10.

![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)
![Plataforma](https://img.shields.io/badge/Plataforma-Windows%20%7C%20macOS%20%7C%20Linux-blue)
![Estado](https://img.shields.io/badge/Estado-Terminado-brightgreen)

---

## Características

- **Pantalla inicial** con los botones *Iniciar partida*, *Instrucciones* y *Salir*
- **Pantalla de instrucciones** con el objetivo, los controles y las reglas
- **9 niveles** con más enemigos, más velocidad y más disparos en cada uno
- **Jefe final** en el nivel 10 con barra de vida
- **Botones de Pausa, Reiniciar nivel y Menú** en la esquina superior derecha
- **Récord guardado automáticamente** en un archivo `.txt`, que se conserva al cerrar el juego
- **Logo personalizado** en la ventana del juego
- Atajos de teclado para todas las acciones

---

## Requisitos

| Componente | Mínimo | Recomendado |
|---|---|---|
| Sistema operativo | Windows 10 de 64 bits (también macOS o Linux) | Windows 10 u 11 de 64 bits |
| Procesador | Doble núcleo de 64 bits, 1.5 GHz | Doble núcleo, 2 GHz o más |
| Memoria RAM | 2 GB | 4 GB (si usas VS Code) |
| Espacio en disco | 400 MB (Java) | 1.5 GB (Java + VS Code) |
| Pantalla | 1024 × 768 | 1366 × 768 o mayor |
| Software | **Java JDK 17 o superior** | JDK 17 (Temurin) + VS Code |

Puedes descargar Java gratis desde [adoptium.net](https://adoptium.net). Elige la versión LTS e instálala con las opciones por defecto.

Para comprobar que Java está instalado, abre una terminal y escribe:

```bash
java -version
```

---

## Cómo ejecutar el juego

### Forma 1: desde el CMD (símbolo del sistema)

1. Presiona la tecla **Windows**, escribe `cmd` y presiona **Enter**.
2. Ve a la carpeta donde está el juego (por ejemplo, `C:\Galaga`):
   ```bash
   cd C:\Galaga
   ```
3. Compila el código (solo la primera vez o después de modificarlo):
   ```bash
   javac Galaga.java
   ```
4. Ejecuta el juego:
   ```bash
   java Galaga
   ```

### Forma 2: desde Visual Studio Code

1. Abre VS Code y ve a **Archivo → Abrir carpeta**. Elige la carpeta del juego.
2. Instala la extensión **Extension Pack for Java** de Microsoft (solo la primera vez).
3. Abre el archivo `Galaga.java`.
4. Espera a que desaparezca el mensaje *"Java: Activating..."* de la barra inferior.
5. Presiona el botón **(Run)** en la esquina superior derecha.

---

## Controles

| Tecla | Botón en pantalla | Acción |
|:---:|:---:|---|
| `←` `→` | — | Mover la nave |
| `Espacio` | — | Disparar |
| `P` | Pausa / Continuar | Pausar o reanudar el juego |
| `R` | Reiniciar | Reiniciar el nivel actual |
| `Esc` | Menú | Volver a la pantalla inicial |
| `Enter` | — | Jugar de nuevo (después de Game Over o Victoria) |

**En la pantalla inicial:**

| Tecla | Botón | Acción |
|:---:|:---:|---|
| `Enter` | Iniciar partida | Comenzar una partida nueva |
| `I` | Instrucciones | Ver las instrucciones |
| `Esc` | Salir | Guardar el récord y cerrar el juego |

---

## Cómo se juega

### Objetivo
Destruye a todos los enemigos de cada nivel para avanzar. Después del nivel 9 aparece el **jefe final**. Derrótalo para ganar.

### Tu nave
- Empiezas con **3 vidas**.
- Pierdes una vida si te alcanza una bala enemiga (naranja).
- Pierdes la partida si te quedas sin vidas o si los enemigos bajan demasiado.

### Niveles

| Nivel | Formación | Enemigos | Impactos por enemigo |
|:---:|:---:|:---:|:---:|
| 1 | 2 × 5 | 10 | 1 |
| 2 – 3 | 3 × 6 | 18 | 1 |
| 4 – 5 | 4 × 7 | 28 | 1 |
| 6 | 5 × 8 | 40 | 1 |
| 7 | 5 × 8 | 40 | **2** |
| 8 – 9 | 5 × 9 | 45 | **2** |
| 10 | Jefe final | 1 | **50** |

En cada nivel los enemigos se mueven más rápido y disparan más seguido.

### Jefe final
- Se mueve rápido de lado a lado y dispara con frecuencia.
- Tiene **100 de vida**. Cada disparo tuyo le quita 2.
- Su barra de vida aparece en la parte inferior de la pantalla.

### Puntuación

| Acción | Puntos |
|---|:---:|
| Destruir un enemigo | 100 |
| Derrotar al jefe final | 5,000 |

La puntuación máxima posible es **32,200 puntos**.

### Récord
La puntuación más alta se guarda en `puntuacion_maxima.txt`. Aparece en la pantalla inicial y en la barra superior durante la partida. Si lo superas, verás **¡NUEVO RÉCORD!** al terminar.

---

## Archivos del proyecto

```
galaga_v2/
├── Galaga.java              ← Código fuente del juego (único archivo necesario)
└── README.md                ← Este archivo
```

Archivos que se generan solos al compilar o jugar (no hace falta subirlos):

| Archivo | Se genera cuando… |
|---|---|
| `Galaga.class` y `Galaga$*.class` | Compilas el juego |
| `puntuacion_maxima.txt` | Termina una partida, vuelves al menú o cierras el juego |

---

## Autores

| Nombre | Número de control |
|---|---|
| Carrera Aguirre Joel | 23211930 |
| Ruan López Tai Ping Alejandra | 24170724 |
| Vazquez Medrano Osvaldo | 23210679 |
