package cds.conjunto;

import java.time.LocalDate;

import cds.ReglaNegocioException;
import cds.partidos.EstadisticaFutbol;
import cds.partidos.EstadisticaIndividual;
import cds.partidos.Partido;
import cds.partidos.Suspension;
import cds.usuarios.Socio;

public class PartidoInterno extends Partido {
	private final Equipo local;
	private final Equipo visitante;


	public PartidoInterno(Equipo local, Equipo visitante, LocalDate fecha) throws ReglaNegocioException {
		this(local, visitante, fecha, true);
	}

	private PartidoInterno(Equipo local, Equipo visitante, LocalDate fecha, boolean validar) throws ReglaNegocioException {
		super(fecha);
		this.local = local;
		this.visitante = visitante;
		if (!validar) {
			return;
		}
		if (local == visitante) {
			throw new ReglaNegocioException("Un equipo no puede jugar contra si mismo");
		}
		if (!local.isActivo() || !visitante.isActivo()) {
			throw new ReglaNegocioException("Los dos equipos deben estar activos");
		}
		if (local.getDeporte() != visitante.getDeporte()) {
			throw new ReglaNegocioException(local.getNombre() + " y " + visitante.getNombre() + " no son del mismo deporte");
		}
		if (local.getCategoria() != visitante.getCategoria()) {
			throw new ReglaNegocioException(local.getNombre() + " y " + visitante.getNombre() + " no son de la misma categoria");
		}
		validarMinimo(local);
		validarMinimo(visitante);
	}

	public static PartidoInterno restaurar(Equipo local, Equipo visitante, LocalDate fecha) throws ReglaNegocioException {
		return new PartidoInterno(local, visitante, fecha, false);
	}


	private void validarMinimo(Equipo e) throws ReglaNegocioException {
		int minimo = e.getDeporte().getMinJugadores();
		int habilitados = e.contarHabilitados();
		if (habilitados < minimo) {
			throw new ReglaNegocioException(e.getNombre() + " tiene " + habilitados + " jugadores habilitados y "
					+ e.getDeporte().getNombre() + " exige " + minimo);
		}
	}

	protected void validarParticipante(Socio s) throws ReglaNegocioException {
		if (local.puedeAlinear(s) || visitante.puedeAlinear(s)) {
			return;
		}
		Equipo suyo = null;
		if (local.tieneJugador(s)) {
			suyo = local;
		} else if (visitante.tieneJugador(s)) {
			suyo = visitante;
		}
		if (suyo == null) {
			throw new ReglaNegocioException(s.getNombre() + " no juega en " + local.getNombre() + " ni en " + visitante.getNombre());
		}
		if (suyo.estaSuspendido(s)) {
			throw new ReglaNegocioException(s.getNombre() + " tiene una suspension pendiente en " + suyo.getNombre());
		}
		throw new ReglaNegocioException(s.getNombre() + " no esta habilitado para jugar");
	}

	protected boolean permiteEmpate() {
		return local.getDeporte().permiteEmpate();
	}

	protected void alFinalizar() {
		local.cumplirSuspensiones();
		visitante.cumplirSuspensiones();
		for (EstadisticaIndividual e : getEstadisticas()) {
			if (e instanceof EstadisticaFutbol) {
				EstadisticaFutbol f = (EstadisticaFutbol) e;
				if (f.generaSuspension()) {
					String motivo = "2 amarillas";
					if (f.getRojas() >= 1) {
						motivo = "tarjeta roja";
					}
					Equipo suyo = visitante;
					if (local.tieneJugador(f.getJugador())) {
						suyo = local;
					}
					suyo.agregarSuspension(new Suspension(f.getJugador(), motivo, this));
				}
			}
		}
	}


	public Equipo getLocal() {
		return local;
	}

	public Equipo getVisitante() {
		return visitante;
	}

}
