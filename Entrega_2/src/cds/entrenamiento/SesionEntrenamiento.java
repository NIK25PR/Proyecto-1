package cds.entrenamiento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import cds.academias.Academia;
import cds.academias.Instalacion;
import cds.conjunto.Equipo;
import cds.usuarios.Entrenador;
import cds.usuarios.Socio;

public class SesionEntrenamiento {
	
	private LocalDate fecha;
	private LocalTime hora;
	private int duracionMinutos;
	private boolean finalizada;
	private Entrenador responsable;
	private Instalacion instalacion;
	private Equipo equipo;
	private Academia academia;
	private List<Asistencia> convocados;
	
	public SesionEntrenamiento (LocalDate fecha, LocalTime hora, int duracionMinutos, Entrenador responsable,
			Instalacion instalacion, Equipo equipo, Academia academia) {
		this.fecha = fecha;
		this.hora = hora;
		this.duracionMinutos = duracionMinutos;
		this.finalizada = false;
		this.responsable = responsable;
		this.instalacion = instalacion;
		this.equipo = equipo;
		this.academia = academia;
		this.convocados = new ArrayList<Asistencia>();
		
	}
	
	public Asistencia convocar(Socio socio) {
		Asistencia asistencia = new Asistencia(socio);
		convocados.add(asistencia);
		return asistencia;
	}
	
	public void finzalizar() {
		finalizada = true;
		
	}
	
	public LocalTime getHora() {
		return hora;
		
	}
	
	public int getDuracionMinutos() {
		return duracionMinutos;
		
	}
	
	public boolean isFinalizado() {
		return finalizada;
		
	}
	
	public Entrenador getResponsable() {
		return responsable;
		
	}
	
	public Instalacion getInstalacion() {
		return instalacion;
		
	}
	
	public Equipo getEquipo() {
		return equipo;
		
	}
	
	public Academia getAcademia() {
		return academia;
		
	}
	
	public List<Asistencia> getConvocados(){
		return new ArrayList<Asistencia>(convocados);
	}

}
