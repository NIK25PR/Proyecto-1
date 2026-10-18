package cds.consola;

import java.time.LocalDate;
import java.time.LocalTime;

import cds.Club;
import cds.academias.Academia;
import cds.academias.Disciplina;
import cds.academias.InscripcionAcademia;
import cds.academias.Instalacion;
import cds.academias.Modalidad;
import cds.academias.Nivel;
import cds.academias.PartidoPractica;
import cds.academias.TipoInstalacion;
import cds.entrenamiento.Asistencia;
import cds.entrenamiento.EstadoAsistencia;
import cds.entrenamiento.SesionEntrenamiento;
import cds.usuarios.Entrenador;
import cds.usuarios.Fisioterapeuta;
import cds.usuarios.Socio;

/**
 * Demostracion de los modulos 4 y 5: academias, reservas, partidos de practica,
 * ranking y sesiones de entrenamiento.
 */
public class DemoAcademias {
	private static final LocalDate HOY = LocalDate.of(2026, 10, 18);

	private Club club;
	private Academia academia;
	private Instalacion cancha1;
	private Instalacion cancha2;
	private Instalacion cancha3;
	private Socio ana;
	private Socio beto;
	private Socio carla;
	private Socio dario;
	private Socio externo;
	private Socio fer;
	private Entrenador leo;
	private Fisioterapeuta fisio;


	public static void main(String[] args) {
		new DemoAcademias().ejecutar();
	}

	public void ejecutar() {
		Pantalla.titulo("DEMO ACADEMIAS Y ENTRENAMIENTO");
		crearAcademia();
		demostrarReservas();
		demostrarPartidoPractica();
		demostrarEntrenamiento();
		Pantalla.resumen();
	}


	private void crearAcademia() {
		Pantalla.seccion("1. Academia de tenis e inscripciones por nivel");
		club = new Club();
		ana = new Socio("ana", "123", "Ana", "1", LocalDate.of(2001, 3, 14));
		beto = new Socio("beto", "123", "Beto", "2", LocalDate.of(1999, 8, 1));
		carla = new Socio("carla", "123", "Carla", "3", LocalDate.of(2000, 11, 23));
		dario = new Socio("dario", "123", "Dario", "4", LocalDate.of(1997, 2, 5));
		externo = new Socio("eva", "123", "Eva (no inscrita)", "5", LocalDate.of(2002, 6, 30));
		fer = new Socio("fer", "123", "Fer", "6", LocalDate.of(2000, 4, 9));
		leo = new Entrenador("leo", "123", "Leonardo", "LEO-CDS");
		fisio = new Fisioterapeuta("fisio", "123", "Paula", "FIS-01");
		Pantalla.esperarExito("Registrar 6 socios, 1 entrenador y 1 fisioterapeuta", () -> {
			club.registrarUsuario(ana);
			club.registrarUsuario(beto);
			club.registrarUsuario(carla);
			club.registrarUsuario(dario);
			club.registrarUsuario(externo);
			club.registrarUsuario(fer);
			club.registrarUsuario(leo);
			club.registrarUsuario(fisio);
		});

		academia = new Academia("Academia de Tenis", Disciplina.TENIS, 2);
		cancha1 = new Instalacion("Cancha 1", TipoInstalacion.CANCHA_TENIS, false);
		cancha2 = new Instalacion("Cancha 2", TipoInstalacion.CANCHA_TENIS, false);
		cancha3 = new Instalacion("Cancha 3", TipoInstalacion.CANCHA_TENIS, true);
		academia.agregarInstalacion(cancha1);
		academia.agregarInstalacion(cancha2);
		academia.agregarInstalacion(cancha3);
		Pantalla.info("Academia de Tenis: 3 canchas, maximo 2 en uso al mismo tiempo");

		Pantalla.esperarExito("Inscribir Ana (principiante), Beto y Fer (intermedios), Carla y Dario (avanzados)", () -> {
			academia.inscribirSocio(ana, Nivel.PRINCIPIANTE, HOY);
			academia.inscribirSocio(beto, Nivel.INTERMEDIO, HOY);
			academia.inscribirSocio(fer, Nivel.INTERMEDIO, HOY);
			academia.inscribirSocio(carla, Nivel.AVANZADO, HOY);
			academia.inscribirSocio(dario, Nivel.AVANZADO, HOY);
		});
		Pantalla.esperarRechazo("Inscribir otra vez a Ana",
				() -> academia.inscribirSocio(ana, Nivel.INTERMEDIO, HOY));
		mostrarRanking();
	}

	private void mostrarRanking() {
		for (InscripcionAcademia i : academia.getInscripciones()) {
			Pantalla.info(String.format("   %-8s %-13s ranking %7.1f", i.getSocio().getNombre(), i.getNivel(),
					i.getPuntajeRanking()));
		}
	}

	private void demostrarReservas() {
		Pantalla.seccion("2. Reservas de instalaciones (capacidad y cupo simultaneo)");
		LocalTime ocho = LocalTime.of(8, 0);
		LocalTime nueve = LocalTime.of(9, 0);
		Pantalla.esperarExito("Ana reserva Cancha 1 de 8:00 a 9:00 para 2 jugadores (sencillos)",
				() -> academia.reservar(cancha1, ana, HOY, ocho, nueve, 2, Modalidad.SENCILLOS));
		Pantalla.esperarExito("Beto reserva Cancha 2 a la misma hora para dobles (4 jugadores)",
				() -> academia.reservar(cancha2, beto, HOY, ocho, nueve, 4, Modalidad.DOBLES));
		Pantalla.esperarRechazo("Carla reserva Cancha 1 a la misma hora (ya esta ocupada)",
				() -> academia.reservar(cancha1, carla, HOY, LocalTime.of(8, 30), LocalTime.of(9, 30), 2, Modalidad.SENCILLOS));
		Pantalla.esperarRechazo("Dario reserva Cancha 3 a esa hora (la academia ya usa 2 canchas)",
				() -> academia.reservar(cancha3, dario, HOY, ocho, nueve, 2, Modalidad.SENCILLOS));
		Pantalla.esperarRechazo("Reserva de sencillos para 3 jugadores (maximo 2)",
				() -> academia.reservar(cancha3, dario, HOY, LocalTime.of(10, 0), LocalTime.of(11, 0), 3, Modalidad.SENCILLOS));
		Pantalla.esperarRechazo("Reserva que termina antes de empezar",
				() -> academia.reservar(cancha3, dario, HOY, LocalTime.of(12, 0), LocalTime.of(11, 0), 2, Modalidad.SENCILLOS));
		Pantalla.info("Reservas registradas: " + academia.getReservas().size());
	}

	private void demostrarPartidoPractica() {
		Pantalla.seccion("3. Partido de practica: niveles compatibles y ranking ELO");
		PartidoPractica p = new PartidoPractica(HOY, academia, Modalidad.SENCILLOS);
		Pantalla.esperarExito("Ana (principiante) vs Beto (intermedio): niveles compatibles", () -> {
			p.agregarParticipante(ana);
			p.agregarParticipante(beto);
		});
		PartidoPractica dispar = new PartidoPractica(HOY, academia, Modalidad.SENCILLOS);
		Pantalla.esperarRechazo("Ana (principiante) vs Carla (avanzada): diferencia de 2 niveles", () -> {
			dispar.agregarParticipante(ana);
			dispar.agregarParticipante(carla);
		});
		Pantalla.esperarRechazo("Agregar a Eva, que no esta inscrita en la academia",
				() -> new PartidoPractica(HOY, academia, Modalidad.SENCILLOS).agregarParticipante(externo));
		Pantalla.esperarRechazo("Agregar a Fer como tercer jugador en un partido de sencillos",
				() -> p.agregarParticipante(fer));

		double antesAna = academia.getInscripcion(ana).getPuntajeRanking();
		double antesBeto = academia.getInscripcion(beto).getPuntajeRanking();
		Pantalla.esperarRechazo("Finalizar 1-1 (en tenis no hay empates)", () -> p.finalizar(1, 1));
		Pantalla.esperarExito("Ana gana 2-0", () -> p.finalizar(2, 0));
		mostrarRanking();
		Pantalla.verificar("El ranking de Ana sube despues de ganar (antes " + antesAna + ")",
				academia.getInscripcion(ana).getPuntajeRanking() > antesAna);
		Pantalla.verificar("El ranking de Beto baja despues de perder (antes " + antesBeto + ")",
				academia.getInscripcion(beto).getPuntajeRanking() < antesBeto);
	}

	private void demostrarEntrenamiento() {
		Pantalla.seccion("4. Sesion de entrenamiento: asistencia y evaluacion (HU-ENT-01)");
		SesionEntrenamiento sesion = new SesionEntrenamiento(HOY, LocalTime.of(16, 0), 90, leo, cancha1, null, academia);
		Asistencia[] a = new Asistencia[3];
		Pantalla.esperarExito("Leo convoca a Ana, Beto y Carla", () -> {
			a[0] = sesion.convocar(ana);
			a[1] = sesion.convocar(beto);
			a[2] = sesion.convocar(carla);
		});
		club.registrarRestriccion(fisio, dario, "Tendinitis", HOY);
		Pantalla.esperarRechazo("Convocar a Dario, que tiene restriccion medica", () -> sesion.convocar(dario));
		Pantalla.esperarRechazo("Convocar dos veces a Ana", () -> sesion.convocar(ana));

		Pantalla.esperarExito("Finalizar la sesion y registrar asistencia", () -> {
			sesion.finzalizar();
			a[0].registrarEstado(EstadoAsistencia.ASISTIO);
			a[1].registrarEstado(EstadoAsistencia.LLEGO_TARDE);
			a[2].registrarEstado(EstadoAsistencia.FALTO);
		});
		Pantalla.esperarExito("Calificar a Ana con 5 y a Beto con 3", () -> {
			a[0].calificar(5, "Excelente reves");
			a[1].calificar(3, "Debe mejorar el saque");
		});
		Pantalla.esperarRechazo("Calificar con nota 8 (la escala es de 1 a 5)", () -> a[0].calificar(8, "fuera de rango"));

		int presentes = 0;
		for (Asistencia x : sesion.getConvocados()) {
			boolean vino = x.getEstado() == EstadoAsistencia.ASISTIO || x.getEstado() == EstadoAsistencia.LLEGO_TARDE;
			if (vino) {
				presentes++;
			}
			Pantalla.info(String.format("   %-6s %-12s nota: %s", x.getSocio().getNombre(), x.getEstado(),
					x.getEvaluacion() == null ? "-" : String.valueOf(x.getEvaluacion().getNota())));
		}
		Pantalla.info("Asistencia: " + presentes + " de " + sesion.getConvocados().size() + " ("
				+ (100 * presentes / Math.max(1, sesion.getConvocados().size())) + "%)");
	}

}
