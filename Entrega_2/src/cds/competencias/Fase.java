package cds.competencias;

import java.util.ArrayList;
import java.util.List;

public class Fase {
	
	private String nombre;
	private int orden;
	private TipoFase tipo;
	private List<PartidoCompetencia> partidos;
	
	Fase(String nombre, int orden, TipoFase tipo){
		
		this.nombre = nombre;
		this.orden = orden;
		this.tipo = tipo;
		this.partidos = new ArrayList<PartidoCompetencia>();
			
	}
	public void agregarPartido(PartidoCompetencia partido) {
		partidos.add(partido);
	}
	
	public String getNombre() {
		return nombre;
	}
	
	public int getOrden() {
		return orden;
	}
	
	public TipoFase getTipo() {
		return tipo;
	}
	
	public List<PartidoCompetencia> getPartidos(){
		return new ArrayList<PartidoCompetencia>(partidos);
	}

}
