package cds.consola;

import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Punto de entrada: pregunta que demostracion ejecutar.
 * Ejecutar desde la carpeta Entrega_2 para que encuentre la carpeta datos/.
 */
public class MenuDemos {

	public static void main(String[] args) {
		Scanner entrada = new Scanner(System.in);
		while (true) {
			System.out.println();
			System.out.println("CLUB DEPORTIVO SENECA - Programas de demostracion");
			System.out.println("  1. Deportes de conjunto y competencias (modulos 2 y 3)");
			System.out.println("  2. Academias y entrenamiento (modulos 4 y 5)");
			System.out.println("  3. Tienda y cafeteria (modulo 6)");
			System.out.println("  4. Ejecutar las tres");
			System.out.println("  0. Salir");
			System.out.print("Opcion: ");
			String opcion = entrada.hasNextLine() ? entrada.nextLine().trim() : "0";
			switch (opcion) {
			case "1":
				new DemoDeportes().ejecutar();
				break;
			case "2":
				new DemoAcademias().ejecutar();
				break;
			case "3":
				new DemoTiendaCafeteria().ejecutar(Paths.get("datos"));
				break;
			case "4":
				new DemoDeportes().ejecutar();
				new DemoAcademias().ejecutar();
				new DemoTiendaCafeteria().ejecutar(Paths.get("datos"));
				break;
			case "0":
				System.out.println("Hasta luego.");
				return;
			default:
				System.out.println("Opcion no valida.");
			}
		}
	}

}
