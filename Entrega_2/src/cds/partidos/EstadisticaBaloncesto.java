package cds.partidos;

import cds.usuarios.Socio;

public class EstadisticaBaloncesto extends EstadisticaIndividual {
	
	private int puntos;
	private int rebotes;
	
	public EstadisticaBaloncesto(Socio jugador, int puntos, int rebotes) {
		super(jugador);
		this.puntos = puntos;
		this.rebotes = rebotes;
		
	}
	
	public int getPuntos() {
		return puntos;
		
	}
	
	public int getRebotes() {
		return rebotes;
	}
	

}
