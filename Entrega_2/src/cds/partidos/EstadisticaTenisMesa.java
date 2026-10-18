package cds.partidos;

import cds.usuarios.Socio;

public class EstadisticaTenisMesa extends EstadisticaIndividual{
	
	private int setsGanados;
	private int puntosTotales;
	
	public EstadisticaTenisMesa(Socio jugador, int setsGanados, int puntosTotales) {
		super(jugador);
		this.setsGanados = setsGanados;
		this.puntosTotales = puntosTotales;
		
	}
	
	public int getSetsGanados() {
		return setsGanados;
		
	}
	
	public int getPuntosTotales() {
		return puntosTotales;
	}

}
