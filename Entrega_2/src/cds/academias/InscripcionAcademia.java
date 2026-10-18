package cds.academias;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cds.usuarios.Socio;

public class InscripcionAcademia {
	
	private Socio socio;
	private Disciplina disciplina;
	private Nivel nivel;
	private LocalDate fechaInscripcion;
	private double puntajeRanking;
	private List<RegistroRanking> historial;
	
	public InscripcionAcademia(Socio socio, Disciplina disciplina, Nivel nivel, LocalDate fechaInscripcion) {
		this.socio = socio;
		this.disciplina = disciplina;
		this.nivel = nivel;
		this.fechaInscripcion = fechaInscripcion;
		this.puntajeRanking = 1000.0;
		this.historial = new ArrayList<RegistroRanking>();
		
	}
	
	public void actualizarPuntaje(double resultado, double puntajeRival, LocalDate fecha) {
		double diferencia = puntajeRanking - puntajeRival;
		double esperado = 0.5 + diferencia / 800.0;
		
		if(esperado > 1.0) {
			esperado = 1.0;
			
		}
		
		if(esperado < 0.0) {
			esperado = 0.0;
			
		}
		
		puntajeRanking = puntajeRanking + 32.0 * (resultado - esperado);
		historial.add(new RegistroRanking(fecha, puntajeRanking));
		
	}
	
	private int nivelComoNumero(Nivel nivel) {
		if(nivel == Nivel.PRINCIPIANTE) {
			return 1;
		}else if (nivel == Nivel.INTERMEDIO) {
			return 2;
		}else {
			return 3;
		}
		
	}
	
	public boolean esNivelCompatibleCon(InscripcionAcademia otra) {
		int miNivel = nivelComoNumero(this.nivel);
		int otroNivel = nivelComoNumero(otra.nivel);
		
		int diferencia;
		if(miNivel >= otroNivel) {
			diferencia = miNivel - otroNivel;
		}else {
			diferencia = otroNivel - miNivel;
		}
		return diferencia <= 1;
		
	}
	
	public Socio getSocio() {
		return socio;
		
	}
	
	public Disciplina getDisiciplina() {
		return disciplina;
		
	}
	
	public Nivel getNivel() {
		return nivel;
		
	}
	
	public LocalDate getFechaInscripcion() {
		return fechaInscripcion;
		
	}
	
	public double getPuntajeRanking() {
		return puntajeRanking;
		
	}
	
	public List<RegistroRanking> getHistorial(){
		return new ArrayList<RegistroRanking>(historial);
		
	}
}
