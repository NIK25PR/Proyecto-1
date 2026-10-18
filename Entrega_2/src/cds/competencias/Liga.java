package cds.competencias;

import java.util.LinkedHashMap;
import java.util.Map;

import cds.conjunto.CategoriaEdad;
import cds.conjunto.DeporteConjunto;
import cds.conjunto.Equipo;

public class Liga extends Competencia {
	
	private int puntosVictoria;
	private int puntosEmpate;
	private Map<Equipo, Integer> tablaDePosiciones;
	
	public Liga(String nombre, String organizador, int temporada, DeporteConjunto deporte, CategoriaEdad categoria, int nominaMinima, int puntosVictoria, int puntosEmpate) {
		super(nombre, organizador, temporada,deporte,categoria,nominaMinima);
		this.puntosVictoria = puntosVictoria;
		this.puntosEmpate = puntosEmpate;
		this.tablaDePosiciones = new LinkedHashMap<Equipo, Integer>();
		
	}
	
	@Override
	
	public void registrarResultado(PartidoCompetencia partido) {
		Equipo equipo = partido.getNomina().getEquipo();
		
		int puntosActuales;
		if(tablaDePosiciones.containsKey(equipo)) {
			puntosActuales = tablaDePosiciones.get(equipo);
		}else {
			puntosActuales = 0;
		}
		
		int puntosGanados;
		if(partido.getMarcadorLocal() == partido.getMarcadorVisitante()) {
			puntosGanados = puntosEmpate;
		}else if (partido.getMarcadorLocal() > partido.getMarcadorVisitante()){
			puntosGanados = puntosVictoria;
			
		}else {
			puntosGanados = 0;
		}
		
		tablaDePosiciones.put(equipo, puntosActuales + puntosGanados);
	}
	
	public Map<Equipo, Integer> getTablaDePosiciones(){
		return new LinkedHashMap<Equipo, Integer>(tablaDePosiciones);
		
	}
	
	public int getPuntosVictoria() {
		return puntosVictoria;
		
	}
	
	public int getPuntosEmpate() {
		return puntosEmpate;
	}
	

}
