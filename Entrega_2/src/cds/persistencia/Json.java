package cds.persistencia;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Json {
	public static Object leer(String texto) throws IOException {
		Analizador analizador = new Analizador(texto);
		Object resultado = analizador.leerValor();
		analizador.espacios();
		if (!analizador.terminado()) {
			throw new IOException("Hay datos despues del documento JSON");
		}
		return resultado;
	}

	public static void escribir(Object valor, PrintWriter salida) throws IOException {
		escribirValor(valor, salida, 0);
		salida.println();
	}

	public static Map<String, Object> objeto() {
		return new LinkedHashMap<String, Object>();
	}

	public static List<Object> arreglo() {
		return new ArrayList<Object>();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> comoObjeto(Object valor) throws IOException {
		if (!(valor instanceof Map)) {
			throw new IOException("Se esperaba un objeto JSON");
		}
		return (Map<String, Object>) valor;
	}

	@SuppressWarnings("unchecked")
	public static List<Object> comoArreglo(Object valor) throws IOException {
		if (!(valor instanceof List)) {
			throw new IOException("Se esperaba un arreglo JSON");
		}
		return (List<Object>) valor;
	}

	public static String texto(Map<String, Object> objeto, String campo) throws IOException {
		Object valor = objeto.get(campo);
		if (!(valor instanceof String)) {
			throw new IOException("El campo " + campo + " debe ser texto");
		}
		return (String) valor;
	}

	public static String textoOpcional(Map<String, Object> objeto, String campo) throws IOException {
		Object valor = objeto.get(campo);
		if (valor == null) {
			return null;
		}
		if (!(valor instanceof String)) {
			throw new IOException("El campo " + campo + " debe ser texto o null");
		}
		return (String) valor;
	}

	public static int entero(Map<String, Object> objeto, String campo) throws IOException {
		Object valor = objeto.get(campo);
		if (!(valor instanceof Number)) {
			throw new IOException("El campo " + campo + " debe ser numerico");
		}
		return ((Number) valor).intValue();
	}

	public static double decimal(Map<String, Object> objeto, String campo) throws IOException {
		Object valor = objeto.get(campo);
		if (!(valor instanceof Number)) {
			throw new IOException("El campo " + campo + " debe ser numerico");
		}
		return ((Number) valor).doubleValue();
	}

	public static boolean booleano(Map<String, Object> objeto, String campo) throws IOException {
		Object valor = objeto.get(campo);
		if (!(valor instanceof Boolean)) {
			throw new IOException("El campo " + campo + " debe ser booleano");
		}
		return ((Boolean) valor).booleanValue();
	}

	private static void escribirValor(Object valor, PrintWriter salida, int nivel) throws IOException {
		if (valor == null) {
			salida.print("null");
		} else if (valor instanceof String) {
			escribirTexto((String) valor, salida);
		} else if (valor instanceof Number || valor instanceof Boolean) {
			if (valor instanceof Double && !Double.isFinite(((Double) valor).doubleValue())) {
				throw new IOException("JSON no admite un numero no finito");
			}
			if (valor instanceof Float && !Float.isFinite(((Float) valor).floatValue())) {
				throw new IOException("JSON no admite un numero no finito");
			}
			salida.print(valor.toString());
		} else if (valor instanceof Map) {
			escribirObjeto((Map<?, ?>) valor, salida, nivel);
		} else if (valor instanceof List) {
			escribirArreglo((List<?>) valor, salida, nivel);
		} else {
			throw new IOException("Tipo de dato no soportado por JSON: " + valor.getClass().getName());
		}
	}

	private static void escribirObjeto(Map<?, ?> objeto, PrintWriter salida, int nivel) throws IOException {
		salida.print('{');
		if (!objeto.isEmpty()) {
			salida.println();
			int numero = 0;
			for (Map.Entry<?, ?> entrada : objeto.entrySet()) {
				if (!(entrada.getKey() instanceof String)) {
					throw new IOException("Las llaves JSON deben ser texto");
				}
				if (numero > 0) {
					salida.println(',');
				}
				espacios(salida, nivel + 1);
			escribirTexto((String) entrada.getKey(), salida);
				salida.print(": ");
				escribirValor(entrada.getValue(), salida, nivel + 1);
				numero++;
			}
			salida.println();
			espacios(salida, nivel);
		}
		salida.print('}');
	}

	private static void escribirArreglo(List<?> arreglo, PrintWriter salida, int nivel) throws IOException {
		salida.print('[');
		if (!arreglo.isEmpty()) {
			salida.println();
			for (int i = 0; i < arreglo.size(); i++) {
				if (i > 0) {
					salida.println(',');
				}
				espacios(salida, nivel + 1);
				escribirValor(arreglo.get(i), salida, nivel + 1);
			}
			salida.println();
			espacios(salida, nivel);
		}
		salida.print(']');
	}

	private static void escribirTexto(String texto, PrintWriter salida) {
		salida.print('"');
		for (int i = 0; i < texto.length(); i++) {
			char caracter = texto.charAt(i);
			if (caracter == '"') {
				salida.print("\\\"");
			} else if (caracter == '\\') {
				salida.print("\\\\");
			} else if (caracter == '\b') {
				salida.print("\\b");
			} else if (caracter == '\f') {
				salida.print("\\f");
			} else if (caracter == '\n') {
				salida.print("\\n");
			} else if (caracter == '\r') {
				salida.print("\\r");
			} else if (caracter == '\t') {
				salida.print("\\t");
			} else if (caracter < 32) {
				salida.print("\\u00");
				salida.print("0123456789abcdef".charAt((caracter >> 4) & 15));
				salida.print("0123456789abcdef".charAt(caracter & 15));
			} else {
				salida.print(caracter);
			}
		}
		salida.print('"');
	}

	private static void espacios(PrintWriter salida, int nivel) {
		for (int i = 0; i < nivel * 2; i++) {
			salida.print(' ');
		}
	}

	private static class Analizador {
		private final String texto;
		private int posicion;

		private Analizador(String texto) {
			this.texto = texto;
			posicion = 0;
		}

		private Object leerValor() throws IOException {
			espacios();
			if (terminado()) {
				throw new IOException("Documento JSON vacio o incompleto");
			}
			char caracter = texto.charAt(posicion);
			if (caracter == '{') {
				return leerObjeto();
			}
			if (caracter == '[') {
				return leerArreglo();
			}
			if (caracter == '"') {
				return leerTexto();
			}
			if (caracter == 't') {
				leerPalabra("true");
				return Boolean.TRUE;
			}
			if (caracter == 'f') {
				leerPalabra("false");
				return Boolean.FALSE;
			}
			if (caracter == 'n') {
				leerPalabra("null");
				return null;
			}
			return leerNumero();
		}

		private Map<String, Object> leerObjeto() throws IOException {
			Map<String, Object> resultado = objeto();
			posicion++;
			espacios();
			if (tomar('}')) {
				return resultado;
			}
			while (true) {
				espacios();
				if (terminado() || texto.charAt(posicion) != '"') {
					throw new IOException("Se esperaba el nombre de un campo JSON");
				}
				String nombre = leerTexto();
				espacios();
				exigir(':');
				Object valor = leerValor();
				if (resultado.containsKey(nombre)) {
					throw new IOException("Campo JSON duplicado: " + nombre);
				}
				resultado.put(nombre, valor);
				espacios();
				if (tomar('}')) {
					return resultado;
				}
				exigir(',');
			}
		}

		private List<Object> leerArreglo() throws IOException {
			List<Object> resultado = arreglo();
			posicion++;
			espacios();
			if (tomar(']')) {
				return resultado;
			}
			while (true) {
				resultado.add(leerValor());
				espacios();
				if (tomar(']')) {
					return resultado;
				}
				exigir(',');
			}
		}

		private String leerTexto() throws IOException {
			String resultado = "";
			posicion++;
			while (!terminado()) {
				char caracter = texto.charAt(posicion++);
				if (caracter == '"') {
					return resultado;
				}
				if (caracter < 32) {
					throw new IOException("Caracter de control dentro de texto JSON");
				}
				if (caracter == '\\') {
					if (terminado()) {
						throw new IOException("Texto JSON incompleto");
					}
					caracter = texto.charAt(posicion++);
					if (caracter == '"' || caracter == '\\' || caracter == '/') {
						resultado += caracter;
					} else if (caracter == 'b') {
						resultado += '\b';
					} else if (caracter == 'f') {
						resultado += '\f';
					} else if (caracter == 'n') {
						resultado += '\n';
					} else if (caracter == 'r') {
						resultado += '\r';
					} else if (caracter == 't') {
						resultado += '\t';
					} else if (caracter == 'u') {
						resultado += leerUnicode();
					} else {
						throw new IOException("Escape JSON no valido");
					}
				} else {
					resultado += caracter;
				}
			}
			throw new IOException("Texto JSON sin cerrar");
		}

		private char leerUnicode() throws IOException {
			if (posicion + 4 > texto.length()) {
				throw new IOException("Escape Unicode JSON incompleto");
			}
			try {
				char resultado = (char) Integer.parseInt(texto.substring(posicion, posicion + 4), 16);
				posicion += 4;
				return resultado;
			} catch (NumberFormatException e) {
				throw new IOException("Escape Unicode JSON no valido");
			}
		}

		private Number leerNumero() throws IOException {
			int inicio = posicion;
			if (tomar('-') && terminado()) {
				throw new IOException("Numero JSON incompleto");
			}
			while (!terminado() && Character.isDigit(texto.charAt(posicion))) {
				posicion++;
			}
			boolean decimal = false;
			if (tomar('.')) {
				decimal = true;
				int inicioDecimales = posicion;
				while (!terminado() && Character.isDigit(texto.charAt(posicion))) {
					posicion++;
				}
				if (inicioDecimales == posicion) {
					throw new IOException("Numero JSON no valido");
				}
			}
			if (tomar('e') || tomar('E')) {
				decimal = true;
				tomar('+');
				tomar('-');
				int inicioExponente = posicion;
				while (!terminado() && Character.isDigit(texto.charAt(posicion))) {
					posicion++;
				}
				if (inicioExponente == posicion) {
					throw new IOException("Exponente JSON no valido");
				}
			}
			String numero = texto.substring(inicio, posicion);
			try {
				if (decimal) {
					return Double.valueOf(numero);
				}
				return Long.valueOf(numero);
			} catch (NumberFormatException e) {
				throw new IOException("Numero JSON no valido: " + numero);
			}
		}

		private void leerPalabra(String palabra) throws IOException {
			if (!texto.startsWith(palabra, posicion)) {
				throw new IOException("Valor JSON no valido");
			}
			posicion += palabra.length();
		}

		private void espacios() {
			while (!terminado() && Character.isWhitespace(texto.charAt(posicion))) {
				posicion++;
			}
		}

		private boolean tomar(char esperado) {
			if (!terminado() && texto.charAt(posicion) == esperado) {
				posicion++;
				return true;
			}
			return false;
		}

		private void exigir(char esperado) throws IOException {
			if (!tomar(esperado)) {
				throw new IOException("Se esperaba '" + esperado + "' en el documento JSON");
			}
		}

		private boolean terminado() {
			return posicion >= texto.length();
		}
	}
}
