package cds.catalogo;

import java.util.ArrayList;
import java.util.List;

public abstract class ProductoCafeteria implements Vendible {
	private final String nombre;
	private double precio;
	private final List<Alergeno> alergenos;


	protected ProductoCafeteria(String nombre, double precio) {
		this.nombre = nombre;
		this.precio = precio;
		alergenos = new ArrayList<Alergeno>();
	}


	public void agregarAlergeno(Alergeno a) {
		if (!alergenos.contains(a)) {
			alergenos.add(a);
		}
	}

	public abstract boolean esBebidaCaliente();


	public String getNombre() {
		return nombre;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		this.precio = precio;
	}

	public List<Alergeno> getAlergenos() {
		return alergenos;
	}

}
