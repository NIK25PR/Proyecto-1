package cds.consola;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cds.Club;
import cds.competencias.Liga;
import cds.competencias.NominaCompetencia;
import cds.competencias.PartidoCompetencia;
import cds.conjunto.CategoriaEdad;
import cds.conjunto.DeporteConjunto;
import cds.conjunto.Equipo;
import cds.conjunto.MiembroEquipo;
import cds.conjunto.PartidoInterno;
import cds.partidos.EstadisticaFutbol;
import cds.usuarios.Fisioterapeuta;
import cds.usuarios.Socio;

/**
 * Demostracion de los modulos 2 y 3: equipos, partidos internos, suspensiones,
 * restricciones medicas y liga oficial.
 */
public class DemoDeportes {
	private static final LocalDate HOY = LocalDate.of(2026, 10, 18);
	private static final String[] NOMBRES = { "Andres", "Bruno", "Camilo", "Diego", "Esteban", "Felipe", "Gabriel",
			"Hugo", "Ivan", "Jorge", "Kevin", "Luis", "Mateo", "Nicolas", "Oscar", "Pablo", "Quintin", "Ramiro" };

	private Club club;
	private final List<Socio> socios = new ArrayList<Socio>();
	private Socio juvenil;
	private Fisioterapeuta fisio;
	private Equipo rojo;
	private Equipo azul;
	private PartidoInterno partido1;
	private PartidoInterno partido3;
	private NominaCompetencia nomina;


	public static void main(String[] args) {
		new DemoDeportes().ejecutar();
	}

	public void ejecutar() {
		Pantalla.titulo("DEMO DEPORTES DE CONJUNTO Y COMPETENCIAS");
		crearSocios();
		crearEquipos();
		jugarPartidoInterno();
		demostrarSuspension();
		demostrarRestriccionMedica();
		demostrarBaloncesto();
		demostrarLiga();
		Pantalla.resumen();
	}


	private void crearSocios() {
		Pantalla.seccion("1. Socios del club");
		club = new Club();
		for (int i = 0; i < NOMBRES.length; i++) {
			socios.add(new Socio(NOMBRES[i].toLowerCase(), "123", NOMBRES[i], "D" + i, LocalDate.of(1999 + i % 5, 1 + i % 12, 10)));
		}
		juvenil = new Socio("tomas", "123", "Tomas (15 anos)", "D99", LocalDate.of(2011, 5, 20));
		fisio = new Fisioterapeuta("fisio", "123", "Paula Fisio", "FIS-01");
		Pantalla.esperarExito("Registrar " + socios.size() + " socios adultos, 1 juvenil y 1 fisioterapeuta", () -> {
			for (Socio s : socios) {
				club.registrarUsuario(s);
			}
			club.registrarUsuario(juvenil);
			club.registrarUsuario(fisio);
		});
		Pantalla.esperarRechazo("Registrar otro usuario con login repetido 'andres'",
				() -> club.registrarUsuario(new Socio("andres", "x", "Otro Andres", "D100", LocalDate.of(2000, 1, 1))));
	}

	private void crearEquipos() {
		Pantalla.seccion("2. Equipos de futbol (minimo 7 jugadores, categoria Mayores)");
		rojo = new Equipo("Seneca Rojo", DeporteConjunto.FUTBOL, CategoriaEdad.MAYORES, 8);
		azul = new Equipo("Seneca Azul", DeporteConjunto.FUTBOL, CategoriaEdad.MAYORES, 10);
		Pantalla.esperarExito("Crear equipos Seneca Rojo (cupo 8) y Seneca Azul (cupo 10)", () -> {
			club.registrarEquipo(rojo);
			club.registrarEquipo(azul);
		});
		Pantalla.esperarExito("Inscribir 8 jugadores en cada equipo", () -> {
			for (int i = 0; i < 8; i++) {
				club.inscribirJugador(rojo, socios.get(i), i == 0 ? "Portero" : "Campo", i + 1, HOY);
				club.inscribirJugador(azul, socios.get(i + 8), i == 0 ? "Portero" : "Campo", i + 1, HOY);
			}
		});
		mostrarPlantilla(rojo);
		mostrarPlantilla(azul);
		Pantalla.esperarRechazo("Inscribir un 9no jugador en Rojo (cupo 8)",
				() -> club.inscribirJugador(rojo, socios.get(16), "Campo", 20, HOY));
		Pantalla.esperarRechazo("Inscribir en Azul a alguien que ya juega futbol en Rojo",
				() -> club.inscribirJugador(azul, socios.get(0), "Campo", 30, HOY));
		Pantalla.esperarRechazo("Repetir el numero de camiseta 1 en Azul",
				() -> club.inscribirJugador(azul, socios.get(16), "Campo", 1, HOY));
		Pantalla.esperarRechazo("Inscribir a un socio de 15 anos en categoria Mayores",
				() -> club.inscribirJugador(azul, juvenil, "Campo", 40, HOY));
	}

	private void mostrarPlantilla(Equipo e) {
		StringBuilder sb = new StringBuilder();
		for (MiembroEquipo m : e.getMiembros()) {
			sb.append(m.getNumeroCamiseta()).append('-').append(m.getSocio().getNombre()).append("  ");
		}
		Pantalla.info(e.getNombre() + ": " + sb.toString().trim());
	}

	private void jugarPartidoInterno() {
		Pantalla.seccion("3. Partido interno de futbol con estadisticas y tarjetas");
		Pantalla.esperarExito("Programar Seneca Rojo vs Seneca Azul", () -> {
			partido1 = club.registrarPartidoInterno(rojo, azul, HOY);
			for (int i = 0; i < 7; i++) {
				partido1.agregarParticipante(socios.get(i));
				partido1.agregarParticipante(socios.get(i + 8));
			}
		});
		if (partido1 == null) {
			return;
		}
		Pantalla.esperarRechazo("Alinear en el partido a un socio que no es de ninguno de los dos equipos",
				() -> partido1.agregarParticipante(socios.get(17)));
		Pantalla.esperarExito("Registrar estadisticas: Andres 2 goles, Bruno roja, Ivan 1 gol, Jorge 2 amarillas", () -> {
			partido1.registrarEstadistica(new EstadisticaFutbol(socios.get(0), 2, 0, 0, 0));
			partido1.registrarEstadistica(new EstadisticaFutbol(socios.get(1), 0, 1, 0, 1));
			partido1.registrarEstadistica(new EstadisticaFutbol(socios.get(8), 1, 0, 0, 0));
			partido1.registrarEstadistica(new EstadisticaFutbol(socios.get(9), 0, 0, 2, 0));
		});
		Pantalla.esperarRechazo("Registrar dos veces las estadisticas de Andres",
				() -> partido1.registrarEstadistica(new EstadisticaFutbol(socios.get(0), 1, 0, 0, 0)));
		Pantalla.esperarExito("Finalizar el partido 2-1", () -> partido1.finalizar(2, 1));
		Pantalla.info("Marcador final: " + rojo.getNombre() + " " + partido1.getMarcadorLocal() + " - "
				+ partido1.getMarcadorVisitante() + " " + azul.getNombre());
		Pantalla.esperarRechazo("Cambiar el resultado de un partido ya jugado", () -> partido1.finalizar(3, 3));
	}

	private void demostrarSuspension() {
		Pantalla.seccion("4. Suspensiones: roja o 2 amarillas = 1 partido (RN-13)");
		Socio bruno = socios.get(1);
		Socio jorge = socios.get(9);
		Pantalla.verificar("Bruno (roja) queda suspendido en Rojo", rojo.estaSuspendido(bruno));
		Pantalla.verificar("Jorge (2 amarillas) queda suspendido en Azul", azul.estaSuspendido(jorge));
		Pantalla.verificar("Andres (sin tarjetas) no esta suspendido", !rojo.estaSuspendido(socios.get(0)));
		Pantalla.info("Jugadores habilitados -> Rojo: " + rojo.contarHabilitados() + ", Azul: " + azul.contarHabilitados());

		PartidoInterno[] p2 = new PartidoInterno[1];
		Pantalla.esperarExito("Programar el segundo partido (Rojo aun tiene 7 habilitados)", () -> {
			p2[0] = club.registrarPartidoInterno(rojo, azul, HOY.plusDays(7));
		});
		if (p2[0] == null) {
			return;
		}
		Pantalla.esperarRechazo("Alinear a Bruno estando suspendido", () -> p2[0].agregarParticipante(bruno));
		Pantalla.esperarExito("Jugar el segundo partido 0-0", () -> p2[0].finalizar(0, 0));
		Pantalla.verificar("Despues de cumplir la fecha, Bruno ya no esta suspendido", !rojo.estaSuspendido(bruno));
	}

	private void demostrarRestriccionMedica() {
		Pantalla.seccion("5. Restriccion medica del fisioterapeuta (HU-FIS-01)");
		Socio lucas = socios.get(11);
		Socio mateo = socios.get(12);
		club.registrarRestriccion(fisio, lucas, "Esguince de tobillo", HOY);
		club.registrarRestriccion(fisio, mateo, "Desgarro muscular", HOY);
		Pantalla.verificar("Luis y Mateo quedan inhabilitados", !lucas.estaHabilitado() && !mateo.estaHabilitado());
		Pantalla.info("Azul queda con " + azul.contarHabilitados() + " jugadores habilitados");
		Pantalla.esperarRechazo("Programar un partido de Azul con menos de 7 habilitados",
				() -> club.registrarPartidoInterno(rojo, azul, HOY.plusDays(14)));
		Pantalla.esperarExito("El fisioterapeuta da de alta a Luis",
				() -> lucas.getRestricciones().get(0).darDeAlta(HOY.plusDays(10)));
		Pantalla.esperarRechazo("Dar de alta dos veces la misma restriccion",
				() -> lucas.getRestricciones().get(0).darDeAlta(HOY.plusDays(11)));
		Pantalla.esperarExito("Ahora Azul tiene " + azul.contarHabilitados() + " habilitados y puede jugar", () -> {
			partido3 = club.registrarPartidoInterno(rojo, azul, HOY.plusDays(14));
		});
	}

	private void demostrarBaloncesto() {
		Pantalla.seccion("6. Baloncesto: minimo 5 jugadores y sin empates");
		Equipo leones = new Equipo("Leones", DeporteConjunto.BALONCESTO, CategoriaEdad.MAYORES, 12);
		Equipo tigres = new Equipo("Tigres", DeporteConjunto.BALONCESTO, CategoriaEdad.MAYORES, 12);
		Pantalla.esperarExito("Crear Leones y Tigres con 5 jugadores cada uno (pueden jugar tambien futbol)", () -> {
			club.registrarEquipo(leones);
			club.registrarEquipo(tigres);
			for (int i = 0; i < 5; i++) {
				club.inscribirJugador(leones, socios.get(i), "Base", i + 1, HOY);
				club.inscribirJugador(tigres, socios.get(i + 13), "Base", i + 1, HOY);
			}
		});
		Pantalla.esperarRechazo("Partido entre un equipo de futbol y uno de baloncesto",
				() -> club.registrarPartidoInterno(rojo, leones, HOY));
		PartidoInterno[] p = new PartidoInterno[1];
		Pantalla.esperarExito("Programar Leones vs Tigres", () -> p[0] = club.registrarPartidoInterno(leones, tigres, HOY));
		if (p[0] == null) {
			return;
		}
		Pantalla.esperarRechazo("Finalizar 80-80 (en baloncesto no hay empates)", () -> p[0].finalizar(80, 80));
		Pantalla.esperarExito("Finalizar 84-79", () -> p[0].finalizar(84, 79));
	}

	private void demostrarLiga() {
		Pantalla.seccion("7. Liga oficial: nomina y tabla de posiciones (RN-12)");
		Liga liga = new Liga("Liga Universitaria", "ASCUN", 2026, DeporteConjunto.FUTBOL, CategoriaEdad.MAYORES, 7, 3, 1);
		List<Socio> titulares = new ArrayList<Socio>();
		for (MiembroEquipo m : rojo.getMiembros()) {
			if (titulares.size() < 7 && rojo.puedeAlinear(m.getSocio())) {
				titulares.add(m.getSocio());
			}
		}
		Pantalla.esperarRechazo("Inscribir la nomina con solo 5 jugadores (minimo 7)",
				() -> liga.inscribirEquipo(rojo, HOY, titulares.subList(0, 5)));
		Pantalla.esperarExito("Inscribir a Seneca Rojo con 7 jugadores habilitados",
				() -> nomina = liga.inscribirEquipo(rojo, HOY, titulares));
		if (nomina == null) {
			return;
		}
		Pantalla.esperarRechazo("Inscribir a un equipo de baloncesto en una liga de futbol",
				() -> liga.inscribirEquipo(club.getEquipos().get(2), HOY, titulares));

		PartidoCompetencia p = new PartidoCompetencia(HOY.plusDays(3), nomina, "Universidad Andina",
				"Estadio Central", DeporteConjunto.FUTBOL);
		Pantalla.esperarRechazo("Alinear a un jugador que no esta en la nomina",
				() -> p.agregarParticipante(socios.get(7)));

		Socio lesionado = titulares.get(2);
		club.registrarRestriccion(fisio, lesionado, "Contusion", HOY);
		Pantalla.esperarRechazo("Alinear a " + lesionado.getNombre() + " con restriccion medica (HU-ENT-04)",
				() -> p.agregarParticipante(lesionado));

		Pantalla.esperarExito("Seneca Rojo gana 3-1 a Universidad Andina", () -> {
			p.agregarParticipante(titulares.get(0));
			p.finalizar(3, 1);
			liga.registrarResultado(p);
		});
		Pantalla.info("Tabla de posiciones:");
		for (Map.Entry<Equipo, Integer> fila : liga.getTablaDePosiciones().entrySet()) {
			Pantalla.info(String.format("   %-20s %3d pts", fila.getKey().getNombre(), fila.getValue()));
		}
		Integer puntos = liga.getTablaDePosiciones().get(rojo);
		Pantalla.verificar("Una victoria en futbol suma 3 puntos (tiene " + puntos + ")",
				puntos != null && puntos == 3);
	}

}
