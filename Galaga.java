import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Galaga extends JPanel implements ActionListener, KeyListener {

    // =========================
    // VENTANA
    // =========================
    static final int ANCHO = 800;
    static final int ALTO = 700;
    static final int ALTO_BARRA = 45;

    // =========================
    // ARCHIVOS
    // =========================
    // Se guardan en la misma carpeta desde donde se ejecuta el juego
    static final String ARCHIVO_RECORD = "puntuacion_maxima.txt";
    static final String ARCHIVO_LOGO = "logo.png"; // opcional

    // Nota: los acentos en los textos se escriben como \u00e1, \u00e9, \u00ed, \u00f3, \u00fa,
    // \u00c9 (E con acento) y \u00a1 (signo de apertura) para que se vean bien en cualquier computadora.

    Timer timer;
    Random random = new Random();
    Image logo;

    // =========================
    // PANTALLAS
    // =========================
    enum Estado { MENU, INSTRUCCIONES, JUGANDO }

    Estado estado = Estado.MENU;
    boolean pausa = false;

    Point posicionRaton = null;

    // =========================
    // BOTONES
    // =========================
    // Pantalla inicial
    Boton btnIniciar = new Boton("Iniciar partida", 275, 400, 250, 50, this::iniciarPartida);
    Boton btnInstrucciones = new Boton("Instrucciones", 275, 470, 250, 50, () -> estado = Estado.INSTRUCCIONES);
    Boton btnSalir = new Boton("Salir", 275, 540, 250, 50, this::salir);

    // Pantalla de instrucciones
    Boton btnVolver = new Boton("Volver", 325, 615, 150, 45, () -> estado = Estado.MENU);

    // Durante la partida (esquina superior derecha)
    Boton btnPausa = new Boton("Pausa", 519, 8, 85, 29, this::alternarPausa);
    Boton btnReiniciar = new Boton("Reiniciar", 612, 8, 85, 29, this::reiniciarNivel);
    Boton btnMenu = new Boton("Men\u00fa", 705, 8, 85, 29, this::irAlMenu);

    // =========================
    // JUGADOR
    // =========================
    int jugadorX = 375;
    int jugadorY = 620;

    final int jugadorAncho = 50;
    final int jugadorAlto = 35;
    final int velocidadJugador = 7;

    boolean izquierda = false;
    boolean derecha = false;

    // =========================
    // JUEGO
    // =========================
    int nivel = 1;
    int puntuacion = 0;
    int vidas = 3;

    boolean gameOver = false;
    boolean victoria = false;

    int tiempoNivel = 120;
    int cooldownDisparo = 0;

    // Valores al empezar el nivel (para el boton Reiniciar)
    int puntuacionInicioNivel = 0;
    int vidasInicioNivel = 3;

    // =========================
    // PUNTUACION MAXIMA
    // =========================
    int record = 0;
    int recordAlIniciar = 0;

    // =========================
    // ENEMIGOS
    // =========================
    ArrayList<Enemigo> enemigos = new ArrayList<>();
    ArrayList<Bala> balasJugador = new ArrayList<>();
    ArrayList<Bala> balasEnemigos = new ArrayList<>();

    int direccionEnemigos = 1;
    int contadorMovimiento = 0;

    // =========================
    // JEFE
    // =========================
    boolean jefeActivo = false;

    int jefeX = 300;
    int jefeY = 70;

    int jefeAncho = 200;
    int jefeAlto = 100;

    int jefeVida = 100;
    int jefeVidaMaxima = 100;

    int direccionJefe = 1;

    // =========================
    // CONSTRUCTOR
    // =========================
    public Galaga() {

        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(this);

        // Raton para los botones
        MouseAdapter raton = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                clicEn(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                posicionRaton = e.getPoint();
                actualizarCursor();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                posicionRaton = null;
                actualizarCursor();
            }
        };

        addMouseListener(raton);
        addMouseMotionListener(raton);

        logo = cargarLogo();
        record = leerRecord();

        timer = new Timer(16, this);
        timer.start();
    }

    // =========================
    // LOGO
    // =========================
    // Si hay un archivo logo.png en la carpeta, se usa ese.
    // Si no, se dibuja uno con la nave del juego.
    Image cargarLogo() {

        File archivo = new File(ARCHIVO_LOGO);

        if (archivo.exists()) {
            try {
                Image imagen = ImageIO.read(archivo);
                if (imagen != null)
                    return imagen;
            } catch (IOException e) {
                System.out.println("No se pudo leer " + ARCHIVO_LOGO + ", se usa el logo por defecto.");
            }
        }

        return crearLogoPorDefecto();
    }

    Image crearLogoPorDefecto() {

        int t = 64;

        BufferedImage imagen = new BufferedImage(t, t, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo
        g.setColor(new Color(15, 15, 45));
        g.fillRoundRect(0, 0, t, t, 16, 16);

        // Nave
        g.setColor(Color.CYAN);
        g.fillPolygon(new int[]{32, 10, 54}, new int[]{8, 48, 48}, 3);

        // Cabina
        g.setColor(Color.WHITE);
        g.fillOval(26, 24, 12, 12);

        // Motores
        g.setColor(Color.ORANGE);
        g.fillRect(18, 48, 8, 8);
        g.fillRect(38, 48, 8, 8);

        g.dispose();

        return imagen;
    }

    // =========================
    // ARCHIVO DE RECORD
    // =========================
    int leerRecord() {

        Path ruta = Paths.get(ARCHIVO_RECORD);

        if (!Files.exists(ruta))
            return 0;

        try {
            String texto = new String(Files.readAllBytes(ruta), StandardCharsets.UTF_8).trim();
            return Integer.parseInt(texto);
        } catch (IOException | NumberFormatException e) {
            System.out.println("No se pudo leer " + ARCHIVO_RECORD + ", el r\u00e9cord empieza en 0.");
            return 0;
        }
    }

    void guardarRecord() {

        try {
            Files.write(
                    Paths.get(ARCHIVO_RECORD),
                    String.valueOf(record).getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            System.out.println("No se pudo guardar " + ARCHIVO_RECORD);
        }
    }

    void sumarPuntos(int puntos) {

        puntuacion += puntos;

        if (puntuacion > record)
            record = puntuacion;
    }

    // =========================
    // CAMBIOS DE PANTALLA
    // =========================
    void iniciarPartida() {

        estado = Estado.JUGANDO;
        pausa = false;
        btnPausa.texto = "Pausa";

        nivel = 1;
        puntuacion = 0;
        vidas = 3;

        jugadorX = 375;
        izquierda = false;
        derecha = false;

        gameOver = false;
        victoria = false;

        recordAlIniciar = record;

        crearNivel();
    }

    void irAlMenu() {

        guardarRecord();

        estado = Estado.MENU;
        pausa = false;
        btnPausa.texto = "Pausa";

        izquierda = false;
        derecha = false;
    }

    // Guarda el record y cierra la aplicacion
    void salir() {

        guardarRecord();

        System.exit(0);
    }

    void alternarPausa() {

        if (estado != Estado.JUGANDO || gameOver || victoria)
            return;

        pausa = !pausa;
        btnPausa.texto = pausa ? "Continuar" : "Pausa";

        izquierda = false;
        derecha = false;
    }

    // Vuelve a empezar el nivel actual con los puntos
    // y las vidas que tenias al comenzarlo
    void reiniciarNivel() {

        if (estado != Estado.JUGANDO || gameOver || victoria)
            return;

        guardarRecord();

        puntuacion = puntuacionInicioNivel;
        vidas = vidasInicioNivel;

        jugadorX = 375;
        izquierda = false;
        derecha = false;

        pausa = false;
        btnPausa.texto = "Pausa";

        crearNivel();
    }

    void terminarPartida(boolean gano) {

        if (gameOver || victoria)
            return;

        if (gano)
            victoria = true;
        else
            gameOver = true;

        guardarRecord();
    }

    // =========================
    // CREAR NIVEL
    // =========================
    void crearNivel() {

        enemigos.clear();
        balasJugador.clear();
        balasEnemigos.clear();

        puntuacionInicioNivel = puntuacion;
        vidasInicioNivel = vidas;

        direccionEnemigos = 1;
        contadorMovimiento = 0;
        cooldownDisparo = 0;

        jefeActivo = false;

        // NIVEL 10 = JEFE
        if (nivel == 10) {

            jefeActivo = true;

            jefeX = 300;
            jefeY = 70;
            direccionJefe = 1;

            jefeVida = jefeVidaMaxima;

            tiempoNivel = 0;

            return;
        }

        // Cantidad de enemigos aumenta con cada nivel
        int filas = 2 + nivel / 2;
        int columnas = 5 + nivel / 2;

        if (filas > 5)
            filas = 5;

        if (columnas > 9)
            columnas = 9;

        for (int fila = 0; fila < filas; fila++) {

            for (int columna = 0; columna < columnas; columna++) {

                int x = 100 + columna * 70;
                int y = 80 + fila * 50;

                int vida = 1;

                // Desde nivel 7 los enemigos tienen 2 vidas
                if (nivel >= 7)
                    vida = 2;

                enemigos.add(new Enemigo(x, y, vida));
            }
        }

        tiempoNivel = 120;
    }

    // =========================
    // ACTUALIZAR JUEGO
    // =========================
    void actualizar() {

        if (estado != Estado.JUGANDO || pausa || gameOver || victoria)
            return;

        if (tiempoNivel > 0)
            tiempoNivel--;

        // Movimiento del jugador
        if (izquierda)
            jugadorX -= velocidadJugador;

        if (derecha)
            jugadorX += velocidadJugador;

        // Limitar jugador
        if (jugadorX < 10)
            jugadorX = 10;

        if (jugadorX > ANCHO - jugadorAncho - 10)
            jugadorX = ANCHO - jugadorAncho - 10;

        if (cooldownDisparo > 0)
            cooldownDisparo--;

        // =========================
        // MOVIMIENTO ENEMIGOS
        // =========================

        contadorMovimiento++;

        int velocidadEnemigos = 30 - nivel * 2;

        if (velocidadEnemigos < 8)
            velocidadEnemigos = 8;

        if (contadorMovimiento >= velocidadEnemigos) {

            contadorMovimiento = 0;

            boolean cambiarDireccion = false;

            for (Enemigo enemigo : enemigos) {

                enemigo.x += direccionEnemigos * 10;

                if (enemigo.x <= 20 ||
                    enemigo.x >= ANCHO - enemigo.ancho - 20) {

                    cambiarDireccion = true;
                }
            }

            if (cambiarDireccion) {

                direccionEnemigos *= -1;

                for (Enemigo enemigo : enemigos)
                    enemigo.y += 15;
            }
        }

        // =========================
        // ACTUALIZAR BALAS JUGADOR
        // =========================

        Iterator<Bala> itBalas = balasJugador.iterator();

        while (itBalas.hasNext()) {

            Bala bala = itBalas.next();

            bala.y += bala.velocidad;

            if (bala.y < 0) {

                itBalas.remove();
                continue;
            }

            // Golpear enemigos
            boolean balaEliminada = false;

            Iterator<Enemigo> itEnemigos = enemigos.iterator();

            while (itEnemigos.hasNext()) {

                Enemigo enemigo = itEnemigos.next();

                if (bala.getRectangulo().intersects(
                        enemigo.getRectangulo())) {

                    enemigo.vida--;

                    if (enemigo.vida <= 0) {

                        itEnemigos.remove();

                        sumarPuntos(100);
                    }

                    balaEliminada = true;
                    break;
                }
            }

            if (balaEliminada) {

                itBalas.remove();
                continue;
            }

            // Golpear jefe
            if (jefeActivo) {

                if (bala.getRectangulo().intersects(
                        getRectanguloJefe())) {

                    jefeVida -= 2;

                    itBalas.remove();

                    if (jefeVida <= 0) {

                        jefeVida = 0;

                        sumarPuntos(5000);

                        terminarPartida(true);
                    }
                }
            }
        }

        // =========================
        // BALAS ENEMIGAS
        // =========================

        Iterator<Bala> itBalasEnemigos =
                balasEnemigos.iterator();

        while (itBalasEnemigos.hasNext()) {

            Bala bala = itBalasEnemigos.next();

            bala.y += bala.velocidad;

            if (bala.y > ALTO) {

                itBalasEnemigos.remove();
                continue;
            }

            if (bala.getRectangulo().intersects(
                    getRectanguloJugador())) {

                perderVida();
                break; // perderVida() borra todas las balas enemigas
            }
        }

        // =========================
        // DISPAROS ENEMIGOS
        // =========================

        if (!enemigos.isEmpty()) {

            int probabilidad = 60 - nivel * 3;

            if (probabilidad < 15)
                probabilidad = 15;

            if (random.nextInt(probabilidad) == 0) {

                Enemigo enemigo =
                        enemigos.get(
                                random.nextInt(enemigos.size())
                        );

                balasEnemigos.add(
                        new Bala(
                                enemigo.x + enemigo.ancho / 2,
                                enemigo.y + enemigo.alto,
                                6
                        )
                );
            }
        }

        // =========================
        // JEFE
        // =========================

        if (jefeActivo && !victoria) {

            jefeX += direccionJefe * 4;

            if (jefeX <= 20 ||
                jefeX + jefeAncho >= ANCHO - 20) {

                direccionJefe *= -1;
            }

            // El jefe dispara
            if (random.nextInt(25) == 0) {

                balasEnemigos.add(
                        new Bala(
                                jefeX + jefeAncho / 2,
                                jefeY + jefeAlto,
                                7
                        )
                );
            }
        }

        // =========================
        // COLISIONES ENEMIGO-JUGADOR
        // =========================

        for (Enemigo enemigo : enemigos) {

            if (enemigo.getRectangulo().intersects(
                    getRectanguloJugador())) {

                perderVida();
                break;
            }

            // Si los enemigos llegan abajo
            if (enemigo.y > 570) {

                terminarPartida(false);
                break;
            }
        }

        // =========================
        // SIGUIENTE NIVEL
        // =========================

        if (enemigos.isEmpty() &&
            nivel < 10 &&
            !jefeActivo &&
            !gameOver) {

            nivel++;

            crearNivel();
        }
    }

    // =========================
    // PERDER VIDA
    // =========================
    void perderVida() {

        vidas--;

        jugadorX = 375;

        balasEnemigos.clear();

        if (vidas <= 0) {

            vidas = 0;

            terminarPartida(false);
        }
    }

    // =========================
    // DISPARAR
    // =========================
    void disparar() {

        if (cooldownDisparo == 0 &&
            !gameOver &&
            !victoria) {

            balasJugador.add(
                    new Bala(
                            jugadorX + jugadorAncho / 2 - 3,
                            jugadorY - 15,
                            -10
                    )
            );

            cooldownDisparo = 10;
        }
    }

    // =========================
    // RECTANGULO JUGADOR
    // =========================
    Rectangle getRectanguloJugador() {

        return new Rectangle(
                jugadorX,
                jugadorY,
                jugadorAncho,
                jugadorAlto + 10
        );
    }

    // =========================
    // RECTANGULO JEFE
    // =========================
    Rectangle getRectanguloJefe() {

        return new Rectangle(
                jefeX,
                jefeY,
                jefeAncho,
                jefeAlto
        );
    }

    // =========================
    // BOTONES VISIBLES
    // =========================
    Boton[] botonesVisibles() {

        switch (estado) {

            case MENU:
                return new Boton[]{btnIniciar, btnInstrucciones, btnSalir};

            case INSTRUCCIONES:
                return new Boton[]{btnVolver};

            default:
                if (gameOver || victoria)
                    return new Boton[]{btnMenu};

                return new Boton[]{btnPausa, btnReiniciar, btnMenu};
        }
    }

    void clicEn(Point punto) {

        requestFocusInWindow();

        for (Boton boton : botonesVisibles()) {

            if (boton.contiene(punto)) {

                boton.accion.run();
                break;
            }
        }

        actualizarCursor();
    }

    void actualizarCursor() {

        boolean sobreBoton = false;

        for (Boton boton : botonesVisibles())
            if (boton.contiene(posicionRaton))
                sobreBoton = true;

        setCursor(Cursor.getPredefinedCursor(
                sobreBoton ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    // =========================
    // DIBUJAR
    // =========================
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        dibujarEstrellas(g2);

        switch (estado) {

            case MENU:
                dibujarMenu(g2);
                break;

            case INSTRUCCIONES:
                dibujarInstrucciones(g2);
                break;

            case JUGANDO:
                dibujarJuego(g2);
                break;
        }
    }

    void dibujarEstrellas(Graphics2D g2) {

        g2.setColor(Color.WHITE);

        for (int i = 0; i < 100; i++) {

            int x = (i * 97) % ANCHO;
            int y = (i * 53) % ALTO;

            g2.fillRect(x, y, 2, 2);
        }
    }

    void textoCentrado(Graphics2D g2, String texto, int y) {

        int ancho = g2.getFontMetrics().stringWidth(texto);

        g2.drawString(texto, (ANCHO - ancho) / 2, y);
    }

    // =========================
    // PANTALLA INICIAL
    // =========================
    void dibujarMenu(Graphics2D g2) {

        // Logo
        g2.drawImage(logo, (ANCHO - 120) / 2, 90, 120, 120, null);

        // Titulo
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.BOLD, 72));
        textoCentrado(g2, "GALAGA", 290);

        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        textoCentrado(g2, "10 niveles y un jefe final", 325);

        // Record
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        textoCentrado(g2, "R\u00c9CORD: " + record, 370);

        // Botones
        btnIniciar.dibujar(g2);
        btnInstrucciones.dibujar(g2);
        btnSalir.dibujar(g2);

        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        textoCentrado(g2, "Enter: iniciar partida    I: instrucciones    Esc: salir", 660);
    }

    // =========================
    // PANTALLA DE INSTRUCCIONES
    // =========================
    void dibujarInstrucciones(Graphics2D g2) {

        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        textoCentrado(g2, "INSTRUCCIONES", 75);

        int y = 125;

        y = tituloSeccion(g2, "OBJETIVO", y);
        y = lineaTexto(g2, "Destruye a todos los enemigos para pasar de nivel. Son 9 niveles", y);
        y = lineaTexto(g2, "y en el nivel 10 te espera el jefe final. \u00a1Derr\u00f3talo para ganar!", y);

        y += 30;
        y = tituloSeccion(g2, "CONTROLES", y);
        y = lineaControl(g2, "Flechas \u2190 \u2192", "Mover la nave a la izquierda o a la derecha", y);
        y = lineaControl(g2, "Espacio", "Disparar", y);
        y = lineaControl(g2, "P", "Pausar / continuar", y);
        y = lineaControl(g2, "R", "Reiniciar el nivel actual", y);
        y = lineaControl(g2, "Esc", "Volver a la pantalla inicial", y);
        y = lineaControl(g2, "Enter", "Jugar de nuevo (despu\u00e9s de Game Over o Victoria)", y);

        y += 30;
        y = tituloSeccion(g2, "REGLAS", y);
        y = lineaTexto(g2, "- Empiezas con 3 vidas; pierdes una si te alcanza una bala enemiga.", y);
        y = lineaTexto(g2, "- Si los enemigos bajan demasiado, pierdes la partida.", y);
        y = lineaTexto(g2, "- Desde el nivel 7 los enemigos aguantan 2 disparos.", y);
        y = lineaTexto(g2, "- El jefe final necesita 50 impactos.", y);
        lineaTexto(g2, "- Cada enemigo vale 100 puntos y el jefe 5,000.", y);

        btnVolver.dibujar(g2);
    }

    int tituloSeccion(Graphics2D g2, String texto, int y) {

        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString(texto, 80, y);

        return y + 28;
    }

    int lineaTexto(Graphics2D g2, String texto, int y) {

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 17));
        g2.drawString(texto, 100, y);

        return y + 24;
    }

    int lineaControl(Graphics2D g2, String tecla, String descripcion, int y) {

        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.BOLD, 17));
        g2.drawString(tecla, 100, y);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 17));
        g2.drawString(descripcion, 240, y);

        return y + 24;
    }

    // =========================
    // PANTALLA DE JUEGO
    // =========================
    void dibujarJuego(Graphics2D g2) {

        // Jugador
        dibujarJugador(g2);

        // Enemigos
        for (Enemigo enemigo : enemigos)
            enemigo.dibujar(g2);

        // Balas
        for (Bala bala : balasJugador)
            bala.dibujar(g2, true);

        for (Bala bala : balasEnemigos)
            bala.dibujar(g2, false);

        // Jefe
        if (jefeActivo && !victoria)
            dibujarJefe(g2);

        // Barra superior
        dibujarBarraSuperior(g2);

        // =========================
        // MENSAJE NIVEL
        // =========================

        if (tiempoNivel > 0 &&
            nivel < 10 &&
            !pausa &&
            !gameOver &&
            !victoria) {

            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 50));
            textoCentrado(g2, "NIVEL " + nivel, 350);
        }

        // =========================
        // NIVEL 10
        // =========================

        if (jefeActivo &&
            !victoria &&
            !gameOver) {

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            textoCentrado(g2, "\u00a1\u00a1JEFE FINAL!!", 582);
        }

        // =========================
        // PAUSA
        // =========================

        if (pausa) {

            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRect(0, ALTO_BARRA, ANCHO, ALTO - ALTO_BARRA);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            textoCentrado(g2, "PAUSA", 330);

            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            textoCentrado(g2, "Presiona P o el bot\u00f3n Continuar para seguir", 375);
        }

        // =========================
        // GAME OVER
        // =========================

        if (gameOver) {

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            textoCentrado(g2, "GAME OVER", 280);

            dibujarResultado(g2);
        }

        // =========================
        // VICTORIA
        // =========================

        if (victoria) {

            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            textoCentrado(g2, "\u00a1VICTORIA!", 250);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 25));
            textoCentrado(g2, "\u00a1Derrotaste al JEFE FINAL!", 290);

            dibujarResultado(g2);
        }
    }

    // Puntuacion y record al terminar la partida
    void dibujarResultado(Graphics2D g2) {

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 25));
        textoCentrado(g2, "Puntuaci\u00f3n: " + puntuacion, 335);

        g2.setColor(Color.YELLOW);
        textoCentrado(g2, "R\u00e9cord: " + record, 370);

        if (puntuacion > recordAlIniciar && puntuacion > 0) {

            g2.setFont(new Font("Arial", Font.BOLD, 30));
            textoCentrado(g2, "\u00a1NUEVO R\u00c9CORD!", 415);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        textoCentrado(g2, "Presiona ENTER para jugar de nuevo", 465);
        textoCentrado(g2, "o el bot\u00f3n Men\u00fa para volver al inicio", 495);
    }

    void dibujarBarraSuperior(Graphics2D g2) {

        g2.setColor(new Color(20, 20, 35));
        g2.fillRect(0, 0, ANCHO, ALTO_BARRA);

        g2.setColor(new Color(70, 70, 110));
        g2.drawLine(0, ALTO_BARRA, ANCHO, ALTO_BARRA);

        g2.setFont(new Font("Arial", Font.BOLD, 16));

        g2.setColor(Color.WHITE);
        g2.drawString("PUNTOS: " + puntuacion, 15, 29);

        g2.setColor(Color.YELLOW);
        g2.drawString("R\u00c9CORD: " + record, 160, 29);

        g2.setColor(Color.WHITE);
        g2.drawString("NIVEL: " + nivel, 320, 29);
        g2.drawString("VIDAS: " + vidas, 420, 29);

        for (Boton boton : botonesVisibles())
            boton.dibujar(g2);
    }

    // =========================
    // DIBUJAR JUGADOR
    // =========================
    void dibujarJugador(Graphics2D g) {

        // Nave
        g.setColor(Color.CYAN);

        int[] x = {
                jugadorX + jugadorAncho / 2,
                jugadorX,
                jugadorX + jugadorAncho
        };

        int[] y = {
                jugadorY,
                jugadorY + jugadorAlto,
                jugadorY + jugadorAlto
        };

        g.fillPolygon(x, y, 3);

        // Cabina
        g.setColor(Color.WHITE);
        g.fillOval(jugadorX + 18, jugadorY + 8, 14, 14);

        // Motores
        g.setColor(Color.ORANGE);
        g.fillRect(jugadorX + 10, jugadorY + jugadorAlto, 10, 8);
        g.fillRect(jugadorX + 30, jugadorY + jugadorAlto, 10, 8);
    }

    // =========================
    // DIBUJAR JEFE
    // =========================
    void dibujarJefe(Graphics2D g) {

        // Barra de vida
        g.setColor(Color.RED);
        g.fillRect(250, 590, 300, 20);

        g.setColor(Color.GREEN);

        int vidaBarra =
                (int) (300 * ((double) jefeVida / jefeVidaMaxima));

        g.fillRect(250, 590, vidaBarra, 20);

        g.setColor(Color.WHITE);
        g.drawRect(250, 590, 300, 20);

        // Cuerpo del jefe
        g.setColor(Color.MAGENTA);
        g.fillOval(jefeX, jefeY, jefeAncho, jefeAlto);

        // Alas
        g.setColor(Color.RED);
        g.fillRect(jefeX - 30, jefeY + 30, 40, 40);
        g.fillRect(jefeX + jefeAncho - 10, jefeY + 30, 40, 40);

        // Centro
        g.setColor(Color.BLACK);
        g.fillOval(jefeX + 70, jefeY + 25, 60, 50);

        // Ojos
        g.setColor(Color.YELLOW);
        g.fillOval(jefeX + 45, jefeY + 30, 20, 20);
        g.fillOval(jefeX + 135, jefeY + 30, 20, 20);
    }

    // =========================
    // TIMER
    // =========================
    @Override
    public void actionPerformed(ActionEvent e) {

        actualizar();

        repaint();
    }

    // =========================
    // TECLAS
    // =========================
    @Override
    public void keyPressed(KeyEvent e) {

        int tecla = e.getKeyCode();

        switch (estado) {

            case MENU:

                if (tecla == KeyEvent.VK_ENTER)
                    iniciarPartida();

                if (tecla == KeyEvent.VK_I)
                    estado = Estado.INSTRUCCIONES;

                if (tecla == KeyEvent.VK_ESCAPE)
                    salir();

                return;

            case INSTRUCCIONES:

                if (tecla == KeyEvent.VK_ESCAPE ||
                    tecla == KeyEvent.VK_ENTER)
                    estado = Estado.MENU;

                return;

            case JUGANDO:

                if (tecla == KeyEvent.VK_ESCAPE) {
                    irAlMenu();
                    return;
                }

                if (tecla == KeyEvent.VK_P) {
                    alternarPausa();
                    return;
                }

                if (tecla == KeyEvent.VK_R) {
                    reiniciarNivel();
                    return;
                }

                if (tecla == KeyEvent.VK_ENTER &&
                    (gameOver || victoria)) {
                    iniciarPartida();
                    return;
                }

                if (pausa)
                    return;

                if (tecla == KeyEvent.VK_LEFT)
                    izquierda = true;

                if (tecla == KeyEvent.VK_RIGHT)
                    derecha = true;

                if (tecla == KeyEvent.VK_SPACE)
                    disparar();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int tecla = e.getKeyCode();

        if (tecla == KeyEvent.VK_LEFT)
            izquierda = false;

        if (tecla == KeyEvent.VK_RIGHT)
            derecha = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // =====================================================
    // CLASE BOTON
    // =====================================================

    class Boton {

        String texto;

        int x;
        int y;
        int ancho;
        int alto;

        Runnable accion;

        Boton(String texto, int x, int y, int ancho, int alto, Runnable accion) {

            this.texto = texto;
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
            this.accion = accion;
        }

        boolean contiene(Point punto) {

            return punto != null &&
                   new Rectangle(x, y, ancho, alto).contains(punto);
        }

        void dibujar(Graphics2D g) {

            boolean encima = contiene(posicionRaton);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Fondo (cambia de color al pasar el raton)
            g.setColor(encima ? new Color(0, 140, 160) : new Color(30, 30, 70));
            g.fillRoundRect(x, y, ancho, alto, 12, 12);

            // Borde
            g.setColor(Color.CYAN);
            g.drawRoundRect(x, y, ancho, alto, 12, 12);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);

            // Texto centrado
            int tamano = alto >= 40 ? 22 : 14;

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, tamano));

            FontMetrics fm = g.getFontMetrics();

            int tx = x + (ancho - fm.stringWidth(texto)) / 2;
            int ty = y + (alto - fm.getHeight()) / 2 + fm.getAscent();

            g.drawString(texto, tx, ty);
        }
    }

    // =====================================================
    // CLASE ENEMIGO
    // =====================================================

    class Enemigo {

        int x;
        int y;

        int ancho = 35;
        int alto = 30;

        int vida;

        Enemigo(int x, int y, int vida) {

            this.x = x;
            this.y = y;
            this.vida = vida;
        }

        Rectangle getRectangulo() {

            return new Rectangle(x - 5, y, ancho + 10, alto);
        }

        void dibujar(Graphics2D g) {

            g.setColor(Color.RED);
            g.fillOval(x, y, ancho, alto);

            // Alas
            g.fillRect(x - 8, y + 10, 10, 10);
            g.fillRect(x + ancho - 2, y + 10, 10, 10);

            // Ojos
            g.setColor(Color.YELLOW);
            g.fillOval(x + 8, y + 8, 6, 6);
            g.fillOval(x + 21, y + 8, 6, 6);
        }
    }

    // =====================================================
    // CLASE BALA
    // =====================================================

    class Bala {

        int x;
        int y;

        int velocidad;

        int ancho = 6;
        int alto = 15;

        Bala(int x, int y, int velocidad) {

            this.x = x;
            this.y = y;
            this.velocidad = velocidad;
        }

        Rectangle getRectangulo() {

            return new Rectangle(x, y, ancho, alto);
        }

        void dibujar(Graphics2D g, boolean jugador) {

            if (jugador)
                g.setColor(Color.YELLOW);
            else
                g.setColor(Color.ORANGE);

            g.fillRect(x, y, ancho, alto);
        }
    }

    // =====================================================
    // MAIN
    // =====================================================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame ventana = new JFrame("GALAGA - 10 NIVELES");

            Galaga juego = new Galaga();

            // Logo en la esquina superior izquierda de la ventana
            ventana.setIconImage(juego.logo);

            ventana.add(juego);
            ventana.pack();

            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Guardar el record al cerrar la ventana
            ventana.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    juego.guardarRecord();
                }
            });

            ventana.setLocationRelativeTo(null);
            ventana.setResizable(false);
            ventana.setVisible(true);

            juego.requestFocusInWindow();
        });
    }
}