package cds.academias;

import java.time.LocalDate;

public class RegistroRanking {
	
	private LocalDate fecha;
	private double puntaje;
	
	public RegistroRanking(LocalDate fecha, double puntaje) {
		this.fecha = fecha;
		this.puntaje = puntaje;
		
	}
	
	public LocalDate getFecha() {
		return fecha;
		
	}
	
	public double getPuntaje() {
		return puntaje;
	}

}
