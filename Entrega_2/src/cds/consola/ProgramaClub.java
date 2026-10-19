package cds.consola;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import cds.Club;
import cds.ReglaNegocioException;
import cds.academias.Academia;
import cds.academias.Disciplina;
import cds.academias.Instalacion;
import cds.academias.Modalidad;
import cds.academias.Nivel;
import cds.academias.PartidoPractica;
import cds.academias.TipoInstalacion;
import cds.catalogo.Alergeno;
import cds.catalogo.Articulo;
import cds.catalogo.Inventario;
import cds.catalogo.ProductoCafeteria;
import cds.catalogo.Ubicacion;
import cds.competencias.Competencia;
import cds.competencias.Liga;
import cds.competencias.NominaCompetencia;
import cds.competencias.PartidoCompetencia;
import cds.conjunto.CategoriaEdad;
import cds.conjunto.DeporteConjunto;
import cds.conjunto.Equipo;
import cds.conjunto.MiembroEquipo;
import cds.conjunto.PartidoInterno;
import cds.entrenamiento.Asistencia;
import cds.entrenamiento.EstadoAsistencia;
import cds.entrenamiento.SesionEntrenamiento;
import cds.partidos.EstadisticaFutbol;
import cds.persistencia.ArchivoClub;
import cds.usuarios.Empleado;
import cds.usuarios.Entrenador;
import cds.usuarios.Socio;
import cds.usuarios.Usuario;
import cds.ventas.LineaVenta;
import cds.ventas.Rubro;
import cds.ventas.TiendaCafeteria;
import cds.ventas.Venta;
import cds.ventas.VentaCafeteria;
import cds.ventas.VentaTienda;

public class ProgramaClub {
	private static final LocalDate HOY = LocalDate.of(2026, 10, 18);

	public static void main(String[] args) {
		String carpeta = "datos";
		if (args.length > 0) {
			carpeta = args[0];
		}
		try {
			Club club = ArchivoClub.cargar(carpeta);
			demostrarDeportes(club);
			demostrarAcademiaYEntrenamiento(club);
			demostrarTiendaYCafeteria(club);
			guardarYCargar(club, carpeta + "/salida_club");
		} catch (IOException e) {
			System.out.println("No se pudieron leer o guardar los datos: " + e.getMessage());
		} catch (ReglaNegocioException e) {
			System.out.println("Demostracion detenida: " + e.getMessage());
		}
	}

	private static void demostrarDeportes(Club club) throws ReglaNegocioException {
		System.out.println("=== Deportes de conjunto y competencia ===");
		Equipo rojo = club.buscarEquipo("Seneca Rojo");
		Equipo azul = club.buscarEquipo("Seneca Azul");
		completarEquipo(club, rojo, 8);
		completarEquipo(club, azul, 7);
		System.out.println(rojo.getNombre() + ": " + rojo.getMiembros().size() + " jugadores");
		System.out.println(azul.getNombre() + ": " + azul.getMiembros().size() + " jugadores");

		PartidoInterno interno = club.registrarPartidoInterno(rojo, azul, HOY);
		for (int i = 0; i < 7; i++) {
			interno.agregarParticipante(rojo.getMiembros().get(i).getSocio());
			interno.agregarParticipante(azul.getMiembros().get(i).getSocio());
		}
		Socio ana = (Socio) club.buscarUsuario("ana");
		interno.registrarEstadistica(new EstadisticaFutbol(ana, 1, 0, 2, 0));
		interno.finalizar(2, 1);
		System.out.println("Partido interno: " + interno.getMarcadorLocal() + "-" + interno.getMarcadorVisitante());
		System.out.println("Ana suspendida para el siguiente partido: " + rojo.estaSuspendido(ana));

		Liga liga = new Liga("Liga Universitaria 2026", "ASCUN", 2026, DeporteConjunto.FUTBOL,
				CategoriaEdad.MAYORES, 7, 3, 1);
		club.registrarCompetencia(liga);
		List<Socio> inscritos = new ArrayList<Socio>();
		for (MiembroEquipo miembro : rojo.getMiembros()) {
			inscritos.add(miembro.getSocio());
		}
		NominaCompetencia nomina = liga.inscribirEquipo(rojo, HOY, inscritos);
		PartidoCompetencia oficial = new PartidoCompetencia(HOY.plusDays(7), nomina, "Universidad Norte",
				"Cancha central", DeporteConjunto.FUTBOL);
		for (MiembroEquipo miembro : rojo.getMiembros()) {
			if (!rojo.estaSuspendido(miembro.getSocio())) {
				oficial.agregarParticipante(miembro.getSocio());
			}
		}
		oficial.finalizar(2, 0);
		liga.registrarResultado(oficial);
		System.out.println("Liga: " + rojo.getNombre() + " suma " + liga.getPuntos(rojo) + " puntos");
	}

	private static void completarEquipo(Club club, Equipo equipo, int cupo) throws ReglaNegocioException {
		int numero = equipo.getMiembros().size() + 1;
		while (equipo.getMiembros().size() < cupo) {
			String login = equipo.getNombre().toLowerCase().replace(" ", "") + numero;
			Socio socio = new Socio(login, "clave", "Jugador " + numero, login.toUpperCase(), LocalDate.of(2000, 1, 1));
			club.registrarUsuario(socio);
			club.inscribirJugador(equipo, socio, "Campo", numero, HOY);
			numero++;
		}
	}

	private static void demostrarAcademiaYEntrenamiento(Club club) throws ReglaNegocioException {
		System.out.println();
		System.out.println("=== Academia y entrenamiento ===");
		Academia tenis = club.buscarAcademia("Academia de Tenis");
		Socio ana = (Socio) club.buscarUsuario("ana");
		Socio beto = (Socio) club.buscarUsuario("beto");
		Socio carlos = (Socio) club.buscarUsuario("carlos");
		Socio diana = (Socio) club.buscarUsuario("diana");
		Socio eva = (Socio) club.buscarUsuario("eva");
		Socio felipe = (Socio) club.buscarUsuario("felipe");
		tenis.inscribirSocio(carlos, Nivel.INTERMEDIO, HOY);
		tenis.inscribirSocio(diana, Nivel.INTERMEDIO, HOY);
		tenis.inscribirSocio(eva, Nivel.INTERMEDIO, HOY);
		Instalacion cancha = new Instalacion("Cancha demostracion", TipoInstalacion.CANCHA_TENIS, false);
		tenis.agregarInstalacion(cancha);
		tenis.reservar(cancha, ana, HOY, LocalTime.of(8, 0), LocalTime.of(9, 0), 2, Modalidad.SENCILLOS);
		System.out.println("Reserva de sencillos: " + cancha.getNombre() + " a las 8:00");
		try {
			tenis.reservar(cancha, carlos, HOY, LocalTime.of(8, 30), LocalTime.of(9, 30), 2, Modalidad.SENCILLOS);
			System.out.println("OK: reserva sobre una cancha ocupada");
		} catch (ReglaNegocioException e) {
			System.out.println("Rechazado: " + e.getMessage());
		}

		PartidoPractica dobles = new PartidoPractica(HOY, tenis, Modalidad.DOBLES);
		club.registrarPartido(dobles);
		dobles.agregarParticipante(beto);
		dobles.agregarParticipante(carlos);
		dobles.agregarParticipante(diana);
		dobles.agregarParticipante(eva);
		dobles.finalizar(2, 0);
		System.out.println("Dobles: rankings actualizados para " + dobles.getParticipantes().size() + " jugadores");
		System.out.println("Ranking de Beto: " + tenis.getInscripcion(beto).getPuntajeRanking());

		Entrenador entrenador = (Entrenador) club.buscarUsuario("leo");
		SesionEntrenamiento sesion = new SesionEntrenamiento(HOY, LocalTime.of(10, 0), 60, entrenador,
				cancha, null, tenis);
		club.registrarSesion(sesion);
		Asistencia asistencia = sesion.convocar(ana);
		try {
			sesion.convocar(felipe);
			System.out.println("OK: convocar a alguien fuera de la academia");
		} catch (ReglaNegocioException e) {
			System.out.println("Rechazado: " + e.getMessage());
		}
		sesion.finalizar();
		asistencia.registrarEstado(EstadoAsistencia.ASISTIO);
		asistencia.calificar(5, "Buen saque");
		System.out.println("Asistencia de Ana: " + asistencia.getEstado() + ", nota " + asistencia.getEvaluacion().getNota());
	}

	private static void demostrarTiendaYCafeteria(Club club) throws ReglaNegocioException {
		System.out.println();
		System.out.println("=== Tienda, cafeteria y puntos ===");
		TiendaCafeteria tienda = club.getTienda();
		Socio ana = (Socio) club.buscarUsuario("ana");
		Socio carlos = (Socio) club.buscarUsuario("carlos");
		Empleado leo = (Empleado) club.buscarUsuario("leo");
		Articulo balon = tienda.buscarArticulo("Balon de futbol");
		Ubicacion local = tienda.buscarUbicacion("Tienda");
		VentaTienda compra = new VentaTienda(HOY, ana);
		compra.agregarLinea(balon, 1);
		compra.aplicarCodigo(leo);
		compra.descontarInventario(tienda.inventariosEn(local));
		compra.confirmar();
		tienda.registrarVenta(compra);
		System.out.println("Compra de tienda registrada; puntos de Ana: " + ana.getPuntosFidelidad());
		Articulo camiseta = tienda.buscarArticulo("Camiseta del club");
		VentaTienda compraFutura = new VentaTienda(HOY, ana);
		compraFutura.agregarLinea(camiseta, 1);
		compraFutura.redimirPuntos(100);
		compraFutura.descontarInventario(tienda.inventariosEn(local));
		compraFutura.confirmar();
		tienda.registrarVenta(compraFutura);
		System.out.println("Ana redime 100 puntos; saldo: " + ana.getPuntosFidelidad());

		ProductoCafeteria brownie = tienda.buscarProducto("Brownie");
		brownie.agregarAlergeno(Alergeno.GLUTEN);
		VentaCafeteria cafe = new VentaCafeteria(HOY, carlos);
		cafe.agregarLinea(tienda.buscarProducto("Latte"), 1);
		cafe.agregarLinea(brownie, 1);
		cafe.confirmar();
		try {
			tienda.registrarVenta(cafe);
			System.out.println("OK: venta caliente sin indicar instalacion");
		} catch (ReglaNegocioException e) {
			System.out.println("Rechazado: " + e.getMessage());
		}
		Instalacion canchaPadel = new Instalacion("Cancha padel demostracion", TipoInstalacion.CANCHA_PADEL, false);
		try {
			tienda.registrarVenta(cafe, canchaPadel);
			System.out.println("OK: despacho de bebida caliente en padel");
		} catch (ReglaNegocioException e) {
			System.out.println("Rechazado: " + e.getMessage());
		}
		Instalacion mesa = new Instalacion("Mesa abierta demostracion", TipoInstalacion.MESA_TENIS_MESA, false);
		tienda.registrarVenta(cafe, mesa);
		System.out.println("Alergenos del pedido: " + cafe.alergenos());
		System.out.println("Puntos de Carlos: " + carlos.getPuntosFidelidad());

		Academia padel = new Academia("Academia de Padel", Disciplina.PADEL, 1);
		padel.agregarInstalacion(canchaPadel);
		padel.inscribirSocio(carlos, Nivel.INTERMEDIO, HOY);
		club.registrarAcademia(padel);
		try {
			padel.reservar(canchaPadel, carlos, HOY, LocalTime.of(11, 0), LocalTime.of(12, 0), 4, Modalidad.DOBLES);
			System.out.println("OK: usar padel con bebida caliente");
		} catch (ReglaNegocioException e) {
			System.out.println("Rechazado: " + e.getMessage());
		}
		carlos.terminarBebidaCaliente();
		padel.reservar(canchaPadel, carlos, HOY, LocalTime.of(11, 0), LocalTime.of(12, 0), 4, Modalidad.DOBLES);
		System.out.println("Reserva de padel permitida despues de terminar la bebida");
	}

	private static void guardarYCargar(Club club, String carpeta) throws IOException, ReglaNegocioException {
		ArchivoClub.guardar(club, carpeta);
		Club copia = ArchivoClub.cargar(carpeta);
		int partidosCompetencia = 0;
		for (Competencia competencia : copia.getCompetencias()) {
			for (NominaCompetencia nomina : competencia.getNominas()) {
				partidosCompetencia += nomina.getPartidos().size();
			}
		}
		System.out.println();
		System.out.println("=== Guardado JSON ===");
		System.out.println("Datos guardados en " + carpeta);
		System.out.println("Usuarios: " + copia.getUsuarios().size() + ", equipos: " + copia.getEquipos().size());
		System.out.println("Partidos del club: " + copia.getPartidos().size() + ", oficiales: " + partidosCompetencia);
		System.out.println("Ventas: " + copia.getTienda().getVentas().size() + ", turnos: " + copia.getTurnos().size());
	}
}
