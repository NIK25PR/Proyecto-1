package cds.competencias;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cds.conjunto.Equipo;
import cds.partidos.EstadisticaFutbol;
import cds.partidos.EstadisticaIndividual;
import cds.usuarios.Socio;

public class NominaCompetencia {
	
	private Competencia competencia;
	private Equipo equipo;
	private LocalDate fechaInscripicion;
	private List<Socio> inscritos;
	private List<PartidoCompetencia> partidos;
	
	NominaCompetencia(Competencia competencia, Equipo equipo, LocalDate fechaInscripcion, List<Socio> inscritos){
		this.competencia = competencia;
		this.partidos = new ArrayList<PartidoCompetencia>();
		this.equipo = equipo;
		this.fechaInscripicion = fechaInscripcion;
		this.inscritos = new ArrayList<Socio>(inscritos);
				
	}
	
	public boolean estaHabilitado(Socio socio) {
		return inscritos.contains(socio);
	}
	
	void agregarPartido(PartidoCompetencia partido) {
		partidos.add(partido);
	}
	
	int amarillasAcumuladas(Socio socio) {
		int total = 0;
		for (PartidoCompetencia p : partidos) {
			if (p.isJugado()) {
				for (EstadisticaIndividual e : p.getEstadisticas()) {
					if (e.getJugador() == socio && e instanceof EstadisticaFutbol) {
						total += ((EstadisticaFutbol) e).getAmarillas();
					}
				}
			}
		}
		return total;
	}
	
	public Competencia getCompetencia() {
		return competencia;
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

	public List<PartidoCompetencia> getPartidos() {
		return new ArrayList<PartidoCompetencia>(partidos);
	}

}
