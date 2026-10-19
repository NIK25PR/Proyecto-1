package cds.academias;

import java.time.LocalDate;
import java.time.LocalTime;

import cds.ReglaNegocioException;
import cds.usuarios.Socio;

public class Reserva {
	
	private Instalacion instalacion;
	private Socio socio;
	private LocalDate fecha;
	private LocalTime horaInicio;
	private LocalTime horaFin;
	private int numJugadores;
	private Modalidad modalidad;
	
	public Reserva(Instalacion instalacion, Socio socio, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, int numJugadores, Modalidad modalidad) throws ReglaNegocioException{
		if (socio.tieneBebidaCaliente() && instalacion.restringeBebidaCaliente()) {
			throw new ReglaNegocioException("No se puede usar " + instalacion.getNombre() + " con una bebida caliente");
		}
		int capacidad = instalacion.capacidadMaxima(modalidad);
		if(numJugadores < 1 || numJugadores > capacidad) {
			throw new ReglaNegocioException("La instalacion " + instalacion.getNombre() + " adminte hasta " + capacidad + " jugadores en modalidad " + modalidad);
			
		}
		
		if(!horaFin.isAfter(horaInicio)) {
			throw new ReglaNegocioException("La hora de fin debe ser posterior a la hora de inicio");
			
		}
		
		this.instalacion = instalacion;
		this.socio = socio;
		this.fecha = fecha;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
		this.numJugadores = numJugadores;
		this.modalidad = modalidad;
		
	}
	
	public boolean coincideEnHorario(Reserva otra) {
		if(this.instalacion!= otra.instalacion || !this.fecha.equals(otra.fecha)) {
			return false;
		}
		return this.horaInicio.isBefore(otra.horaFin) && otra.horaInicio.isBefore(this.horaFin);
		
	}
	
	public Instalacion getInstalacion() {
		return instalacion;
		
	}
	
	public Socio getSocio() {
		return socio;
		
	}
	
	public LocalDate getFecha() {
		return fecha;
		
	}
	
	public LocalTime getHoraInicio() {
		return horaInicio;
		
	}
	
	public LocalTime getHoraFin() {
		return horaFin;
		
	}
	
	public int getNumJugadores() {
		return numJugadores;
		
	}
	
	public Modalidad getModalidad() {
		return modalidad;
	}

}
