package cds.competencias;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cds.conjunto.Equipo;
import cds.usuarios.Socio;

public class NominaCompetencia {
	
	private Equipo equipo;
	private LocalDate fechaInscripicion;
	private List<Socio> inscritos;
	
	NominaCompetencia(Equipo equipo, LocalDate fechaInscripcion, List<Socio> inscritos){
		this.equipo = equipo;
		this.fechaInscripicion = fechaInscripcion;
		this.inscritos = new ArrayList<Socio>(inscritos);
				
	}
	
	public boolean estaHabilitado(Socio socio) {
		return inscritos.contains(socio);
	}
	
	public Equipo getEquipo() {
		return equipo;
	}
	
	public LocalDate getFechaInscripcion() {
		return fechaInscripicion;
	}
	
	public List<Socio> getInscritos(){
		return new ArrayList<Socio>(inscritos);
	}

}
