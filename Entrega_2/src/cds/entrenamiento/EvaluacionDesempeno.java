package cds.entrenamiento;

import cds.ReglaNegocioException;

public class EvaluacionDesempeno {
	
	private int nota;
	private String comentario;
	
	public EvaluacionDesempeno(int nota, String comentario) throws ReglaNegocioException {
		if (nota < 1 || nota > 5) {
			throw new ReglaNegocioException("La nota debe estar entre 1 y 5");
		}
		this.nota = nota;
		this.comentario = comentario;
	}
	
	public int getNota() {
		return nota;
		
	}
	
	public String getComentario() {
		return comentario;
	}

}
