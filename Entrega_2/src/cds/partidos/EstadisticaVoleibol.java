package cds.partidos;

import cds.usuarios.Socio;

public class EstadisticaVoleibol extends EstadisticaIndividual{
	
	private int aces;
	private int bloqueos;
	
	public EstadisticaVoleibol(Socio jugador, int aces, int bloqueos) {
		super(jugador);
		this.aces = aces;
		this.bloqueos = bloqueos;
		
	}
	
	public int getAces() {
		return aces;
		
	}
	
	public int getBloqueos() {
		return bloqueos;
	}

}
