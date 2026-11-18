package org.example;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    // Dataset oficial del Equipo 13 (origen, destino, minutos)
    static final String[] NODOS = {
            "Shinjuku", "Shibuya", "Tokyo", "Ikebukuro", "Ueno", "Shinagawa",
            "Akihabara", "Harajuku", "Roppongi", "Ginza", "Asakusa", "Akasaka",
            "Ebisu", "Meguro", "Yoyogi"
    };

    static final Object[][] RUTAS = {
            {"Shinjuku", "Shibuya", 15}, {"Shibuya", "Tokyo", 15},
            {"Tokyo", "Ikebukuro", 8}, {"Ikebukuro", "Ueno", 23},
            {"Ueno", "Shinagawa", 8}, {"Shinagawa", "Akihabara", 13},
            {"Akihabara", "Harajuku", 18}, {"Harajuku", "Roppongi", 7},
            {"Roppongi", "Ginza", 8}, {"Ginza", "Asakusa", 9},
            {"Asakusa", "Akasaka", 22}, {"Akasaka", "Ebisu", 13},
            {"Ebisu", "Meguro", 11}, {"Meguro", "Yoyogi", 9},
            {"Shinjuku", "Ueno", 38}, {"Ikebukuro", "Harajuku", 15},
            {"Akihabara", "Asakusa", 24}, {"Ginza", "Meguro", 37},
            {"Tokyo", "Ebisu", 31}, {"Shibuya", "Yoyogi", 19}
    };

    // Grafo NO dirigido: el metro se puede recorrer en ambos sentidos
    private final Graph<String, DefaultWeightedEdge> grafo =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    public Main() {
        for (String n : NODOS) {
            grafo.addVertex(n);
        }
        for (Object[] r : RUTAS) {
            DefaultWeightedEdge e = grafo.addEdge((String) r[0], (String) r[1]);
            grafo.setEdgeWeight(e, ((Integer) r[2]).doubleValue());
        }
    }

    // Ruta óptima (Dijkstra). Devuelve null si no hay conexión.
    public GraphPath<String, DefaultWeightedEdge> rutaOptima(String origen, String destino) {
        DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra =
                new DijkstraShortestPath<>(grafo);
        return dijkstra.getPath(origen, destino);
    }

    // Estaciones alcanzables desde 'inicio' (sin incluir la propia estación)
    public List<String> alcanzables(String inicio) {
        List<String> lista = new ArrayList<>();
        BreadthFirstIterator<String, DefaultWeightedEdge> bfs =
                new BreadthFirstIterator<>(grafo, inicio);
        while (bfs.hasNext()) {
            String estacion = bfs.next();
            if (!estacion.equals(inicio)) {
                lista.add(estacion);
            }
        }
        return lista;
    }

    // Zona de influencia (BFS): estaciones alcanzables hasta maxSaltos paradas.
    public void zonaInfluencia(String inicio, int maxSaltos) {
        BreadthFirstIterator<String, DefaultWeightedEdge> bfs =
                new BreadthFirstIterator<>(grafo, inicio);

        System.out.println("\nZona de influencia de " + inicio + " (hasta " + maxSaltos + " saltos):");
        while (bfs.hasNext()) {
            String estacion = bfs.next();
            int saltos = bfs.getDepth(estacion);
            if (saltos > maxSaltos) {
                break; // BFS visita por niveles, ya no hay más dentro del límite
            }
            System.out.println("  Saltos " + saltos + " -> " + estacion);
        }
    }

    void mostrarRuta(String origen, String destino) {
        GraphPath<String, DefaultWeightedEdge> camino = rutaOptima(origen, destino);
        if (camino == null) {
            System.out.println("\nNo hay conexión disponible de " + origen + " a " + destino + ".");
            return;
        }
        List<String> estaciones = camino.getVertexList();
        System.out.println("\nRuta óptima de " + origen + " a " + destino + ":");
        System.out.println("Estaciones del recorrido: " + String.join(" -> ", estaciones));
        System.out.println("\nDetalle por tramo:");
        for (int i = 0; i < estaciones.size() - 1; i++) {
            String a = estaciones.get(i);
            String b = estaciones.get(i + 1);
            int minutos = (int) grafo.getEdgeWeight(grafo.getEdge(a, b));
            System.out.println("  " + (i + 1) + ". " + a + " -> " + b + " (" + minutos + " min)");
        }
        System.out.println("\nParadas totales: " + estaciones.size());
        System.out.println("Tiempo Total: " + (int) camino.getWeight() + " min");
    }

    // ---------- MENÚ INTERACTIVO ----------

    static void listarEstaciones() {
        System.out.println("\nEstaciones disponibles:");
        for (int i = 0; i < NODOS.length; i++) {
            System.out.println("  " + (i + 1) + ". " + NODOS[i]);
        }
    }

    // Número de una estación dentro del arreglo NODOS (1-15)
    static int numeroDe(String estacion) {
        for (int i = 0; i < NODOS.length; i++) {
            if (NODOS[i].equals(estacion)) {
                return i + 1;
            }
        }
        return -1;
    }

    // Lee un número entre min y max; repite hasta que sea válido.
    static int leerNumero(Scanner sc, String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            String linea = sc.nextLine().trim();
            try {
                int n = Integer.parseInt(linea);
                if (n >= min && n <= max) {
                    return n;
                }
            } catch (NumberFormatException e) {
                // cae al mensaje de error
            }
            System.out.println("Entrada inválida. Ingresa un número entre " + min + " y " + max + ".");
        }
    }

    // Muestra solo los destinos alcanzables desde el origen y pide elegir uno
    String elegirDestino(Scanner sc, String origen) {
        List<String> posibles = alcanzables(origen);
        if (posibles.isEmpty()) {
            return null;
        }
        System.out.println("\nDestinos disponibles desde " + origen + ":");
        for (String p : posibles) {
            System.out.println("  " + numeroDe(p) + ". " + p);
        }
        while (true) {
            int d = leerNumero(sc, "Número de la estación de DESTINO: ", 1, NODOS.length);
            if (posibles.contains(NODOS[d - 1])) {
                return NODOS[d - 1];
            }
            System.out.println("Esa estación no está en la lista de destinos disponibles.");
        }
    }

    public static void main(String[] args) {
        Main metro = new Main();
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n===== METRO DE TOKIO - EQUIPO 13 =====");
            System.out.println("1. Ver estaciones");
            System.out.println("2. Calcular ruta óptima (Dijkstra)");
            System.out.println("3. Zona de influencia (BFS)");
            System.out.println("0. Salir");
            opcion = leerNumero(sc, "Elige una opción: ", 0, 3);

            switch (opcion) {
                case 1:
                    listarEstaciones();
                    break;
                case 2:
                    listarEstaciones();
                    int o = leerNumero(sc, "Número de la estación de ORIGEN: ", 1, NODOS.length);
                    String origen = NODOS[o - 1];
                    String destino = metro.elegirDestino(sc, origen);
                    if (destino == null) {
                        System.out.println("\nDesde " + origen + " no se puede llegar a ninguna estación.");
                    } else {
                        metro.mostrarRuta(origen, destino);
                    }
                    break;
                case 3:
                    listarEstaciones();
                    int s = leerNumero(sc, "Número de la estación de INICIO: ", 1, NODOS.length);
                    int saltos = leerNumero(sc, "Máximo de saltos (1-14): ", 1, 14);
                    metro.zonaInfluencia(NODOS[s - 1], saltos);
                    break;
                case 0:
                    System.out.println("¡Hasta pronto!");
                    break;
            }
        } while (opcion != 0);

        sc.close();
    }
}