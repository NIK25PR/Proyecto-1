package cds.catalogo;

public class Snack extends ProductoCafeteria {

	public Snack(String nombre, double precio) {
		super(nombre, precio);
	}


	public boolean esBebidaCaliente() {
		return false;
	}

}
