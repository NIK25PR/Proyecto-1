package cds.entrenamiento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import cds.ReglaNegocioException;
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
	
	public Asistencia convocar(Socio socio) throws ReglaNegocioException {
		if (equipo == null && academia == null) {
			throw new ReglaNegocioException("La sesion debe pertenecer a un equipo o una academia");
		}
		if (equipo != null && academia != null) {
			throw new ReglaNegocioException("La sesion debe pertenecer a un equipo o una academia, no a ambos");
		}
		if (equipo != null && !equipo.tieneJugador(socio)) {
			throw new ReglaNegocioException(socio.getNombre() + " no pertenece al equipo de esta sesion");
		}
		if (academia != null && academia.getInscripcion(socio) == null) {
			throw new ReglaNegocioException(socio.getNombre() + " no esta inscrito en la academia de esta sesion");
		}
		if (!socio.estaHabilitado()) {
			throw new ReglaNegocioException(socio.getNombre() + " no esta habilitado para entrenar");
		}
		if (instalacion != null && socio.tieneBebidaCaliente() && instalacion.restringeBebidaCaliente()) {
			throw new ReglaNegocioException("No se puede usar " + instalacion.getNombre() + " con una bebida caliente");
		}
		for (Asistencia a : convocados) {
			if (a.getSocio() == socio) {
				throw new ReglaNegocioException(socio.getNombre() + " ya esta convocado a esta sesion");
			}
		}
		Asistencia asistencia = new Asistencia(socio);
		convocados.add(asistencia);
		return asistencia;
	}

	public Asistencia restaurarConvocado(Socio socio) {
		Asistencia asistencia = new Asistencia(socio);
		convocados.add(asistencia);
		return asistencia;
	}
	
	public void finalizar() {
		finalizada = true;
		
	}
	
	public LocalDate getFecha() {
		return fecha;
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
