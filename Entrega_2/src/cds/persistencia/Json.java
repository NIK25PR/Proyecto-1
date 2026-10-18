package cds.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lector y escritor de JSON minimo, sin librerias externas.
 *
 * Representacion en Java:
 * objeto -> Map<String, Object>, arreglo -> List<Object>, texto -> String,
 * numero -> Long o Double, true/false -> Boolean, null -> null.
 */
public final class Json {

	private Json() {
	}


	// ------------------------------------------------------------ archivos

	public static void guardarArchivo(Path ruta, Object valor) throws IOException {
		Path carpeta = ruta.toAbsolutePath().getParent();
		if (carpeta != null) {
			Files.createDirectories(carpeta);
		}
		Files.writeString(ruta, escribir(valor), StandardCharsets.UTF_8);
	}

	public static Object leerArchivo(Path ruta) throws IOException {
		String contenido = Files.readString(ruta, StandardCharsets.UTF_8);
		try {
			return leer(contenido);
		} catch (IllegalArgumentException e) {
			throw new IOException("JSON invalido en " + ruta + ": " + e.getMessage(), e);
		}
	}


	// ------------------------------------------------------------ escritura

	public static String escribir(Object valor) {
		StringBuilder sb = new StringBuilder();
		escribir(valor, sb, 0);
		sb.append('\n');
		return sb.toString();
	}

	private static void escribir(Object v, StringBuilder sb, int nivel) {
		if (v == null) {
			sb.append("null");
		} else if (v instanceof String) {
			escribirTexto((String) v, sb);
		} else if (v instanceof Boolean || v instanceof Integer || v instanceof Long) {
			sb.append(v);
		} else if (v instanceof Number) {
			double d = ((Number) v).doubleValue();
			if (Double.isNaN(d) || Double.isInfinite(d)) {
				throw new IllegalArgumentException("Numero no valido para JSON: " + d);
			}
			if (d == Math.rint(d) && Math.abs(d) < 1e15) {
				sb.append((long) d);
			} else {
				sb.append(d);
			}
		} else if (v instanceof Map) {
			Map<?, ?> mapa = (Map<?, ?>) v;
			if (mapa.isEmpty()) {
				sb.append("{}");
				return;
			}
			sb.append("{\n");
			int i = 0;
			for (Map.Entry<?, ?> e : mapa.entrySet()) {
				sangria(sb, nivel + 1);
				escribirTexto(String.valueOf(e.getKey()), sb);
				sb.append(": ");
				escribir(e.getValue(), sb, nivel + 1);
				if (++i < mapa.size()) {
					sb.append(',');
				}
				sb.append('\n');
			}
			sangria(sb, nivel);
			sb.append('}');
		} else if (v instanceof List) {
			List<?> lista = (List<?>) v;
			if (lista.isEmpty()) {
				sb.append("[]");
				return;
			}
			sb.append("[\n");
			for (int i = 0; i < lista.size(); i++) {
				sangria(sb, nivel + 1);
				escribir(lista.get(i), sb, nivel + 1);
				if (i < lista.size() - 1) {
					sb.append(',');
				}
				sb.append('\n');
			}
			sangria(sb, nivel);
			sb.append(']');
		} else {
			throw new IllegalArgumentException("No se puede escribir en JSON un " + v.getClass().getSimpleName());
		}
	}

	private static void sangria(StringBuilder sb, int nivel) {
		for (int i = 0; i < nivel; i++) {
			sb.append("  ");
		}
	}

	private static void escribirTexto(String s, StringBuilder sb) {
		sb.append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '"': sb.append("\\\""); break;
			case '\\': sb.append("\\\\"); break;
			case '\n': sb.append("\\n"); break;
			case '\r': sb.append("\\r"); break;
			case '\t': sb.append("\\t"); break;
			default:
				if (c < 0x20) {
					sb.append(String.format("\\u%04x", (int) c));
				} else {
					sb.append(c);
				}
			}
		}
		sb.append('"');
	}


	// ------------------------------------------------------------ lectura

	public static Object leer(String texto) {
		Lector l = new Lector(texto);
		l.espacios();
		Object v = l.valor();
		l.espacios();
		if (l.pos < texto.length()) {
			throw l.error("Sobra texto despues del JSON");
		}
		return v;
	}

	private static class Lector {
		private final String s;
		private int pos;

		Lector(String s) {
			this.s = s;
			pos = 0;
		}

		IllegalArgumentException error(String msg) {
			return new IllegalArgumentException(msg + " (posicion " + pos + ")");
		}

		void espacios() {
			while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
				pos++;
			}
		}

		char actual() {
			if (pos >= s.length()) {
				throw error("Fin inesperado del texto");
			}
			return s.charAt(pos);
		}

		void esperar(char c) {
			if (actual() != c) {
				throw error("Se esperaba '" + c + "'");
			}
			pos++;
		}

		Object valor() {
			char c = actual();
			if (c == '{') return objeto();
			if (c == '[') return arreglo();
			if (c == '"') return texto();
			if (c == '-' || Character.isDigit(c)) return numero();
			if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
			if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
			if (s.startsWith("null", pos)) { pos += 4; return null; }
			throw error("Valor no reconocido");
		}

		Map<String, Object> objeto() {
			Map<String, Object> mapa = new LinkedHashMap<String, Object>();
			esperar('{');
			espacios();
			if (actual() == '}') {
				pos++;
				return mapa;
			}
			while (true) {
				espacios();
				String clave = texto();
				espacios();
				esperar(':');
				espacios();
				mapa.put(clave, valor());
				espacios();
				if (actual() == ',') {
					pos++;
				} else {
					esperar('}');
					return mapa;
				}
			}
		}

		List<Object> arreglo() {
			List<Object> lista = new ArrayList<Object>();
			esperar('[');
			espacios();
			if (actual() == ']') {
				pos++;
				return lista;
			}
			while (true) {
				espacios();
				lista.add(valor());
				espacios();
				if (actual() == ',') {
					pos++;
				} else {
					esperar(']');
					return lista;
				}
			}
		}

		String texto() {
			esperar('"');
			StringBuilder sb = new StringBuilder();
			while (true) {
				char c = actual();
				pos++;
				if (c == '"') {
					return sb.toString();
				}
				if (c != '\\') {
					sb.append(c);
					continue;
				}
				char e = actual();
				pos++;
				switch (e) {
				case '"': sb.append('"'); break;
				case '\\': sb.append('\\'); break;
				case '/': sb.append('/'); break;
				case 'n': sb.append('\n'); break;
				case 'r': sb.append('\r'); break;
				case 't': sb.append('\t'); break;
				case 'b': sb.append('\b'); break;
				case 'f': sb.append('\f'); break;
				case 'u':
					if (pos + 4 > s.length()) {
						throw error("Escape \\u incompleto");
					}
					sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
					pos += 4;
					break;
				default:
					throw error("Escape no valido: \\" + e);
				}
			}
		}

		Object numero() {
			int inicio = pos;
			while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) {
				pos++;
			}
			String num = s.substring(inicio, pos);
			try {
				if (num.contains(".") || num.contains("e") || num.contains("E")) {
					return Double.parseDouble(num);
				}
				return Long.parseLong(num);
			} catch (NumberFormatException ex) {
				throw error("Numero no valido: " + num);
			}
		}
	}


	// ------------------------------------------------------------ ayudas

	public static Map<String, Object> nuevoObjeto() {
		return new LinkedHashMap<String, Object>();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> objeto(Object v) {
		if (!(v instanceof Map)) {
			throw new IllegalArgumentException("Se esperaba un objeto JSON");
		}
		return (Map<String, Object>) v;
	}

	@SuppressWarnings("unchecked")
	public static List<Object> lista(Object v) {
		if (!(v instanceof List)) {
			throw new IllegalArgumentException("Se esperaba un arreglo JSON");
		}
		return (List<Object>) v;
	}

	private static Object campo(Map<String, Object> o, String nombre) {
		if (!o.containsKey(nombre)) {
			throw new IllegalArgumentException("Falta el campo \"" + nombre + "\"");
		}
		return o.get(nombre);
	}

	public static List<Object> lista(Map<String, Object> o, String nombre) {
		return lista(campo(o, nombre));
	}

	public static String texto(Map<String, Object> o, String nombre) {
		Object v = campo(o, nombre);
		if (v != null && !(v instanceof String)) {
			throw new IllegalArgumentException("El campo \"" + nombre + "\" debe ser texto");
		}
		return (String) v;
	}

	public static double decimal(Map<String, Object> o, String nombre) {
		Object v = campo(o, nombre);
		if (!(v instanceof Number)) {
			throw new IllegalArgumentException("El campo \"" + nombre + "\" debe ser numero");
		}
		return ((Number) v).doubleValue();
	}

	public static int entero(Map<String, Object> o, String nombre) {
		return (int) Math.round(decimal(o, nombre));
	}

	public static boolean booleano(Map<String, Object> o, String nombre) {
		Object v = campo(o, nombre);
		if (!(v instanceof Boolean)) {
			throw new IllegalArgumentException("El campo \"" + nombre + "\" debe ser true o false");
		}
		return (Boolean) v;
	}

	/** Fecha en formato AAAA-MM-DD, o null si el campo es null. */
	public static LocalDate fecha(Map<String, Object> o, String nombre) {
		String t = texto(o, nombre);
		return t == null ? null : LocalDate.parse(t);
	}

	public static String fecha(LocalDate f) {
		return f == null ? null : f.toString();
	}

}
