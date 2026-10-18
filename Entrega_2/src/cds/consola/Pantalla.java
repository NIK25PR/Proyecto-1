package cds.consola;

import java.text.NumberFormat;
import java.util.Locale;

import cds.ReglaNegocioException;

/**
 * Ayudas para mostrar en consola el estado de la aplicacion de forma clara.
 *
 * Cada comprobacion imprime una etiqueta:
 *   [OK]         la operacion funciono como se esperaba
 *   [RECHAZADO]  una regla de negocio bloqueo la operacion, como debia ser
 *   [FALLA]      el resultado no es el que dicen las reglas del proyecto
 */
public final class Pantalla {

	private static final NumberFormat PESOS = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));
	private static int correctas = 0;
	private static int fallas = 0;

	/** Una accion que puede lanzar excepciones. */
	public interface Paso {
		void ejecutar() throws Exception;
	}

	private Pantalla() {
	}


	public static void titulo(String texto) {
		String linea = "=".repeat(texto.length() + 8);
		System.out.println();
		System.out.println(linea);
		System.out.println("    " + texto);
		System.out.println(linea);
		correctas = 0;
		fallas = 0;
	}

	public static void seccion(String texto) {
		System.out.println();
		System.out.println("--- " + texto + " " + "-".repeat(Math.max(3, 60 - texto.length())));
	}

	public static void info(String texto) {
		System.out.println("    " + texto);
	}

	public static String pesos(double valor) {
		return "$" + PESOS.format(Math.round(valor));
	}


	/** La operacion debe funcionar. */
	public static void esperarExito(String descripcion, Paso paso) {
		try {
			paso.ejecutar();
			correctas++;
			System.out.println("  [OK]        " + descripcion);
		} catch (Exception e) {
			fallas++;
			System.out.println("  [FALLA]     " + descripcion);
			System.out.println("              -> no debia fallar: " + e.getMessage());
		}
	}

	/** Una regla de negocio debe impedir la operacion. */
	public static void esperarRechazo(String descripcion, Paso paso) {
		try {
			paso.ejecutar();
			fallas++;
			System.out.println("  [FALLA]     " + descripcion);
			System.out.println("              -> debia rechazarse y se permitio");
		} catch (ReglaNegocioException e) {
			correctas++;
			System.out.println("  [RECHAZADO] " + descripcion);
			System.out.println("              -> " + e.getMessage());
		} catch (Exception e) {
			fallas++;
			System.out.println("  [FALLA]     " + descripcion);
			System.out.println("              -> error inesperado: " + e);
		}
	}

	/** Comprueba que un valor sea el esperado. */
	public static void verificar(String descripcion, boolean cumple) {
		if (cumple) {
			correctas++;
			System.out.println("  [OK]        " + descripcion);
		} else {
			fallas++;
			System.out.println("  [FALLA]     " + descripcion);
		}
	}

	public static void resumen() {
		System.out.println();
		System.out.println("RESUMEN: " + correctas + " comprobaciones correctas, " + fallas + " fallas.");
		if (fallas > 0) {
			System.out.println("Las lineas [FALLA] indican reglas del proyecto que el modelo todavia no cumple.");
		}
	}

}
