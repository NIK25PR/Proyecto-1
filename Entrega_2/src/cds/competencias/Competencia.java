package cds.competencias;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cds.ReglaNegocioException;
import cds.conjunto.CategoriaEdad;
import cds.conjunto.DeporteConjunto;
import cds.conjunto.Equipo;
import cds.usuarios.Socio;

public abstract class Competencia {
	
	private String nombre;
	private String organizador;
	private int temporada;
	private DeporteConjunto deporte;
	private CategoriaEdad categoria;
	private int nominaMinima;
	private List<NominaCompetencia> nominas;
	
	
	protected Competencia (String nombre, String organizador,int temporada, DeporteConjunto deporte, CategoriaEdad categoria, int nominaMinima) {
		this.nombre = nombre;
		this.organizador = organizador;
		this.temporada = temporada;
		this.deporte = deporte;
		this.categoria = categoria;
		this.nominaMinima = nominaMinima;
		this.nominas = new ArrayList<NominaCompetencia>();
			
	}
	
	public NominaCompetencia inscribirEquipo(Equipo equipo, LocalDate fecha, List<Socio> jugadoresHabilitados) throws ReglaNegocioException{
		if (equipo.getDeporte()!= deporte || equipo.getCategoria() != categoria) {
			throw new ReglaNegocioException(equipo.getNombre() + "no pertenece al deporte/categoria de " + nombre);
		}
		if(jugadoresHabilitados.size()<nominaMinima) {
			throw new ReglaNegocioException("La nomina debe tener al menos " + nominaMinima + " jugaodres habilitados");
		}
		NominaCompetencia nomina = new NominaCompetencia(equipo, fecha, jugadoresHabilitados);
		nominas.add(nomina);
		return nomina;
	}
	
	public abstract void registrarResultado(PartidoCompetencia partido);
	
	public String getNombre() {
		return nombre;
		
	}
	
	public String getOrganizador() {
		return organizador;
		
	}
	
	public int getTemporada() {
		return temporada;
		
	}
	
	public DeporteConjunto getDeporte() {
		return deporte;
		
	}
	
	public CategoriaEdad getCategoria() {
		return categoria;
		
	}
	
	public int getNominaMinima() {
		return nominaMinima;
		
	}
	
	public List<NominaCompetencia> getNominas() {
		return new ArrayList<NominaCompetencia>(nominas);
	}
	

}
