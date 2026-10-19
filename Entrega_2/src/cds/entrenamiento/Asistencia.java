package cds.entrenamiento;

import cds.ReglaNegocioException;
import cds.usuarios.Socio;

public class Asistencia {
	
	private Socio socio;
	private EstadoAsistencia estado;
	private EvaluacionDesempeno evaluacion;
	
	public Asistencia(Socio socio) {
		this.socio = socio;
		this.estado = EstadoAsistencia.PENDIENTE;
		this.evaluacion = null;
		
	}
	
	public void registrarEstado(EstadoAsistencia estado) {
		this.estado = estado;
		
	}
	
	public void calificar(int nota, String comentario) throws ReglaNegocioException {
		this.evaluacion = new EvaluacionDesempeno(nota, comentario);
		
	}
	
	public Socio getSocio() {
		return socio;
		
	}
	
	public EstadoAsistencia getEstado() {
		return estado;
		
	}
	
	public EvaluacionDesempeno getEvaluacion() {
		return evaluacion;
	}

	
}
