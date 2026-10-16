package cds.partidos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cds.ReglaNegocioException;
import cds.usuarios.Socio;

public abstract class Partido {
	private final LocalDate fecha;
	private int marcadorLocal;
	private int marcadorVisitante;
	private boolean jugado;
	private final List<Socio> participantes;
	private final List<EstadisticaIndividual> estadisticas;


	protected Partido(LocalDate fecha) {
		this.fecha = fecha;
		jugado = false;
		participantes = new ArrayList<Socio>();
		estadisticas = new ArrayList<EstadisticaIndividual>();
	}


	public void agregarParticipante(Socio s) throws ReglaNegocioException {
		if (jugado) {
			throw new ReglaNegocioException("El partido ya se jugo");
		}
		if (participantes.contains(s)) {
			throw new ReglaNegocioException(s.getNombre() + " ya esta en el partido");
		}
		validarParticipante(s);
		participantes.add(s);
	}

	public void registrarEstadistica(EstadisticaIndividual e) throws ReglaNegocioException {
		if (!participantes.contains(e.getJugador())) {
			throw new ReglaNegocioException(e.getJugador().getNombre() + " no participo en el partido");
		}
		for (EstadisticaIndividual actual : estadisticas) {
			if (actual.getJugador() == e.getJugador()) {
				throw new ReglaNegocioException(e.getJugador().getNombre() + " ya tiene estadisticas en este partido");
			}
		}
		estadisticas.add(e);
	}

	public void finalizar(int local, int visitante) throws ReglaNegocioException {
		if (jugado) {
			throw new ReglaNegocioException("El partido ya se jugo");
		}
		if (local == visitante && !permiteEmpate()) {
			throw new ReglaNegocioException("En este deporte no hay empates");
		}
		marcadorLocal = local;
		marcadorVisitante = visitante;
		jugado = true;
		alFinalizar();
	}


	protected abstract void validarParticipante(Socio s) throws ReglaNegocioException;

	protected abstract boolean permiteEmpate();

	protected void alFinalizar() {
	}


	public LocalDate getFecha() {
		return fecha;
	}

	public int getMarcadorLocal() {
		return marcadorLocal;
	}

	public int getMarcadorVisitante() {
		return marcadorVisitante;
	}

	public boolean isJugado() {
		return jugado;
	}

	public List<Socio> getParticipantes() {
		return Collections.unmodifiableList(participantes);
	}

	public List<EstadisticaIndividual> getEstadisticas() {
		return Collections.unmodifiableList(estadisticas);
	}

}
