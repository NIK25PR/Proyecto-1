package cds.partidos;

import cds.usuarios.Socio;

public class EstadisticaRaqueta extends EstadisticaIndividual{
	
	private int setsGanados;
	private int gamesGanados;
	private int aces;
	private int erroresNoForzados;
	
	public EstadisticaRaqueta(Socio jugador, int gamesGanados, int setsGanados, int aces, int erroresNoForzados) {
		super(jugador);
		this.setsGanados = setsGanados;
		this.gamesGanados = gamesGanados;
		this.aces = aces;
		this.erroresNoForzados = erroresNoForzados;
		
	}
	
	public int getSetsGanados() {
		return setsGanados;
		
	}
	
	public int getGamesGanados() {
		return gamesGanados;
		
	}
	
	public int getAces() {
		return aces;
		
	}
	
	public int getErroresNoForzados() {
		return erroresNoForzados;
	}

}
