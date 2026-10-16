package cds.partidos;

import cds.usuarios.Socio;

public class EstadisticaFutbol extends EstadisticaIndividual {
	private final int goles;
	private final int asistencias;
	private final int amarillas;
	private final int rojas;


	public EstadisticaFutbol(Socio jugador, int goles, int asistencias, int amarillas, int rojas) {
		super(jugador);
		this.goles = goles;
		this.asistencias = asistencias;
		this.amarillas = amarillas;
		this.rojas = rojas;
	}


	public boolean generaSuspension() {
		return amarillas >= 2 || rojas >= 1;
	}


	public int getGoles() {
		return goles;
	}

	public int getAsistencias() {
		return asistencias;
	}

	public int getAmarillas() {
		return amarillas;
	}

	public int getRojas() {
		return rojas;
	}

}
