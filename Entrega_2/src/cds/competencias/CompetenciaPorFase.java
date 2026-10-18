package cds.competencias;

import java.util.ArrayList;
import java.util.List;

import cds.conjunto.CategoriaEdad;
import cds.conjunto.DeporteConjunto;

public class CompetenciaPorFase  extends Competencia{
	
	private List<Fase> fases;
	
	public CompetenciaPorFase(String nombre,String organizador, int temporada, DeporteConjunto deporte, CategoriaEdad categoria, int nominaMinima) {
		super(nombre, organizador,temporada,deporte,categoria,nominaMinima);
		this.fases = new ArrayList<Fase>();
		
	}
	
	public Fase agregarFase(String nombre, int orden, TipoFase tipo) {
		Fase fase = new Fase(nombre, orden, tipo);
		fases.add(fase);
		return fase;
	}
	
	@Override
	public void registrarResultado(PartidoCompetencia partido) {
		
	}
	
	public List<Fase> getFases(){
		return new ArrayList<Fase>(fases);
	}
	
	

}
