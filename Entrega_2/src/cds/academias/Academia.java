package cds.academias;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import cds.ReglaNegocioException;
import cds.usuarios.Socio;

public class Academia {
	
	private String nombre;
	private Disciplina disciplina;
	private int maxInstalacionesSimultaneas;
	private List<InscripcionAcademia> inscripciones;
	private List<Instalacion> instalaciones;
	private List<Reserva> reservas;
	
	public Academia(String nombre, Disciplina disciplina, int maxInstalacionesSimultaneas) {
		this.nombre = nombre;
		this.disciplina = disciplina;
		this.maxInstalacionesSimultaneas = maxInstalacionesSimultaneas;
		this.inscripciones = new ArrayList<InscripcionAcademia>();
		this.instalaciones = new ArrayList<Instalacion>();
		this.reservas = new ArrayList<Reserva>();
		
	}
	
	public InscripcionAcademia inscribirSocio(Socio socio, Nivel nivel, LocalDate fecha) throws ReglaNegocioException{
		for (InscripcionAcademia i : inscripciones) {
			if(i.getSocio() == socio) {
				throw new ReglaNegocioException(socio.getNombre() + " ya esta inscrito en " + nombre);
	
			}
		}
		InscripcionAcademia inscripcion = new InscripcionAcademia(socio, disciplina, nivel, fecha);
		inscripciones.add(inscripcion);
		return inscripcion;
	}
	
	public void agregarInstalacion(Instalacion instalacion) {
		instalaciones.add(instalacion);
		
	}
	
	public Reserva reservar(Instalacion instalacion, Socio socio, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, int numJugadores, Modalidad modalidad) throws ReglaNegocioException{
		if(!instalaciones.contains(instalacion)) {
			throw new ReglaNegocioException(instalacion.getNombre() + " no pertenece a la academia " + nombre);
		}
		Reserva nueva = new Reserva(instalacion, socio, fecha, horaInicio, horaFin, numJugadores, modalidad);
		for (Reserva r : reservas) {
			if(r.coincideEnHorario(nueva)) {
				throw new ReglaNegocioException(instalacion.getNombre() + " ya esta reservado en este horario");
			}
		}
		List<Instalacion> instalacionesEnUso = new ArrayList<Instalacion>();
		for (Reserva r: reservas) {
			boolean mismoHorario = r.getFecha().equals(fecha) && r.getHoraInicio().isBefore(horaFin) && horaInicio.isBefore(r.getHoraFin());
			if(mismoHorario && !instalacionesEnUso.contains(r.getInstalacion())) {
				instalacionesEnUso.add(r.getInstalacion());
			}
		}
		if(instalacionesEnUso.size() >= maxInstalacionesSimultaneas) {
			throw new ReglaNegocioException("No hay cupo: " + nombre + " ya tiene " + maxInstalacionesSimultaneas + " instalaciones en uso en ese horario");
			
		}
		
		reservas.add(nueva);
		return nueva;
	}
	
	public InscripcionAcademia getInscripcion(Socio socio) {
		for(InscripcionAcademia i : inscripciones) {
			if(i.getSocio() == socio) {
				return i;
			}
		}
		return null;
	}
	
	public String getNombre() {
		return nombre;
		
	}
	
	public Disciplina getDisciplina() {
		return disciplina;
		
	}
	
	public int getMaxInstalacionesSimultaneas() {
		return maxInstalacionesSimultaneas;
		
	}
	
	public List<InscripcionAcademia> getInscripciones(){
		return new ArrayList<InscripcionAcademia>(inscripciones);
		
	}
	
	public List<Instalacion> getInstalaciones(){
		return new ArrayList<Instalacion>(instalaciones);
		
	}
	
	public List<Reserva> getReservas(){
		return new ArrayList<Reserva>(reservas);
	}
	
	

}
