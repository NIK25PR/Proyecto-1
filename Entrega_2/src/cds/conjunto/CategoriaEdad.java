package cds.conjunto;

public enum CategoriaEdad {

	SUB_13(0, 13, "Sub-13"),
	SUB_15(14, 15, "Sub-15"),
	SUB_17(16, 17, "Sub-17"),
	MAYORES(18, Integer.MAX_VALUE, "Mayores");

	private final int edadMinima;
	private final int edadMaxima;
	private final String nombre;


	CategoriaEdad(int edadMinima, int edadMaxima, String nombre) {
		this.edadMinima = edadMinima;
		this.edadMaxima = edadMaxima;
		this.nombre = nombre;
	}


	public boolean admite(int edad) {
		return edad >= edadMinima && edad <= edadMaxima;
	}

	public int getEdadMinima() { return edadMinima; }
	public int getEdadMaxima() { return edadMaxima; }
	public String getNombre() { return nombre; }

}
