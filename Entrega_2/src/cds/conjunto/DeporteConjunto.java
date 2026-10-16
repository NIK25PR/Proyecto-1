package cds.conjunto;

public enum DeporteConjunto {
	
	FUTBOL(7, true, "Futbol"),
	BALONCESTO(5, false, "Baloncesto"),
	VOLEIBOL(6, false, "Voleibol");
	
	private final int minJugadores;
	private final boolean permiteEmpate;
	private final String nombre;
	
	
	private DeporteConjunto(int minJugadores, boolean permiteEmpate, String nombre) {
		this.minJugadores = minJugadores;
		this.permiteEmpate = permiteEmpate;
		this.nombre = nombre;
	}
	
	
	public int getMinJugadores() { return minJugadores; }
	public boolean permiteEmpate() { return permiteEmpate; }
	public String getNombre() { return nombre; }

}
