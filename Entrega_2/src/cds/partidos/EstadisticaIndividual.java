package cds.partidos;

import cds.usuarios.Socio;

public abstract class EstadisticaIndividual {
	private final Socio jugador;


	protected EstadisticaIndividual(Socio jugador) {
		this.jugador = jugador;
	}


	public Socio getJugador() {
		return jugador;
	}

}
