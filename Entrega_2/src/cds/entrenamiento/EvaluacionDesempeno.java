package cds.entrenamiento;

public class EvaluacionDesempeno {
	
	private int nota;
	private String comentario;
	
	public EvaluacionDesempeno(int nota, String comentario) {
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
