package cds;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import cds.academias.Academia;
import cds.academias.InscripcionAcademia;
import cds.academias.RegistroRanking;
import cds.competencias.Competencia;
import cds.entrenamiento.Asistencia;
import cds.entrenamiento.EstadoAsistencia;
import cds.entrenamiento.SesionEntrenamiento;
import cds.usuarios.Administrador;
import cds.usuarios.Empleado;
import cds.usuarios.Entrenador;
import cds.usuarios.EstadoSolicitud;
import cds.usuarios.Fisioterapeuta;
import cds.usuarios.RestriccionMedica;
import cds.usuarios.SolicitudCambioTurno;
import cds.usuarios.Socio;
import cds.usuarios.Turno;
import cds.usuarios.Usuario;
import cds.conjunto.DeporteConjunto;
import cds.conjunto.Equipo;
import cds.conjunto.PartidoInterno;
import cds.partidos.Partido;
import cds.ventas.Mensualidad;
import cds.ventas.Rubro;
import cds.ventas.TiendaCafeteria;
import cds.ventas.Venta;

public class Club {
	private final List<Usuario> usuarios;
	private final List<Equipo> equipos;
	private final List<Partido> partidos;
	private final TiendaCafeteria tienda;
	private final List<Academia> academias;
	private final List<Competencia> competencias;
	private final List<SesionEntrenamiento> sesiones;
	private final List<Turno> turnos;
	private final List<SolicitudCambioTurno> solicitudes;

	public static final LocalTime APERTURA = LocalTime.of(6, 0);
	public static final LocalTime CIERRE = LocalTime.of(22, 0);

	public Club() {
		usuarios = new ArrayList<Usuario>();
		equipos = new ArrayList<Equipo>();
		partidos = new ArrayList<Partido>();
		tienda = new TiendaCafeteria();
		academias = new ArrayList<Academia>();
		competencias = new ArrayList<Competencia>();
		sesiones = new ArrayList<SesionEntrenamiento>();
		turnos = new ArrayList<Turno>();
		solicitudes = new ArrayList<SolicitudCambioTurno>();
	}


	
	public void registrarUsuario(Usuario u) throws ReglaNegocioException {
		if (buscarUsuario(u.getLogin()) != null) {
			throw new ReglaNegocioException("Ya existe un usuario con el login " + u.getLogin());
		}
		usuarios.add(u);
	}

	public Usuario buscarUsuario(String login) {
		for (Usuario u : usuarios) {
			if (u.getLogin().equals(login)) {
				return u;
			}
		}
		return null;
	}

	
	
	public Usuario autenticar(String login, String password) throws ReglaNegocioException {
		Usuario u = buscarUsuario(login);
		if (u == null || !u.verificarPassword(password)) {
			throw new ReglaNegocioException("Login o password incorrectos");
		}
		return u;
	}

	public void registrarEquipo(Equipo e) throws ReglaNegocioException {
		for (Equipo actual : equipos) {
			if (actual.getNombre().equals(e.getNombre())) {
				throw new ReglaNegocioException("Ya existe un equipo llamado " + e.getNombre());
			}
		}
		equipos.add(e);
	}

	public Equipo buscarEquipo(String nombre) {
		for (Equipo e : equipos) {
			if (e.getNombre().equals(nombre)) {
				return e;
			}
		}
		return null;
	}

	public void registrarPartido(Partido p) {
		partidos.add(p);
	}

	public void registrarAcademia(Academia a) throws ReglaNegocioException {
		if (buscarAcademia(a.getNombre()) != null) {
			throw new ReglaNegocioException("Ya existe la academia " + a.getNombre());
		}
		academias.add(a);
	}

	public Academia buscarAcademia(String nombre) {
		for (Academia a : academias) {
			if (a.getNombre().equals(nombre)) {
				return a;
			}
		}
		return null;
	}

	public void registrarCompetencia(Competencia c) throws ReglaNegocioException {
		if (buscarCompetencia(c.getNombre()) != null) {
			throw new ReglaNegocioException("Ya existe la competencia " + c.getNombre());
		}
		competencias.add(c);
	}

	public Competencia buscarCompetencia(String nombre) {
		for (Competencia c : competencias) {
			if (c.getNombre().equals(nombre)) {
				return c;
			}
		}
		return null;
	}

	public void registrarSesion(SesionEntrenamiento s) {
		sesiones.add(s);
	}

	public void registrarTurno(Turno t) {
		turnos.add(t);
	}

	public SolicitudCambioTurno solicitarCambioTurno(Empleado solicitante, Turno turno, DayOfWeek nuevoDia,
			LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) throws ReglaNegocioException {
		if (turno.getEmpleado() != solicitante) {
			throw new ReglaNegocioException("Solo se puede pedir el cambio de un turno propio");
		}
		if (!nuevaHoraFin.isAfter(nuevaHoraInicio)) {
			throw new ReglaNegocioException("La hora de fin debe ser posterior a la hora de inicio");
		}
		SolicitudCambioTurno s = new SolicitudCambioTurno(solicitante, turno, nuevoDia, nuevaHoraInicio, nuevaHoraFin);
		solicitudes.add(s);
		return s;
	}

	public void resolverSolicitud(Administrador admin, SolicitudCambioTurno s, boolean aprobar) throws ReglaNegocioException {
		if (s.getEstado() != EstadoSolicitud.PENDIENTE) {
			throw new ReglaNegocioException("La solicitud ya fue resuelta: " + s.getEstado());
		}
		if (!aprobar) {
			s.resolver(admin, EstadoSolicitud.RECHAZADA);
			return;
		}
		Turno t = s.getTurno();
		DayOfWeek diaViejo = t.getDia();
		LocalTime inicioViejo = t.getHoraInicio();
		LocalTime finViejo = t.getHoraFin();
		t.cambiar(s.getNuevoDia(), s.getNuevaHoraInicio(), s.getNuevaHoraFin());
		String falta = horaSinCobertura(diaViejo);
		if (falta == null) {
			falta = horaSinCobertura(s.getNuevoDia());
		}
		if (falta != null) {
			t.cambiar(diaViejo, inicioViejo, finViejo);
			s.resolver(admin, EstadoSolicitud.RECHAZADA);
			throw new ReglaNegocioException("El cambio deja sin cobertura (2 entrenadores y 1 fisioterapeuta) el " + falta);
		}
		s.resolver(admin, EstadoSolicitud.APROBADA);
	}

	private String horaSinCobertura(DayOfWeek dia) {
		for (LocalTime h = APERTURA; h.isBefore(CIERRE); h = h.plusHours(1)) {
			int entrenadores = 0;
			int fisios = 0;
			for (Turno t : turnos) {
				if (t.cubre(dia, h)) {
					if (t.getEmpleado() instanceof Entrenador) {
						entrenadores++;
					} else if (t.getEmpleado() instanceof Fisioterapeuta) {
						fisios++;
					}
				}
			}
			if (entrenadores < 2 || fisios < 1) {
				return dia + " a las " + h;
			}
		}
		return null;
	}

	public double totalVentas(Rubro rubro, LocalDate desde, LocalDate hasta) {
		double total = 0;
		for (Venta v : tienda.getVentas()) {
			if (v.getRubro() == rubro && v.isConfirmada() && !v.getFecha().isBefore(desde) && !v.getFecha().isAfter(hasta)) {
				total += v.total();
			}
		}
		if (rubro == Rubro.MENSUALIDAD) {
			for (Mensualidad m : tienda.getMensualidades()) {
				if (m.isPagada() && !m.getFechaPago().isBefore(desde) && !m.getFechaPago().isAfter(hasta)) {
					total += m.getMonto();
				}
			}
		}
		return total;
	}

	public int convocatorias(Socio socio) {
		int total = 0;
		for (SesionEntrenamiento s : sesiones) {
			if (s.isFinalizado()) {
				for (Asistencia a : s.getConvocados()) {
					if (a.getSocio() == socio) {
						total++;
					}
				}
			}
		}
		return total;
	}

	public int asistencias(Socio socio) {
		int total = 0;
		for (SesionEntrenamiento s : sesiones) {
			if (s.isFinalizado()) {
				for (Asistencia a : s.getConvocados()) {
					if (a.getSocio() == socio && (a.getEstado() == EstadoAsistencia.ASISTIO || a.getEstado() == EstadoAsistencia.LLEGO_TARDE)) {
						total++;
					}
				}
			}
		}
		return total;
	}

	public List<Partido> partidosJugados() {
		List<Partido> jugados = new ArrayList<Partido>();
		for (Partido p : partidos) {
			if (p.isJugado()) {
				jugados.add(p);
			}
		}
		return jugados;
	}

	public List<RegistroRanking> evolucionRanking(Academia academia, Socio socio) {
		InscripcionAcademia i = academia.getInscripcion(socio);
		if (i == null) {
			return new ArrayList<RegistroRanking>();
		}
		return i.getHistorial();
	}

	public Equipo buscarEquipoDe(Socio s, DeporteConjunto d) {
		for (Equipo e : equipos) {
			if (e.getDeporte() == d && e.tieneJugador(s)) {
				return e;
			}
		}
		return null;
	}

	public void inscribirJugador(Equipo e, Socio s, String posicion, int numero, LocalDate hoy) throws ReglaNegocioException {
		Equipo actual = buscarEquipoDe(s, e.getDeporte());
		if (actual != null) {
			throw new ReglaNegocioException(s.getNombre() + " ya juega " + e.getDeporte().getNombre() + " en " + actual.getNombre());
		}
		e.agregarJugador(s, posicion, numero, hoy);
	}

	public PartidoInterno registrarPartidoInterno(Equipo local, Equipo visitante, LocalDate fecha) throws ReglaNegocioException {
		PartidoInterno p = new PartidoInterno(local, visitante, fecha);
		partidos.add(p);
		return p;
	}

	public void registrarRestriccion(Fisioterapeuta fisio, Socio s, String motivo, LocalDate fecha) {
		s.agregarRestriccion(new RestriccionMedica(fisio, motivo, fecha));
	}


	public List<Usuario> getUsuarios() {
		return usuarios;
	}
	

	public List<Equipo> getEquipos() {
		return equipos;
	}

	public List<Partido> getPartidos() {
		return partidos;
	}

	public TiendaCafeteria getTienda() {
		return tienda;
	}

	public List<Academia> getAcademias() {
		return academias;
	}

	public List<Competencia> getCompetencias() {
		return competencias;
	}

	public List<SesionEntrenamiento> getSesiones() {
		return sesiones;
	}

	public List<Turno> getTurnos() {
		return turnos;
	}

	public List<SolicitudCambioTurno> getSolicitudes() {
		return solicitudes;
	}

}
