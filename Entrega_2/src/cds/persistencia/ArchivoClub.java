package cds.persistencia;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cds.Club;
import cds.ReglaNegocioException;
import cds.academias.Academia;
import cds.academias.Disciplina;
import cds.academias.InscripcionAcademia;
import cds.academias.Instalacion;
import cds.academias.Modalidad;
import cds.academias.Nivel;
import cds.academias.PartidoPractica;
import cds.academias.RegistroRanking;
import cds.academias.Reserva;
import cds.academias.TipoInstalacion;
import cds.catalogo.Alergeno;
import cds.catalogo.Articulo;
import cds.catalogo.Bebida;
import cds.catalogo.EstadoSugerencia;
import cds.catalogo.Inventario;
import cds.catalogo.ProductoCafeteria;
import cds.catalogo.Snack;
import cds.catalogo.Sugerencia;
import cds.catalogo.Ubicacion;
import cds.catalogo.Vendible;
import cds.competencias.Competencia;
import cds.competencias.CompetenciaPorFase;
import cds.competencias.Fase;
import cds.competencias.Liga;
import cds.competencias.NominaCompetencia;
import cds.competencias.PartidoCompetencia;
import cds.competencias.TipoFase;
import cds.conjunto.CategoriaEdad;
import cds.conjunto.DeporteConjunto;
import cds.conjunto.Equipo;
import cds.conjunto.MiembroEquipo;
import cds.conjunto.PartidoInterno;
import cds.entrenamiento.Asistencia;
import cds.entrenamiento.EstadoAsistencia;
import cds.entrenamiento.EvaluacionDesempeno;
import cds.entrenamiento.SesionEntrenamiento;
import cds.partidos.EstadisticaBaloncesto;
import cds.partidos.EstadisticaFutbol;
import cds.partidos.EstadisticaIndividual;
import cds.partidos.EstadisticaRaqueta;
import cds.partidos.EstadisticaTenisMesa;
import cds.partidos.EstadisticaVoleibol;
import cds.partidos.Partido;
import cds.partidos.Suspension;
import cds.usuarios.Administrador;
import cds.usuarios.Empleado;
import cds.usuarios.Entrenador;
import cds.usuarios.EstadoSolicitud;
import cds.usuarios.Fisioterapeuta;
import cds.usuarios.RestriccionMedica;
import cds.usuarios.Socio;
import cds.usuarios.SolicitudCambioTurno;
import cds.usuarios.Turno;
import cds.usuarios.Usuario;
import cds.ventas.LineaVenta;
import cds.ventas.Mensualidad;
import cds.ventas.TiendaCafeteria;
import cds.ventas.Venta;
import cds.ventas.VentaCafeteria;
import cds.ventas.VentaTienda;

public class ArchivoClub {
	private static final String ARCHIVO = "club.json";

	public static void guardar(Club club, String carpeta) throws IOException {
		File directorio = new File(carpeta);
		if (!directorio.exists() && !directorio.mkdirs()) {
			throw new IOException("No se pudo crear la carpeta de datos: " + carpeta);
		}
		if (!directorio.isDirectory()) {
			throw new IOException("La ruta de datos no es una carpeta: " + carpeta);
		}
		List<Instalacion> instalaciones = todasInstalaciones(club);
		List<NominaCompetencia> nominas = todasNominas(club);
		List<Fase> fases = todasFases(club);
		List<Partido> partidos = todosPartidos(club);
		Map<String, Object> raiz = Json.objeto();
		raiz.put("version", Integer.valueOf(1));
		raiz.put("usuarios", guardarUsuarios(club));
		raiz.put("equipos", guardarEquipos(club, partidos));
		raiz.put("instalaciones", guardarInstalaciones(instalaciones));
		raiz.put("academias", guardarAcademias(club, instalaciones));
		raiz.put("competencias", guardarCompetencias(club, nominas, fases));
		raiz.put("partidos", guardarPartidos(club, partidos, nominas, fases));
		raiz.put("sesiones", guardarSesiones(club, instalaciones));
		raiz.put("tienda", guardarTienda(club.getTienda()));
		raiz.put("turnos", guardarTurnos(club));
		raiz.put("solicitudes", guardarSolicitudes(club));
		File salida = new File(directorio, ARCHIVO);
		Path temporal = Files.createTempFile(directorio.toPath(), ARCHIVO + ".", ".tmp");
		boolean reemplazado = false;
		try {
			try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(temporal, StandardCharsets.UTF_8))) {
				Json.escribir(raiz, out);
				if (out.checkError()) {
					throw new IOException("No se pudo escribir el archivo temporal " + temporal);
				}
			}
			try {
				Files.move(temporal, salida.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporal, salida.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			reemplazado = true;
		} finally {
			if (!reemplazado) {
				try {
					Files.deleteIfExists(temporal);
				} catch (IOException e) {
				}
			}
		}
	}

	public static Club cargar(String carpeta) throws IOException, ReglaNegocioException {
		File archivo = new File(carpeta, ARCHIVO);
		if (!archivo.isFile()) {
			throw new IOException("No se encontro el archivo de datos del club: " + archivo.getPath());
		}
		String contenido = new String(Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8);
		Map<String, Object> raiz = Json.comoObjeto(Json.leer(contenido));
		if (Json.entero(raiz, "version") != 1) {
			throw new IOException("Version de datos no compatible en " + archivo.getPath());
		}
		try {
			return cargarClub(raiz);
		} catch (IllegalArgumentException e) {
			throw new IOException("El archivo JSON contiene un valor no compatible: " + archivo.getPath(), e);
		}
	}

	private static List<Object> guardarUsuarios(Club club) throws IOException {
		List<Object> resultado = Json.arreglo();
		for (Usuario usuario : club.getUsuarios()) {
			Map<String, Object> item = Json.objeto();
			item.put("login", usuario.getLogin());
			item.put("password", usuario.getPassword());
			item.put("nombre", usuario.getNombre());
			item.put("puntosFidelidad", Integer.valueOf(usuario.getPuntosFidelidad()));
			item.put("tieneBebidaCaliente", Boolean.valueOf(usuario.tieneBebidaCaliente()));
			if (usuario instanceof Socio) {
				Socio socio = (Socio) usuario;
				item.put("tipo", "SOCIO");
				item.put("documento", socio.getDocumento());
				item.put("fechaNacimiento", socio.getFechaNacimiento().toString());
				item.put("activo", Boolean.valueOf(socio.isActivo()));
				item.put("entrenador", Boolean.valueOf(socio.esEntrenador()));
				List<Object> restricciones = Json.arreglo();
				for (RestriccionMedica restriccion : socio.getRestricciones()) {
					Map<String, Object> registro = Json.objeto();
					registro.put("fisioterapeuta", restriccion.getRegistradaPor().getLogin());
					registro.put("motivo", restriccion.getMotivo());
					registro.put("fechaInicio", restriccion.getFechaInicio().toString());
					registro.put("fechaAlta", texto(restriccion.getFechaAlta()));
					restricciones.add(registro);
				}
				item.put("restricciones", restricciones);
			} else if (usuario instanceof Entrenador) {
				item.put("tipo", "ENTRENADOR");
				item.put("codigoDescuento", ((Entrenador) usuario).getCodigoDescuento());
			} else if (usuario instanceof Fisioterapeuta) {
				item.put("tipo", "FISIOTERAPEUTA");
				item.put("codigoDescuento", ((Fisioterapeuta) usuario).getCodigoDescuento());
			} else if (usuario instanceof Administrador) {
				item.put("tipo", "ADMINISTRADOR");
			} else {
				throw new IOException("Tipo de usuario no compatible: " + usuario.getClass().getName());
			}
			resultado.add(item);
		}
		return resultado;
	}

	private static List<Object> guardarEquipos(Club club, List<Partido> partidos) {
		List<Object> resultado = Json.arreglo();
		for (Equipo equipo : club.getEquipos()) {
			Map<String, Object> item = Json.objeto();
			item.put("nombre", equipo.getNombre());
			item.put("deporte", equipo.getDeporte().name());
			item.put("categoria", equipo.getCategoria().name());
			item.put("cupoMaximo", Integer.valueOf(equipo.getCupoMaximo()));
			item.put("activo", Boolean.valueOf(equipo.isActivo()));
			List<Object> miembros = Json.arreglo();
			for (MiembroEquipo miembro : equipo.getMiembros()) {
				Map<String, Object> registro = Json.objeto();
				registro.put("socio", miembro.getSocio().getLogin());
				registro.put("posicion", miembro.getPosicion());
				registro.put("numeroCamiseta", Integer.valueOf(miembro.getNumeroCamiseta()));
				registro.put("fechaIngreso", miembro.getFechaIngreso().toString());
				miembros.add(registro);
			}
			item.put("miembros", miembros);
			List<Object> suspensiones = Json.arreglo();
			for (Suspension suspension : equipo.getSuspensiones()) {
				Map<String, Object> registro = Json.objeto();
				registro.put("socio", suspension.getSocio().getLogin());
				registro.put("motivo", suspension.getMotivo());
				registro.put("origen", Integer.valueOf(idPartido(partidos, suspension.getOrigen())));
				registro.put("cumplida", Boolean.valueOf(suspension.isCumplida()));
				suspensiones.add(registro);
			}
			item.put("suspensiones", suspensiones);
			resultado.add(item);
		}
		return resultado;
	}

	private static List<Instalacion> todasInstalaciones(Club club) {
		List<Instalacion> resultado = new ArrayList<Instalacion>();
		for (Academia academia : club.getAcademias()) {
			for (Instalacion instalacion : academia.getInstalaciones()) {
				if (!resultado.contains(instalacion)) {
					resultado.add(instalacion);
				}
			}
		}
		for (SesionEntrenamiento sesion : club.getSesiones()) {
			Instalacion instalacion = sesion.getInstalacion();
			if (instalacion != null && !resultado.contains(instalacion)) {
				resultado.add(instalacion);
			}
		}
		return resultado;
	}

	private static List<Object> guardarInstalaciones(List<Instalacion> instalaciones) {
		List<Object> resultado = Json.arreglo();
		for (Instalacion instalacion : instalaciones) {
			Map<String, Object> item = Json.objeto();
			item.put("nombre", instalacion.getNombre());
			item.put("tipo", instalacion.getTipo().name());
			item.put("techada", Boolean.valueOf(instalacion.isTechada()));
			resultado.add(item);
		}
		return resultado;
	}

	private static List<Object> guardarAcademias(Club club, List<Instalacion> instalaciones) {
		List<Object> resultado = Json.arreglo();
		for (Academia academia : club.getAcademias()) {
			Map<String, Object> item = Json.objeto();
			item.put("nombre", academia.getNombre());
			item.put("disciplina", academia.getDisciplina().name());
			item.put("maxInstalacionesSimultaneas", Integer.valueOf(academia.getMaxInstalacionesSimultaneas()));
			List<Object> inscripciones = Json.arreglo();
			for (InscripcionAcademia inscripcion : academia.getInscripciones()) {
				Map<String, Object> registro = Json.objeto();
				registro.put("socio", inscripcion.getSocio().getLogin());
				registro.put("nivel", inscripcion.getNivel().name());
				registro.put("fecha", inscripcion.getFechaInscripcion().toString());
				registro.put("puntajeRanking", Double.valueOf(inscripcion.getPuntajeRanking()));
				List<Object> historial = Json.arreglo();
				for (RegistroRanking punto : inscripcion.getHistorial()) {
					Map<String, Object> evento = Json.objeto();
					evento.put("fecha", punto.getFecha().toString());
					evento.put("puntaje", Double.valueOf(punto.getPuntaje()));
					historial.add(evento);
				}
				registro.put("historial", historial);
				inscripciones.add(registro);
			}
			item.put("inscripciones", inscripciones);
			List<Object> idsInstalaciones = Json.arreglo();
			for (Instalacion instalacion : academia.getInstalaciones()) {
				idsInstalaciones.add(Integer.valueOf(instalaciones.indexOf(instalacion)));
			}
			item.put("instalaciones", idsInstalaciones);
			List<Object> reservas = Json.arreglo();
			for (Reserva reserva : academia.getReservas()) {
				Map<String, Object> registro = Json.objeto();
				registro.put("instalacion", Integer.valueOf(instalaciones.indexOf(reserva.getInstalacion())));
				registro.put("socio", reserva.getSocio().getLogin());
				registro.put("fecha", reserva.getFecha().toString());
				registro.put("horaInicio", reserva.getHoraInicio().toString());
				registro.put("horaFin", reserva.getHoraFin().toString());
				registro.put("numJugadores", Integer.valueOf(reserva.getNumJugadores()));
				registro.put("modalidad", reserva.getModalidad().name());
				reservas.add(registro);
			}
			item.put("reservas", reservas);
			resultado.add(item);
		}
		return resultado;
	}

	private static List<NominaCompetencia> todasNominas(Club club) {
		List<NominaCompetencia> resultado = new ArrayList<NominaCompetencia>();
		for (Competencia competencia : club.getCompetencias()) {
			resultado.addAll(competencia.getNominas());
		}
		return resultado;
	}

	private static List<Fase> todasFases(Club club) {
		List<Fase> resultado = new ArrayList<Fase>();
		for (Competencia competencia : club.getCompetencias()) {
			if (competencia instanceof CompetenciaPorFase) {
				resultado.addAll(((CompetenciaPorFase) competencia).getFases());
			}
		}
		return resultado;
	}

	private static List<Object> guardarCompetencias(Club club, List<NominaCompetencia> nominas, List<Fase> fases) throws IOException {
		List<Object> resultado = Json.arreglo();
		for (Competencia competencia : club.getCompetencias()) {
			Map<String, Object> item = Json.objeto();
			item.put("tipo", competencia instanceof Liga ? "LIGA" : "FASES");
			item.put("nombre", competencia.getNombre());
			item.put("organizador", competencia.getOrganizador());
			item.put("temporada", Integer.valueOf(competencia.getTemporada()));
			item.put("deporte", competencia.getDeporte().name());
			item.put("categoria", competencia.getCategoria().name());
			item.put("nominaMinima", Integer.valueOf(competencia.getNominaMinima()));
			if (competencia instanceof Liga) {
				Liga liga = (Liga) competencia;
				item.put("puntosVictoria", Integer.valueOf(liga.getPuntosVictoria()));
				item.put("puntosEmpate", Integer.valueOf(liga.getPuntosEmpate()));
				List<Object> tabla = Json.arreglo();
				for (Equipo equipo : liga.getEquiposTabla()) {
					Map<String, Object> fila = Json.objeto();
					fila.put("equipo", equipo.getNombre());
					fila.put("puntos", Integer.valueOf(liga.getPuntos(equipo)));
					tabla.add(fila);
				}
				item.put("tabla", tabla);
			} else if (competencia instanceof CompetenciaPorFase) {
				List<Object> fasesCompetencia = Json.arreglo();
				for (Fase fase : ((CompetenciaPorFase) competencia).getFases()) {
					Map<String, Object> registro = Json.objeto();
					registro.put("id", Integer.valueOf(fases.indexOf(fase)));
					registro.put("nombre", fase.getNombre());
					registro.put("orden", Integer.valueOf(fase.getOrden()));
					registro.put("tipo", fase.getTipo().name());
					fasesCompetencia.add(registro);
				}
				item.put("fases", fasesCompetencia);
			} else {
				throw new IOException("Tipo de competencia no compatible: " + competencia.getClass().getName());
			}
			List<Object> nominasCompetencia = Json.arreglo();
			for (NominaCompetencia nomina : competencia.getNominas()) {
				Map<String, Object> registro = Json.objeto();
				registro.put("id", Integer.valueOf(nominas.indexOf(nomina)));
				registro.put("equipo", nomina.getEquipo().getNombre());
				registro.put("fechaInscripcion", nomina.getFechaInscripcion().toString());
				List<Object> inscritos = Json.arreglo();
				for (Socio socio : nomina.getInscritos()) {
					inscritos.add(socio.getLogin());
				}
				registro.put("inscritos", inscritos);
				nominasCompetencia.add(registro);
			}
			item.put("nominas", nominasCompetencia);
			resultado.add(item);
		}
		return resultado;
	}

	private static List<Partido> todosPartidos(Club club) {
		List<Partido> resultado = new ArrayList<Partido>();
		for (Partido partido : club.getPartidos()) {
			if (!resultado.contains(partido)) {
				resultado.add(partido);
			}
		}
		for (Competencia competencia : club.getCompetencias()) {
			for (NominaCompetencia nomina : competencia.getNominas()) {
				for (PartidoCompetencia partido : nomina.getPartidos()) {
					if (!resultado.contains(partido)) {
						resultado.add(partido);
					}
				}
			}
		}
		for (Equipo equipo : club.getEquipos()) {
			for (Suspension suspension : equipo.getSuspensiones()) {
				Partido origen = suspension.getOrigen();
				if (origen != null && !resultado.contains(origen)) {
					resultado.add(origen);
				}
			}
		}
		return resultado;
	}

	private static List<Object> guardarPartidos(Club club, List<Partido> partidos, List<NominaCompetencia> nominas, List<Fase> fases) throws IOException {
		List<Object> resultado = Json.arreglo();
		for (Partido partido : partidos) {
			Map<String, Object> item = Json.objeto();
			item.put("id", Integer.valueOf(partidos.indexOf(partido)));
			item.put("tipo", tipoPartido(partido));
			item.put("fecha", partido.getFecha().toString());
			item.put("marcadorLocal", Integer.valueOf(partido.getMarcadorLocal()));
			item.put("marcadorVisitante", Integer.valueOf(partido.getMarcadorVisitante()));
			item.put("jugado", Boolean.valueOf(partido.isJugado()));
			item.put("registradoEnClub", Boolean.valueOf(club.getPartidos().contains(partido)));
			List<Object> participantes = Json.arreglo();
			for (Socio socio : partido.getParticipantes()) {
				participantes.add(socio.getLogin());
			}
			item.put("participantes", participantes);
			List<Object> estadisticas = Json.arreglo();
			for (EstadisticaIndividual estadistica : partido.getEstadisticas()) {
				estadisticas.add(guardarEstadistica(estadistica));
			}
			item.put("estadisticas", estadisticas);
			if (partido instanceof PartidoInterno) {
				PartidoInterno interno = (PartidoInterno) partido;
				item.put("local", interno.getLocal().getNombre());
				item.put("visitante", interno.getVisitante().getNombre());
			} else if (partido instanceof PartidoPractica) {
				PartidoPractica practica = (PartidoPractica) partido;
				item.put("academia", practica.getAcademia().getNombre());
				item.put("modalidad", practica.getModalidad().name());
			} else if (partido instanceof PartidoCompetencia) {
				PartidoCompetencia competencia = (PartidoCompetencia) partido;
				item.put("nomina", Integer.valueOf(nominas.indexOf(competencia.getNomina())));
				item.put("rival", competencia.getInstitucionRival());
				item.put("escenario", competencia.getEscenarios());
				item.put("deporte", competencia.getDeporte().name());
				Fase fase = competencia.getFase();
				item.put("fase", Integer.valueOf(fase == null ? -1 : fases.indexOf(fase)));
			}
			resultado.add(item);
		}
		return resultado;
	}

	private static String tipoPartido(Partido partido) throws IOException {
		if (partido instanceof PartidoInterno) {
			return "INTERNO";
		}
		if (partido instanceof PartidoPractica) {
			return "PRACTICA";
		}
		if (partido instanceof PartidoCompetencia) {
			return "COMPETENCIA";
		}
		throw new IOException("Tipo de partido no compatible: " + partido.getClass().getName());
	}

	private static Map<String, Object> guardarEstadistica(EstadisticaIndividual estadistica) throws IOException {
		Map<String, Object> item = Json.objeto();
		item.put("jugador", estadistica.getJugador().getLogin());
		if (estadistica instanceof EstadisticaFutbol) {
			EstadisticaFutbol futbol = (EstadisticaFutbol) estadistica;
			item.put("tipo", "FUTBOL");
			item.put("goles", Integer.valueOf(futbol.getGoles()));
			item.put("asistencias", Integer.valueOf(futbol.getAsistencias()));
			item.put("amarillas", Integer.valueOf(futbol.getAmarillas()));
			item.put("rojas", Integer.valueOf(futbol.getRojas()));
		} else if (estadistica instanceof EstadisticaBaloncesto) {
			EstadisticaBaloncesto baloncesto = (EstadisticaBaloncesto) estadistica;
			item.put("tipo", "BALONCESTO");
			item.put("puntos", Integer.valueOf(baloncesto.getPuntos()));
			item.put("rebotes", Integer.valueOf(baloncesto.getRebotes()));
		} else if (estadistica instanceof EstadisticaVoleibol) {
			EstadisticaVoleibol voleibol = (EstadisticaVoleibol) estadistica;
			item.put("tipo", "VOLEIBOL");
			item.put("aces", Integer.valueOf(voleibol.getAces()));
			item.put("bloqueos", Integer.valueOf(voleibol.getBloqueos()));
		} else if (estadistica instanceof EstadisticaRaqueta) {
			EstadisticaRaqueta raqueta = (EstadisticaRaqueta) estadistica;
			item.put("tipo", "RAQUETA");
			item.put("setsGanados", Integer.valueOf(raqueta.getSetsGanados()));
			item.put("gamesGanados", Integer.valueOf(raqueta.getGamesGanados()));
			item.put("aces", Integer.valueOf(raqueta.getAces()));
			item.put("erroresNoForzados", Integer.valueOf(raqueta.getErroresNoForzados()));
		} else if (estadistica instanceof EstadisticaTenisMesa) {
			EstadisticaTenisMesa mesa = (EstadisticaTenisMesa) estadistica;
			item.put("tipo", "TENIS_MESA");
			item.put("setsGanados", Integer.valueOf(mesa.getSetsGanados()));
			item.put("puntosTotales", Integer.valueOf(mesa.getPuntosTotales()));
		} else {
			throw new IOException("Tipo de estadistica no compatible: " + estadistica.getClass().getName());
		}
		return item;
	}

	private static List<Object> guardarSesiones(Club club, List<Instalacion> instalaciones) {
		List<Object> resultado = Json.arreglo();
		for (SesionEntrenamiento sesion : club.getSesiones()) {
			Map<String, Object> item = Json.objeto();
			item.put("fecha", sesion.getFecha().toString());
			item.put("hora", sesion.getHora().toString());
			item.put("duracionMinutos", Integer.valueOf(sesion.getDuracionMinutos()));
			item.put("finalizada", Boolean.valueOf(sesion.isFinalizado()));
			item.put("responsable", sesion.getResponsable().getLogin());
			item.put("instalacion", Integer.valueOf(sesion.getInstalacion() == null ? -1 : instalaciones.indexOf(sesion.getInstalacion())));
			item.put("equipo", sesion.getEquipo() == null ? null : sesion.getEquipo().getNombre());
			item.put("academia", sesion.getAcademia() == null ? null : sesion.getAcademia().getNombre());
			List<Object> convocados = Json.arreglo();
			for (Asistencia asistencia : sesion.getConvocados()) {
				Map<String, Object> registro = Json.objeto();
				registro.put("socio", asistencia.getSocio().getLogin());
				registro.put("estado", asistencia.getEstado().name());
				EvaluacionDesempeno evaluacion = asistencia.getEvaluacion();
				if (evaluacion == null) {
					registro.put("evaluacion", null);
				} else {
					Map<String, Object> nota = Json.objeto();
					nota.put("nota", Integer.valueOf(evaluacion.getNota()));
					nota.put("comentario", evaluacion.getComentario());
					registro.put("evaluacion", nota);
				}
				convocados.add(registro);
			}
			item.put("convocados", convocados);
			resultado.add(item);
		}
		return resultado;
	}

	private static Map<String, Object> guardarTienda(TiendaCafeteria tienda) throws IOException {
		Map<String, Object> resultado = Json.objeto();
		List<Object> ubicaciones = Json.arreglo();
		for (Ubicacion ubicacion : tienda.getUbicaciones()) {
			Map<String, Object> registro = Json.objeto();
			registro.put("nombre", ubicacion.getNombre());
			ubicaciones.add(registro);
		}
		resultado.put("ubicaciones", ubicaciones);
		List<Object> articulos = Json.arreglo();
		for (Articulo articulo : tienda.getArticulos()) {
			Map<String, Object> registro = Json.objeto();
			registro.put("nombre", articulo.getNombre());
			registro.put("categoria", articulo.getCategoria());
			registro.put("precio", Double.valueOf(articulo.getPrecio()));
			articulos.add(registro);
		}
		resultado.put("articulos", articulos);
		List<Object> inventarios = Json.arreglo();
		for (Inventario inventario : tienda.getInventarios()) {
			Map<String, Object> registro = Json.objeto();
			registro.put("articulo", inventario.getArticulo().getNombre());
			registro.put("ubicacion", inventario.getUbicacion().getNombre());
			registro.put("cantidad", Integer.valueOf(inventario.getCantidad()));
			inventarios.add(registro);
		}
		resultado.put("inventarios", inventarios);
		List<Object> productos = Json.arreglo();
		for (ProductoCafeteria producto : tienda.getProductos()) {
			Map<String, Object> registro = Json.objeto();
			if (producto instanceof Bebida) {
				registro.put("tipo", "BEBIDA");
			} else if (producto instanceof Snack) {
				registro.put("tipo", "SNACK");
			} else {
				throw new IOException("Tipo de producto de cafeteria no compatible: " + producto.getClass().getName());
			}
			registro.put("nombre", producto.getNombre());
			registro.put("precio", Double.valueOf(producto.getPrecio()));
			if (producto instanceof Bebida) {
				registro.put("caliente", Boolean.valueOf(((Bebida) producto).isCaliente()));
			}
			List<Object> alergenos = Json.arreglo();
			for (Alergeno alergeno : producto.getAlergenos()) {
				alergenos.add(alergeno.name());
			}
			registro.put("alergenos", alergenos);
			productos.add(registro);
		}
		resultado.put("productos", productos);
		List<Object> sugerencias = Json.arreglo();
		for (Sugerencia sugerencia : tienda.getSugerencias()) {
			Map<String, Object> registro = Json.objeto();
			registro.put("autor", sugerencia.getAutor().getLogin());
			registro.put("descripcion", sugerencia.getDescripcion());
			registro.put("fecha", sugerencia.getFecha().toString());
			registro.put("estado", sugerencia.getEstado().name());
			sugerencias.add(registro);
		}
		resultado.put("sugerencias", sugerencias);
		List<Object> ventas = Json.arreglo();
		for (Venta venta : tienda.getVentas()) {
			Map<String, Object> registro = Json.objeto();
			if (venta instanceof VentaTienda) {
				registro.put("tipo", "TIENDA");
			} else if (venta instanceof VentaCafeteria) {
				registro.put("tipo", "CAFETERIA");
			} else {
				throw new IOException("Tipo de venta no compatible: " + venta.getClass().getName());
			}
			registro.put("fecha", venta.getFecha().toString());
			registro.put("comprador", venta.getComprador().getLogin());
			registro.put("puntosRedimidos", Integer.valueOf(venta.getPuntosRedimidos()));
			registro.put("confirmada", Boolean.valueOf(venta.isConfirmada()));
			registro.put("puntosProcesados", Boolean.valueOf(venta.isPuntosProcesados()));
			registro.put("total", Double.valueOf(venta.total()));
			registro.put("puntosGenerados", Integer.valueOf(venta.puntosGenerados()));
			if (venta instanceof VentaTienda) {
				VentaTienda ventaTienda = (VentaTienda) venta;
				Empleado duenoCodigo = ventaTienda.getDuenoCodigo();
				registro.put("duenoCodigo", duenoCodigo == null ? null : duenoCodigo.getLogin());
			} else {
				registro.put("porcentajePropina", Double.valueOf(((VentaCafeteria) venta).getPorcentajePropina()));
			}
			List<Object> lineas = Json.arreglo();
			for (LineaVenta linea : venta.getLineas()) {
				Map<String, Object> renglon = Json.objeto();
				Vendible vendible = linea.getProducto();
				if (vendible instanceof Articulo) {
					renglon.put("tipoProducto", "ARTICULO");
				} else if (vendible instanceof ProductoCafeteria) {
					renglon.put("tipoProducto", "CAFETERIA");
				} else {
					throw new IOException("Tipo de producto de venta no compatible: " + vendible.getClass().getName());
				}
				renglon.put("producto", vendible.getNombre());
				renglon.put("cantidad", Integer.valueOf(linea.getCantidad()));
				renglon.put("precioUnitario", Double.valueOf(linea.getPrecioUnitario()));
				lineas.add(renglon);
			}
			registro.put("lineas", lineas);
			ventas.add(registro);
		}
		resultado.put("ventas", ventas);
		List<Object> mensualidades = Json.arreglo();
		for (Mensualidad mensualidad : tienda.getMensualidades()) {
			Map<String, Object> registro = Json.objeto();
			registro.put("socio", mensualidad.getSocio().getLogin());
			registro.put("mes", mensualidad.getMes().toString());
			registro.put("monto", Double.valueOf(mensualidad.getMonto()));
			registro.put("fechaPago", texto(mensualidad.getFechaPago()));
			mensualidades.add(registro);
		}
		resultado.put("mensualidades", mensualidades);
		return resultado;
	}

	private static List<Object> guardarTurnos(Club club) {
		List<Object> resultado = Json.arreglo();
		for (Turno turno : club.getTurnos()) {
			Map<String, Object> item = Json.objeto();
			item.put("empleado", turno.getEmpleado().getLogin());
			item.put("dia", turno.getDia().name());
			item.put("horaInicio", turno.getHoraInicio().toString());
			item.put("horaFin", turno.getHoraFin().toString());
			resultado.add(item);
		}
		return resultado;
	}

	private static List<Object> guardarSolicitudes(Club club) {
		List<Object> resultado = Json.arreglo();
		for (SolicitudCambioTurno solicitud : club.getSolicitudes()) {
			Map<String, Object> item = Json.objeto();
			item.put("solicitante", solicitud.getSolicitante().getLogin());
			item.put("turno", Integer.valueOf(club.getTurnos().indexOf(solicitud.getTurno())));
			item.put("nuevoDia", solicitud.getNuevoDia().name());
			item.put("nuevaHoraInicio", solicitud.getNuevaHoraInicio().toString());
			item.put("nuevaHoraFin", solicitud.getNuevaHoraFin().toString());
			item.put("estado", solicitud.getEstado().name());
			Administrador resueltaPor = solicitud.getResueltaPor();
			item.put("resueltaPor", resueltaPor == null ? null : resueltaPor.getLogin());
			resultado.add(item);
		}
		return resultado;
	}

	private static String texto(LocalDate fecha) {
		return fecha == null ? null : fecha.toString();
	}

	private static int idPartido(List<Partido> partidos, Partido partido) {
		return partido == null ? -1 : partidos.indexOf(partido);
	}

	private static Club cargarClub(Map<String, Object> raiz) throws IOException, ReglaNegocioException {
		Club club = new Club();
		List<Object> datosUsuarios = arreglo(raiz, "usuarios");
		cargarUsuarios(club, datosUsuarios);
		List<Object> datosEquipos = arreglo(raiz, "equipos");
		cargarEquipos(club, datosEquipos);
		List<Instalacion> instalaciones = cargarInstalaciones(raiz);
		List<Object> datosAcademias = arreglo(raiz, "academias");
		cargarAcademias(club, datosAcademias, instalaciones);
		List<NominaCompetencia> nominas = new ArrayList<NominaCompetencia>();
		List<Fase> fases = new ArrayList<Fase>();
		List<Object> datosCompetencias = arreglo(raiz, "competencias");
		cargarCompetencias(club, datosCompetencias, nominas, fases);
		List<Partido> partidos = new ArrayList<Partido>();
		List<Object> datosPartidos = arreglo(raiz, "partidos");
		cargarPartidos(club, datosPartidos, nominas, fases, partidos);
		cargarEstadoEquipos(club, datosEquipos, partidos);
		List<Object> datosSesiones = arreglo(raiz, "sesiones");
		cargarSesiones(club, datosSesiones, instalaciones);
		Map<String, Object> datosTienda = objeto(raiz, "tienda");
		cargarTienda(club, datosTienda);
		cargarTurnos(club, arreglo(raiz, "turnos"));
		cargarSolicitudes(club, arreglo(raiz, "solicitudes"));
		cargarEstadoUsuarios(club, datosUsuarios);
		return club;
	}

	private static void cargarUsuarios(Club club, List<Object> datos) throws IOException, ReglaNegocioException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			String tipo = texto(item, "tipo");
			String login = texto(item, "login");
			String password = texto(item, "password");
			String nombre = texto(item, "nombre");
			Usuario usuario;
			if (tipo.equals("SOCIO")) {
				usuario = new Socio(login, password, nombre, texto(item, "documento"), LocalDate.parse(texto(item, "fechaNacimiento")));
			} else if (tipo.equals("ENTRENADOR")) {
				usuario = new Entrenador(login, password, nombre, texto(item, "codigoDescuento"));
			} else if (tipo.equals("FISIOTERAPEUTA")) {
				usuario = new Fisioterapeuta(login, password, nombre, texto(item, "codigoDescuento"));
			} else if (tipo.equals("ADMINISTRADOR")) {
				usuario = new Administrador(login, password, nombre);
			} else {
				throw new IOException("Tipo de usuario no compatible: " + tipo);
			}
			club.registrarUsuario(usuario);
		}
	}

	private static void cargarEquipos(Club club, List<Object> datos) throws IOException, ReglaNegocioException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			Equipo equipo = new Equipo(texto(item, "nombre"), DeporteConjunto.valueOf(texto(item, "deporte")),
					CategoriaEdad.valueOf(texto(item, "categoria")), entero(item, "cupoMaximo"));
			club.registrarEquipo(equipo);
			for (Object miembroDato : arreglo(item, "miembros")) {
				Map<String, Object> miembro = Json.comoObjeto(miembroDato);
				Socio socio = socio(club, texto(miembro, "socio"));
				club.inscribirJugador(equipo, socio, texto(miembro, "posicion"), entero(miembro, "numeroCamiseta"),
						LocalDate.parse(texto(miembro, "fechaIngreso")));
			}
		}
	}

	private static List<Instalacion> cargarInstalaciones(Map<String, Object> raiz) throws IOException {
		List<Instalacion> resultado = new ArrayList<Instalacion>();
		for (Object dato : arreglo(raiz, "instalaciones")) {
			Map<String, Object> item = Json.comoObjeto(dato);
			resultado.add(new Instalacion(texto(item, "nombre"), TipoInstalacion.valueOf(texto(item, "tipo")),
					booleano(item, "techada")));
		}
		return resultado;
	}

	private static void cargarAcademias(Club club, List<Object> datos, List<Instalacion> instalaciones)
			throws IOException, ReglaNegocioException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			Academia academia = new Academia(texto(item, "nombre"), Disciplina.valueOf(texto(item, "disciplina")),
					entero(item, "maxInstalacionesSimultaneas"));
			club.registrarAcademia(academia);
			for (Object id : arreglo(item, "instalaciones")) {
				academia.agregarInstalacion(instalacion(instalaciones, numero(id).intValue()));
			}
			for (Object inscripcionDato : arreglo(item, "inscripciones")) {
				Map<String, Object> registro = Json.comoObjeto(inscripcionDato);
				InscripcionAcademia inscripcion = academia.inscribirSocio(socio(club, texto(registro, "socio")),
						Nivel.valueOf(texto(registro, "nivel")), LocalDate.parse(texto(registro, "fecha")));
				List<RegistroRanking> historial = new ArrayList<RegistroRanking>();
				for (Object puntoDato : arreglo(registro, "historial")) {
					Map<String, Object> punto = Json.comoObjeto(puntoDato);
					historial.add(new RegistroRanking(LocalDate.parse(texto(punto, "fecha")), decimal(punto, "puntaje")));
				}
				inscripcion.restaurarRanking(decimal(registro, "puntajeRanking"), historial);
			}
			for (Object reservaDato : arreglo(item, "reservas")) {
				Map<String, Object> registro = Json.comoObjeto(reservaDato);
				academia.reservar(instalacion(instalaciones, entero(registro, "instalacion")),
						socio(club, texto(registro, "socio")), LocalDate.parse(texto(registro, "fecha")),
						LocalTime.parse(texto(registro, "horaInicio")), LocalTime.parse(texto(registro, "horaFin")),
						entero(registro, "numJugadores"), Modalidad.valueOf(texto(registro, "modalidad")));
			}
		}
	}

	private static void cargarCompetencias(Club club, List<Object> datos, List<NominaCompetencia> nominas,
			List<Fase> fases) throws IOException, ReglaNegocioException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			String tipo = texto(item, "tipo");
			String nombre = texto(item, "nombre");
			String organizador = texto(item, "organizador");
			int temporada = entero(item, "temporada");
			DeporteConjunto deporte = DeporteConjunto.valueOf(texto(item, "deporte"));
			CategoriaEdad categoria = CategoriaEdad.valueOf(texto(item, "categoria"));
			int nominaMinima = entero(item, "nominaMinima");
			Competencia competencia;
			if (tipo.equals("LIGA")) {
				competencia = new Liga(nombre, organizador, temporada, deporte, categoria, nominaMinima,
						entero(item, "puntosVictoria"), entero(item, "puntosEmpate"));
			} else if (tipo.equals("FASES")) {
				competencia = new CompetenciaPorFase(nombre, organizador, temporada, deporte, categoria, nominaMinima);
			} else {
				throw new IOException("Tipo de competencia no compatible: " + tipo);
			}
			club.registrarCompetencia(competencia);
			if (competencia instanceof CompetenciaPorFase) {
				for (Object faseDato : arreglo(item, "fases")) {
					Map<String, Object> registro = Json.comoObjeto(faseDato);
					Fase fase = ((CompetenciaPorFase) competencia).agregarFase(texto(registro, "nombre"),
							entero(registro, "orden"), TipoFase.valueOf(texto(registro, "tipo")));
					fases.add(fase);
					if (entero(registro, "id") != fases.size() - 1) {
						throw new IOException("Identificador de fase fuera de orden en " + nombre);
					}
				}
			}
			for (Object nominaDato : arreglo(item, "nominas")) {
				Map<String, Object> registro = Json.comoObjeto(nominaDato);
				List<Socio> inscritos = new ArrayList<Socio>();
				for (Object login : arreglo(registro, "inscritos")) {
					inscritos.add(socio(club, texto(login)));
				}
				NominaCompetencia nomina = competencia.inscribirEquipo(equipo(club, texto(registro, "equipo")),
						LocalDate.parse(texto(registro, "fechaInscripcion")), inscritos);
				nominas.add(nomina);
				if (entero(registro, "id") != nominas.size() - 1) {
					throw new IOException("Identificador de nomina fuera de orden en " + nombre);
				}
			}
			if (competencia instanceof Liga) {
				Liga liga = (Liga) competencia;
				for (Object filaDato : arreglo(item, "tabla")) {
					Map<String, Object> fila = Json.comoObjeto(filaDato);
					liga.restaurarPuntos(equipo(club, texto(fila, "equipo")), entero(fila, "puntos"));
				}
			}
		}
	}

	private static void cargarPartidos(Club club, List<Object> datos, List<NominaCompetencia> nominas,
			List<Fase> fases, List<Partido> partidos) throws IOException, ReglaNegocioException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			String tipo = texto(item, "tipo");
			LocalDate fecha = LocalDate.parse(texto(item, "fecha"));
			Partido partido;
			if (tipo.equals("INTERNO")) {
				partido = PartidoInterno.restaurar(equipo(club, texto(item, "local")), equipo(club, texto(item, "visitante")), fecha);
			} else if (tipo.equals("PRACTICA")) {
				partido = new PartidoPractica(fecha, academia(club, texto(item, "academia")), Modalidad.valueOf(texto(item, "modalidad")));
			} else if (tipo.equals("COMPETENCIA")) {
				NominaCompetencia nomina = indice(nominas, entero(item, "nomina"), "nomina");
				PartidoCompetencia partidoCompetencia = new PartidoCompetencia(fecha, nomina, texto(item, "rival"),
						texto(item, "escenario"), DeporteConjunto.valueOf(texto(item, "deporte")));
				int faseId = entero(item, "fase");
				if (faseId >= 0) {
					partidoCompetencia.asignarFase(indice(fases, faseId, "fase"));
				}
				partido = partidoCompetencia;
			} else {
				throw new IOException("Tipo de partido no compatible: " + tipo);
			}
			for (Object login : arreglo(item, "participantes")) {
				partido.agregarParticipante(socio(club, texto(login)));
			}
			for (Object estadisticaDato : arreglo(item, "estadisticas")) {
				partido.registrarEstadistica(cargarEstadistica(club, Json.comoObjeto(estadisticaDato)));
			}
			partido.restaurarEstado(entero(item, "marcadorLocal"), entero(item, "marcadorVisitante"), booleano(item, "jugado"));
			if (booleano(item, "registradoEnClub")) {
				club.registrarPartido(partido);
			}
			partidos.add(partido);
			if (entero(item, "id") != partidos.size() - 1) {
				throw new IOException("Identificador de partido fuera de orden");
			}
		}
	}

	private static EstadisticaIndividual cargarEstadistica(Club club, Map<String, Object> item) throws IOException {
		Socio socio = socio(club, texto(item, "jugador"));
		String tipo = texto(item, "tipo");
		if (tipo.equals("FUTBOL")) {
			return new EstadisticaFutbol(socio, entero(item, "goles"), entero(item, "asistencias"),
					entero(item, "amarillas"), entero(item, "rojas"));
		}
		if (tipo.equals("BALONCESTO")) {
			return new EstadisticaBaloncesto(socio, entero(item, "puntos"), entero(item, "rebotes"));
		}
		if (tipo.equals("VOLEIBOL")) {
			return new EstadisticaVoleibol(socio, entero(item, "aces"), entero(item, "bloqueos"));
		}
		if (tipo.equals("RAQUETA")) {
			return new EstadisticaRaqueta(socio, entero(item, "gamesGanados"), entero(item, "setsGanados"),
					entero(item, "aces"), entero(item, "erroresNoForzados"));
		}
		if (tipo.equals("TENIS_MESA")) {
			return new EstadisticaTenisMesa(socio, entero(item, "setsGanados"), entero(item, "puntosTotales"));
		}
		throw new IOException("Tipo de estadistica no compatible: " + tipo);
	}

	private static void cargarEstadoEquipos(Club club, List<Object> datos, List<Partido> partidos) throws IOException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			Equipo equipo = equipo(club, texto(item, "nombre"));
			for (Object suspensionDato : arreglo(item, "suspensiones")) {
				Map<String, Object> registro = Json.comoObjeto(suspensionDato);
				int origen = entero(registro, "origen");
				Partido partidoOrigen = origen < 0 ? null : indice(partidos, origen, "partido de suspension");
				Suspension suspension = new Suspension(socio(club, texto(registro, "socio")), texto(registro, "motivo"), partidoOrigen);
				if (booleano(registro, "cumplida")) {
					suspension.cumplir();
				}
				equipo.agregarSuspension(suspension);
			}
			if (!booleano(item, "activo")) {
				equipo.darDeBaja();
			}
		}
	}

	private static void cargarSesiones(Club club, List<Object> datos, List<Instalacion> instalaciones)
			throws IOException, ReglaNegocioException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			int instalacionId = entero(item, "instalacion");
			String equipoNombre = textoOpcional(item, "equipo");
			String academiaNombre = textoOpcional(item, "academia");
			SesionEntrenamiento sesion = new SesionEntrenamiento(LocalDate.parse(texto(item, "fecha")),
					LocalTime.parse(texto(item, "hora")), entero(item, "duracionMinutos"),
					entrenador(club, texto(item, "responsable")), instalacionId < 0 ? null : indice(instalaciones, instalacionId, "instalacion"),
					equipoNombre == null ? null : equipo(club, equipoNombre),
					academiaNombre == null ? null : academia(club, academiaNombre));
			for (Object convocatoriaDato : arreglo(item, "convocados")) {
				Map<String, Object> registro = Json.comoObjeto(convocatoriaDato);
				Asistencia asistencia = sesion.restaurarConvocado(socio(club, texto(registro, "socio")));
				asistencia.registrarEstado(EstadoAsistencia.valueOf(texto(registro, "estado")));
				Object evaluacionDato = registro.get("evaluacion");
				if (evaluacionDato != null) {
					Map<String, Object> evaluacion = Json.comoObjeto(evaluacionDato);
					asistencia.calificar(entero(evaluacion, "nota"), textoOpcional(evaluacion, "comentario"));
				}
			}
			if (booleano(item, "finalizada")) {
				sesion.finalizar();
			}
			club.registrarSesion(sesion);
		}
	}

	private static void cargarTienda(Club club, Map<String, Object> datos) throws IOException, ReglaNegocioException {
		TiendaCafeteria tienda = club.getTienda();
		for (Object dato : arreglo(datos, "ubicaciones")) {
			Map<String, Object> item = Json.comoObjeto(dato);
			tienda.registrarUbicacion(new Ubicacion(texto(item, "nombre")));
		}
		for (Object dato : arreglo(datos, "articulos")) {
			Map<String, Object> item = Json.comoObjeto(dato);
			tienda.registrarArticulo(new Articulo(texto(item, "nombre"), texto(item, "categoria"), decimal(item, "precio")));
		}
		for (Object dato : arreglo(datos, "productos")) {
			Map<String, Object> item = Json.comoObjeto(dato);
			String tipo = texto(item, "tipo");
			ProductoCafeteria producto;
			if (tipo.equals("BEBIDA")) {
				producto = new Bebida(texto(item, "nombre"), decimal(item, "precio"), booleano(item, "caliente"));
			} else if (tipo.equals("SNACK")) {
				producto = new Snack(texto(item, "nombre"), decimal(item, "precio"));
			} else {
				throw new IOException("Tipo de producto de cafeteria no compatible: " + tipo);
			}
			for (Object alergeno : arreglo(item, "alergenos")) {
				producto.agregarAlergeno(Alergeno.valueOf(texto(alergeno)));
			}
			tienda.registrarProducto(producto);
		}
		for (Object dato : arreglo(datos, "inventarios")) {
			Map<String, Object> item = Json.comoObjeto(dato);
			Articulo articulo = articulo(tienda, texto(item, "articulo"));
			Ubicacion ubicacion = ubicacion(tienda, texto(item, "ubicacion"));
			tienda.registrarInventario(new Inventario(articulo, ubicacion, entero(item, "cantidad")));
		}
		for (Object dato : arreglo(datos, "sugerencias")) {
			Map<String, Object> item = Json.comoObjeto(dato);
			Sugerencia sugerencia = new Sugerencia(empleado(club, texto(item, "autor")), texto(item, "descripcion"),
					LocalDate.parse(texto(item, "fecha")));
			EstadoSugerencia estado = EstadoSugerencia.valueOf(texto(item, "estado"));
			if (estado == EstadoSugerencia.APROBADA) {
				sugerencia.aprobar();
			} else if (estado == EstadoSugerencia.RECHAZADA) {
				sugerencia.rechazar();
			}
			tienda.registrarSugerencia(sugerencia);
		}
		for (Object dato : arreglo(datos, "ventas")) {
			Map<String, Object> item = Json.comoObjeto(dato);
			String tipo = texto(item, "tipo");
			Usuario comprador = usuario(club, texto(item, "comprador"));
			Venta venta;
			if (tipo.equals("TIENDA")) {
				VentaTienda ventaTienda = new VentaTienda(LocalDate.parse(texto(item, "fecha")), comprador);
				String dueno = textoOpcional(item, "duenoCodigo");
				if (dueno != null) {
					ventaTienda.aplicarCodigo(empleado(club, dueno));
				}
				venta = ventaTienda;
			} else if (tipo.equals("CAFETERIA")) {
				VentaCafeteria ventaCafeteria = new VentaCafeteria(LocalDate.parse(texto(item, "fecha")), comprador);
				ventaCafeteria.setPorcentajePropina(decimal(item, "porcentajePropina"));
				venta = ventaCafeteria;
			} else {
				throw new IOException("Tipo de venta no compatible: " + tipo);
			}
			for (Object lineaDato : arreglo(item, "lineas")) {
				Map<String, Object> linea = Json.comoObjeto(lineaDato);
				Vendible producto;
				String tipoProducto = texto(linea, "tipoProducto");
				if (tipoProducto.equals("ARTICULO")) {
					producto = articulo(tienda, texto(linea, "producto"));
				} else if (tipoProducto.equals("CAFETERIA")) {
					producto = producto(tienda, texto(linea, "producto"));
				} else {
					throw new IOException("Tipo de producto de venta no compatible: " + tipoProducto);
				}
				venta.getLineas().add(new LineaVenta(producto, entero(linea, "cantidad"), decimal(linea, "precioUnitario")));
			}
			tienda.restaurarVenta(venta, entero(item, "puntosRedimidos"), booleano(item, "confirmada"),
					booleano(item, "puntosProcesados"));
			if (Double.compare(venta.total(), decimal(item, "total")) != 0
					|| venta.puntosGenerados() != entero(item, "puntosGenerados")) {
				throw new IOException("El total de una venta no coincide con sus lineas y descuentos");
			}
		}
		for (Object dato : arreglo(datos, "mensualidades")) {
			Map<String, Object> item = Json.comoObjeto(dato);
			Mensualidad mensualidad = new Mensualidad(socio(club, texto(item, "socio")),
					YearMonth.parse(texto(item, "mes")), decimal(item, "monto"));
			String fechaPago = textoOpcional(item, "fechaPago");
			if (fechaPago != null) {
				mensualidad.pagar(LocalDate.parse(fechaPago));
			}
			tienda.registrarMensualidad(mensualidad);
		}
	}

	private static void cargarTurnos(Club club, List<Object> datos) throws IOException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			Turno turno = new Turno(empleado(club, texto(item, "empleado")), DayOfWeek.valueOf(texto(item, "dia")),
					LocalTime.parse(texto(item, "horaInicio")), LocalTime.parse(texto(item, "horaFin")));
			club.registrarTurno(turno);
		}
	}

	private static void cargarSolicitudes(Club club, List<Object> datos) throws IOException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			Empleado solicitante = empleado(club, texto(item, "solicitante"));
			Turno turno = indice(club.getTurnos(), entero(item, "turno"), "turno");
			SolicitudCambioTurno solicitud = new SolicitudCambioTurno(solicitante, turno,
					DayOfWeek.valueOf(texto(item, "nuevoDia")), LocalTime.parse(texto(item, "nuevaHoraInicio")),
					LocalTime.parse(texto(item, "nuevaHoraFin")));
			EstadoSolicitud estado = EstadoSolicitud.valueOf(texto(item, "estado"));
			if (estado != EstadoSolicitud.PENDIENTE) {
				String admin = textoOpcional(item, "resueltaPor");
				if (admin == null) {
					throw new IOException("La solicitud resuelta no tiene administrador");
				}
				solicitud.resolver(administrador(club, admin), estado);
			}
			club.getSolicitudes().add(solicitud);
		}
	}

	private static void cargarEstadoUsuarios(Club club, List<Object> datos) throws IOException, ReglaNegocioException {
		for (Object dato : datos) {
			Map<String, Object> item = Json.comoObjeto(dato);
			Usuario usuario = usuario(club, texto(item, "login"));
			int puntos = entero(item, "puntosFidelidad");
			if (puntos < 0) {
				throw new IOException("El saldo de puntos no puede ser negativo");
			}
			usuario.acreditarPuntosFidelidad(puntos);
			if (booleano(item, "tieneBebidaCaliente")) {
				usuario.marcarBebidaCaliente();
			}
			if (usuario instanceof Socio) {
				Socio socio = (Socio) usuario;
				if (booleano(item, "entrenador")) {
					socio.marcarComoEntrenador();
				}
				for (Object restriccionDato : arreglo(item, "restricciones")) {
					Map<String, Object> registro = Json.comoObjeto(restriccionDato);
					Fisioterapeuta fisio = fisioterapeuta(club, texto(registro, "fisioterapeuta"));
					RestriccionMedica restriccion = new RestriccionMedica(fisio, texto(registro, "motivo"),
							LocalDate.parse(texto(registro, "fechaInicio")));
					String fechaAlta = textoOpcional(registro, "fechaAlta");
					if (fechaAlta != null) {
						restriccion.darDeAlta(LocalDate.parse(fechaAlta));
					}
					socio.agregarRestriccion(restriccion);
				}
				if (!booleano(item, "activo")) {
					socio.darDeBaja();
				}
			}
		}
	}

	private static Map<String, Object> objeto(Map<String, Object> objeto, String campo) throws IOException {
		return Json.comoObjeto(objeto.get(campo));
	}

	private static List<Object> arreglo(Map<String, Object> objeto, String campo) throws IOException {
		return Json.comoArreglo(objeto.get(campo));
	}

	private static String texto(Map<String, Object> objeto, String campo) throws IOException {
		return Json.texto(objeto, campo);
	}

	private static String texto(Object valor) throws IOException {
		if (!(valor instanceof String)) {
			throw new IOException("Se esperaba texto en el archivo JSON");
		}
		return (String) valor;
	}

	private static String textoOpcional(Map<String, Object> objeto, String campo) throws IOException {
		return Json.textoOpcional(objeto, campo);
	}

	private static int entero(Map<String, Object> objeto, String campo) throws IOException {
		return Json.entero(objeto, campo);
	}

	private static double decimal(Map<String, Object> objeto, String campo) throws IOException {
		return Json.decimal(objeto, campo);
	}

	private static boolean booleano(Map<String, Object> objeto, String campo) throws IOException {
		return Json.booleano(objeto, campo);
	}

	private static Number numero(Object valor) throws IOException {
		if (!(valor instanceof Number)) {
			throw new IOException("Se esperaba un numero en el archivo JSON");
		}
		return (Number) valor;
	}

	private static Usuario usuario(Club club, String login) throws IOException {
		Usuario usuario = club.buscarUsuario(login);
		if (usuario == null) {
			throw new IOException("El archivo menciona un usuario que no existe: " + login);
		}
		return usuario;
	}

	private static Socio socio(Club club, String login) throws IOException {
		Usuario usuario = usuario(club, login);
		if (!(usuario instanceof Socio)) {
			throw new IOException("El archivo menciona un socio que no existe: " + login);
		}
		return (Socio) usuario;
	}

	private static Empleado empleado(Club club, String login) throws IOException {
		Usuario usuario = usuario(club, login);
		if (!(usuario instanceof Empleado)) {
			throw new IOException("El archivo menciona un empleado que no existe: " + login);
		}
		return (Empleado) usuario;
	}

	private static Entrenador entrenador(Club club, String login) throws IOException {
		Usuario usuario = usuario(club, login);
		if (!(usuario instanceof Entrenador)) {
			throw new IOException("El responsable de la sesion no es entrenador: " + login);
		}
		return (Entrenador) usuario;
	}

	private static Fisioterapeuta fisioterapeuta(Club club, String login) throws IOException {
		Usuario usuario = usuario(club, login);
		if (!(usuario instanceof Fisioterapeuta)) {
			throw new IOException("El usuario no es fisioterapeuta: " + login);
		}
		return (Fisioterapeuta) usuario;
	}

	private static Administrador administrador(Club club, String login) throws IOException {
		Usuario usuario = usuario(club, login);
		if (!(usuario instanceof Administrador)) {
			throw new IOException("El usuario no es administrador: " + login);
		}
		return (Administrador) usuario;
	}

	private static Equipo equipo(Club club, String nombre) throws IOException {
		Equipo equipo = club.buscarEquipo(nombre);
		if (equipo == null) {
			throw new IOException("El archivo menciona un equipo que no existe: " + nombre);
		}
		return equipo;
	}

	private static Academia academia(Club club, String nombre) throws IOException {
		Academia academia = club.buscarAcademia(nombre);
		if (academia == null) {
			throw new IOException("El archivo menciona una academia que no existe: " + nombre);
		}
		return academia;
	}

	private static Instalacion instalacion(List<Instalacion> instalaciones, int id) throws IOException {
		return indice(instalaciones, id, "instalacion");
	}

	private static Articulo articulo(TiendaCafeteria tienda, String nombre) throws IOException {
		Articulo articulo = tienda.buscarArticulo(nombre);
		if (articulo == null) {
			throw new IOException("El archivo menciona un articulo que no existe: " + nombre);
		}
		return articulo;
	}

	private static Ubicacion ubicacion(TiendaCafeteria tienda, String nombre) throws IOException {
		Ubicacion ubicacion = tienda.buscarUbicacion(nombre);
		if (ubicacion == null) {
			throw new IOException("El archivo menciona una ubicacion que no existe: " + nombre);
		}
		return ubicacion;
	}

	private static ProductoCafeteria producto(TiendaCafeteria tienda, String nombre) throws IOException {
		ProductoCafeteria producto = tienda.buscarProducto(nombre);
		if (producto == null) {
			throw new IOException("El archivo menciona un producto que no existe: " + nombre);
		}
		return producto;
	}

	private static <T> T indice(List<T> elementos, int id, String tipo) throws IOException {
		if (id < 0 || id >= elementos.size()) {
			throw new IOException("Referencia de " + tipo + " fuera de rango: " + id);
		}
		return elementos.get(id);
	}

}
