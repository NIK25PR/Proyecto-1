package cds.catalogo;

public class Bebida extends ProductoCafeteria {
	private final boolean caliente;


	public Bebida(String nombre, double precio, boolean caliente) {
		super(nombre, precio);
		this.caliente = caliente;
	}


	public boolean esBebidaCaliente() {
		return caliente;
	}

	public boolean isCaliente() {
		return caliente;
	}

}
